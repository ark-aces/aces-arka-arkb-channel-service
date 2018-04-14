package com.arkaces.arka_arkb_channel_service.transfer;

import ark_java_client.ArkClient;
import com.arkaces.aces_server.common.identifer.IdentifierGenerator;
import com.arkaces.arka_arkb_channel_service.FeeSettings;
import com.arkaces.arka_arkb_channel_service.ServiceArkbAccountSettings;
import com.arkaces.arka_arkb_channel_service.ark.ArkSatoshiService;
import com.arkaces.arka_arkb_channel_service.contract.ContractEntity;
import com.arkaces.arka_arkb_channel_service.contract.ContractRepository;
import com.arkaces.arka_arkb_channel_service.exchange_rate.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ArkTransactionEventHandler {

    private final ContractRepository contractRepository;
    private final TransferRepository transferRepository;
    private final IdentifierGenerator identifierGenerator;
    private final ArkClient arkaClient;
    private final ArkSatoshiService arkSatoshiService;
    private final FeeSettings feeSettings;
    private final ArkClient arkbClient;
    private final BigDecimal arkbPerArka;
    private final ServiceArkbAccountSettings serviceArkbAccountSettings;

    @PostMapping("/arkaEvents")
    public ResponseEntity<Void> handleEvent(@RequestBody ArkTransactionEventPayload eventPayload) {
        String arkaTransactionId = eventPayload.getTransactionId();
        
        log.info("Received Arka event: " + arkaTransactionId + " -> " + eventPayload.getData());
        
        String subscriptionId = eventPayload.getSubscriptionId();
        ContractEntity contractEntity = contractRepository.findOneBySubscriptionId(subscriptionId);
        if (contractEntity != null) {
            // todo: lock contract for update to prevent concurrent processing of a listener transaction.
            // Listeners send events serially, so that shouldn't be an issue, but we might want to lock
            // to be safe.

            log.info("Matched event for contract id " + contractEntity.getId() + " arka transaction id " + arkaTransactionId);

            String transferId = identifierGenerator.generate();

            TransferEntity transferEntity = new TransferEntity();
            transferEntity.setId(transferId);
            transferEntity.setCreatedAt(LocalDateTime.now());
            transferEntity.setArkaTransactionId(arkaTransactionId);
            transferEntity.setContractEntity(contractEntity);

            // Get Arka amount from transaction
            ArkTransaction arkTransaction = eventPayload.getData();

            BigDecimal incomingArkaAmount = arkSatoshiService.toArk(arkTransaction.getAmount());
            transferEntity.setArkaAmount(incomingArkaAmount);

            transferEntity.setArkbPerArka(arkbPerArka);

            // Deduct fees
            transferEntity.setArkaFlatFee(feeSettings.getArkaFlatFee());
            transferEntity.setArkaPercentFee(feeSettings.getArkaPercentFee());

            BigDecimal percentFee = feeSettings.getArkaPercentFee()
                    .divide(new BigDecimal("100.00"), 8, BigDecimal.ROUND_HALF_UP);
            BigDecimal arkaTotalFeeAmount = incomingArkaAmount.multiply(percentFee).add(feeSettings.getArkaFlatFee());
            transferEntity.setArkaTotalFee(arkaTotalFeeAmount);

            // Calculate send ark amount
            BigDecimal arkaSendAmount = incomingArkaAmount.subtract(arkaTotalFeeAmount);
            BigDecimal arkbSendAmount = arkaSendAmount.multiply(arkbPerArka).setScale(8, RoundingMode.HALF_DOWN);
            // todo: optionally subtract arkb transaction fee
            BigDecimal minArkbAmount = BigDecimal.ZERO; // todo: this should be expected arkb txn fee
            if (arkbSendAmount.compareTo(minArkbAmount) <= 0) {
                arkbSendAmount = BigDecimal.ZERO;
            }
            transferEntity.setArkbSendAmount(arkbSendAmount);

            transferEntity.setStatus(TransferStatus.NEW);
            transferRepository.save(transferEntity);

            // Check that service has enough arkb to send
            // todo: get arkb balance
            // todo: we may need to just depend on confirmations
            BigDecimal serviceAvailableArkb = arkbSendAmount;

            // Send arkb transaction
            if (arkbSendAmount.compareTo(BigDecimal.ZERO) >= 0) {
                String arkbTransactionId = arkbClient.broadcastTransaction(contractEntity.getRecipientArkbAddress(), arkSatoshiService.toSatoshi(arkbSendAmount), null, serviceArkbAccountSettings.getPassphrase(), 10);
                transferEntity.setArkbTransactionId(arkbTransactionId);

                log.info("Sent " + arkbSendAmount + " arkb to " + contractEntity.getRecipientArkbAddress()
                        + ", arkb transaction id " + arkbTransactionId + ", ark transaction " + arkaTransactionId);

                // todo: asynchronously confirm transaction, if transaction fails to confirm we should return arka amount
                transferEntity.setNeedsArkbConfirmation(true);
            }

            transferEntity.setStatus(TransferStatus.COMPLETE);
            transferRepository.save(transferEntity);
            
            log.info("Saved transfer id " + transferEntity.getId() + " to contract " + contractEntity.getId());
        }
        
        return ResponseEntity.ok().build();
    }
}

package com.arkaces.arka_arkb_channel_service.transfer;

import com.arkaces.aces_server.common.identifer.IdentifierGenerator;
import com.arkaces.arka_arkb_channel_service.Constants;
import com.arkaces.arka_arkb_channel_service.FeeSettings;
import com.arkaces.arka_arkb_channel_service.ark.ArkSatoshiService;
import com.arkaces.arka_arkb_channel_service.contract.ContractEntity;
import com.arkaces.arka_arkb_channel_service.contract.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ArkSatoshiService arkSatoshiService;
    private final FeeSettings feeSettings;
    private final BigDecimal arkbPerArka;
    private final ApplicationEventPublisher applicationEventPublisher;

    @PostMapping("/arkaEvents")
    public ResponseEntity<Void> handleEvent(@RequestBody ArkTransactionEventPayload eventPayload) {
        String arkaTransactionId = eventPayload.getTransactionId();
        
        log.info("Received Arka event: " + arkaTransactionId + " -> " + eventPayload.getData());
        
        String subscriptionId = eventPayload.getSubscriptionId();
        ContractEntity contractEntity = contractRepository.findOneBySubscriptionId(subscriptionId);
        if (contractEntity == null) {
            log.info("Arka event has no corresponding contract: " + eventPayload);
            return ResponseEntity.ok().build();
        }

        log.info("Matched event for contract id " + contractEntity.getId() + " arka transaction id " + arkaTransactionId);

        TransferEntity existingTransferEntity = transferRepository.findOneByArkaTransactionId(arkaTransactionId);
        if (existingTransferEntity != null) {
            log.info("Transfer for ark transaction " + arkaTransactionId + " already exists with id " + existingTransferEntity.getId());
            return ResponseEntity.ok().build();
        }
        
        String transferId = identifierGenerator.generate();

        TransferEntity transferEntity = new TransferEntity();
        transferEntity.setId(transferId);
        transferEntity.setStatus(TransferStatus.NEW);
        transferEntity.setCreatedAt(LocalDateTime.now());
        transferEntity.setArkaTransactionId(arkaTransactionId);
        transferEntity.setReturnArkaAddress(eventPayload.getData().getSenderId());
        transferEntity.setContractEntity(contractEntity);

        // Get Arka amount from transaction
        ArkTransaction arkTransaction = eventPayload.getData();

        BigDecimal incomingArkaAmount = arkSatoshiService.toArk(arkTransaction.getAmount());
        transferEntity.setArkaAmount(incomingArkaAmount);

        transferEntity.setArkbPerArka(arkbPerArka);
        
        transferEntity.setArkaFlatFee(feeSettings.getArkaFlatFee());
        transferEntity.setArkaPercentFee(feeSettings.getArkaPercentFee());

        BigDecimal percentFee = feeSettings.getArkaPercentFee()
                .divide(new BigDecimal("100.00"), 8, BigDecimal.ROUND_HALF_UP);
        BigDecimal arkaTotalFeeAmount = incomingArkaAmount.multiply(percentFee).add(feeSettings.getArkaFlatFee());
        transferEntity.setArkaTotalFee(arkaTotalFeeAmount);

        // Calculate send ark amount
        BigDecimal arkaSendAmount = incomingArkaAmount.subtract(arkaTotalFeeAmount);
        BigDecimal arkbSendAmount = arkaSendAmount.multiply(arkbPerArka).setScale(8, RoundingMode.HALF_DOWN);
        if (arkbSendAmount.compareTo(Constants.ARK_TRANSACTION_FEE) <= 0) {
            arkbSendAmount = BigDecimal.ZERO;
        }
        transferEntity.setArkbSendAmount(arkbSendAmount);

        transferRepository.save(transferEntity);

        log.info("Saved transfer id " + transferEntity.getId() + " to contract " + contractEntity.getId());

        NewTransferEvent newTransferEvent = new NewTransferEvent();
        newTransferEvent.setTransferPid(transferEntity.getPid());
        applicationEventPublisher.publishEvent(newTransferEvent);

        
        return ResponseEntity.ok().build();
    }

}

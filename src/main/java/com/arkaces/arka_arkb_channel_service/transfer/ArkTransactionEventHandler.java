package com.arkaces.arka_arkb_channel_service.transfer;

import ark_java_client.Transaction;
import com.arkaces.aces_server.common.identifer.IdentifierGenerator;
import com.arkaces.arka_arkb_channel_service.Constants;
import com.arkaces.arka_arkb_channel_service.FeeSettings;
import com.arkaces.arka_arkb_channel_service.ark.ArkSatoshiService;
import com.arkaces.arka_arkb_channel_service.ark_listener.NewArkTransactionEvent;
import com.arkaces.arka_arkb_channel_service.contract.ContractEntity;
import com.arkaces.arka_arkb_channel_service.contract.ContractRepository;
import com.arkaces.arka_arkb_channel_service.exchange_rate.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
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
    private final ExchangeRateService exchangeRateService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @EventListener
    @Transactional
    public void handleArkTransactionEvent(NewArkTransactionEvent eventPayload) {
        String arkaTransactionId = eventPayload.getTransactionId();

        log.info("Received Arka event: " + arkaTransactionId + " -> " + eventPayload.getTransaction());

        ContractEntity contractEntity = contractRepository.findOne(eventPayload.getContractPid());
        if (contractEntity == null) {
            log.info("Arka event has no corresponding contract: " + eventPayload);
            return;
        }

        log.info("Matched event for contract id " + contractEntity.getId() + " arka transaction id " + arkaTransactionId);

        TransferEntity existingTransferEntity = transferRepository.findOneByArkaTransactionId(arkaTransactionId);
        if (existingTransferEntity != null) {
            log.info("Transfer for ark transaction " + arkaTransactionId + " already exists with id " + existingTransferEntity.getId());
            return;
        }

        String transferId = identifierGenerator.generate();

        TransferEntity transferEntity = new TransferEntity();
        transferEntity.setId(transferId);
        transferEntity.setStatus(TransferStatus.NEW);
        transferEntity.setCreatedAt(LocalDateTime.now());
        transferEntity.setArkaTransactionId(arkaTransactionId);
        transferEntity.setReturnArkaAddress(eventPayload.getTransaction().getSenderId());
        transferEntity.setContractEntity(contractEntity);

        // Get Arka amount from transaction
        Transaction arkTransaction = eventPayload.getTransaction();

        BigDecimal incomingArkaAmount = arkSatoshiService.toArk(arkTransaction.getAmount());
        transferEntity.setArkaAmount(incomingArkaAmount);

        BigDecimal arkbToArkaRate = exchangeRateService.getRate();
        transferEntity.setArkbPerArka(arkbToArkaRate);

        transferEntity.setArkaFlatFee(feeSettings.getArkaFlatFee());
        transferEntity.setArkaPercentFee(feeSettings.getArkaPercentFee());

        BigDecimal percentFee = feeSettings.getArkaPercentFee()
                .divide(new BigDecimal("100.00"), 8, BigDecimal.ROUND_HALF_UP);
        BigDecimal arkaTotalFeeAmount = incomingArkaAmount.multiply(percentFee).add(feeSettings.getArkaFlatFee());
        transferEntity.setArkaTotalFee(arkaTotalFeeAmount);

        // Calculate send ark amount
        BigDecimal arkaSendAmount = incomingArkaAmount.subtract(arkaTotalFeeAmount);
        BigDecimal arkbSendAmount = arkaSendAmount.multiply(arkbToArkaRate).setScale(8, RoundingMode.HALF_DOWN);
        if (arkbSendAmount.compareTo(Constants.ARK_TRANSACTION_FEE) <= 0) {
            arkbSendAmount = BigDecimal.ZERO;
        }
        transferEntity.setArkbSendAmount(arkbSendAmount);

        transferRepository.save(transferEntity);

        log.info("Saved transfer id " + transferEntity.getId() + " to contract " + contractEntity.getId());

        NewTransferEvent newTransferEvent = new NewTransferEvent();
        newTransferEvent.setTransferPid(transferEntity.getPid());
        applicationEventPublisher.publishEvent(newTransferEvent);
    }

}

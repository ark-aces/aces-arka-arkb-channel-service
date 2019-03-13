package com.arkaces.arka_arkb_channel_service.ark_listener;

import ark_java_client.ArkClient;
import ark_java_client.Transaction;
import com.arkaces.arka_arkb_channel_service.contract.ContractEntity;
import com.arkaces.arka_arkb_channel_service.contract.ContractRepository;
import com.arkaces.arka_arkb_channel_service.transfer.TransferEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ArkListener {

    private final ContractRepository contractRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final Integer arkScanDepth;
    private final Integer arkMinConfirmations;
    private final ArkClient arkaClient;

    @Scheduled(fixedDelayString = "${arkScanIntervalSec}000")
    @Transactional
    public void scan() {
        log.info("Scanning for transactions");
        try {
            Integer limit = 50;
            Map<String, Transaction> transactionsById = new HashMap<>();
            for (Integer offset = 0; offset < arkScanDepth; offset += limit) {
                arkaClient.getTransactions(limit, offset)
                        .forEach(transaction -> transactionsById.put(transaction.getId(), transaction));
            }

            List<ContractEntity> contractEntities = contractRepository.findAll();
            for (ContractEntity contractEntity : contractEntities) {
                Set<String> existingTxnIds = contractEntity.getTransferEntities().stream()
                        .map(TransferEntity::getArkaTransactionId)
                        .collect(Collectors.toSet());

                Set<String> newTxnIds = transactionsById.values().stream()
                        .filter(transaction -> transaction.getRecipientId().equals(contractEntity.getDepositArkaAddress()))
                        .filter(transaction -> ! existingTxnIds.contains(transaction.getId()))
                        .filter(transaction -> transaction.getConfirmations() >= arkMinConfirmations)
                        .map(Transaction::getId)
                        .collect(Collectors.toSet());

                for (String txnId : newTxnIds) {
                    NewArkTransactionEvent newArkTransactionEvent = new NewArkTransactionEvent();
                    newArkTransactionEvent.setContractPid(contractEntity.getPid());
                    newArkTransactionEvent.setTransactionId(txnId);
                    newArkTransactionEvent.setTransaction(transactionsById.get(txnId));
                    applicationEventPublisher.publishEvent(newArkTransactionEvent);
                }
            }
        }
        catch (Exception e) {
            log.error("Transaction listener threw exception while running", e);
        }
    }
}
package com.arkaces.arka_arkb_channel_service;

import com.arkaces.arka_arkb_channel_service.ark.ArkaService;
import com.arkaces.arka_arkb_channel_service.contract.ContractEntity;
import com.arkaces.arka_arkb_channel_service.contract.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@ConditionalOnProperty(value = "arkaSweep.enabled")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ArkSweeper {

    private final ArkaSweepSettings arkaSweepSettings;
    private final ArkaService arkaService;
    private final ContractRepository contractRepository;

    @Scheduled(fixedDelayString = "${arkaSweep.runIntervalSec}000")
    public void sweep() {
        try {
            log.info("Executing ark wallet sweep");
            List<ContractEntity> contractEntities = contractRepository.findAll();
            for (ContractEntity contractEntity : contractEntities) {
                try {
                    sweepContract(contractEntity);
                } catch (Exception e) {
                    log.error("Failed to send sweep transfer for contract " + contractEntity.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("Failed to execute sweep", e);
        }
    }

    private void sweepContract(ContractEntity contractEntity) {
        BigDecimal balance = arkaService.getArkBalance(contractEntity.getDepositArkaAddress());
        BigDecimal sendAmount = balance.subtract(Constants.ARK_TRANSACTION_FEE);
        if (sendAmount.compareTo(BigDecimal.ZERO) > 0) {
            String fromAddress = contractEntity.getDepositArkaAddress();
            String arkaServiceAddress = arkaSweepSettings.getArkaAddress();
            log.info("Sending sweep transfer from {} to {} with amount {}", arkaServiceAddress, fromAddress, sendAmount);

            String transactionId = arkaService.sendTransaction(
                    arkaServiceAddress,
                    sendAmount,
                    contractEntity.getDepositArkaAddressPassphrase()
            );

            log.info("Successfully sent sweep transfer with transaction id " + transactionId);
        }
    }

}

package com.arkaces.arka_arkb_channel_service.transfer;

import com.arkaces.aces_server.aces_service.notification.NotificationService;
import com.arkaces.aces_server.common.error.NotFoundException;
import com.arkaces.arka_arkb_channel_service.Constants;
import com.arkaces.arka_arkb_channel_service.ark.ArkService;
import com.arkaces.arka_arkb_channel_service.ark.ArkaService;
import com.arkaces.arka_arkb_channel_service.contract.ContractEntity;
import com.arkaces.arka_arkb_channel_service.service_capacity.ServiceCapacityEntity;
import com.arkaces.arka_arkb_channel_service.service_capacity.ServiceCapacityRepository;
import com.arkaces.arka_arkb_channel_service.service_capacity.ServiceCapacityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@Transactional
public class TransferService {

    private final TransferRepository transferRepository;
    private final ArkService arkService;
    private final ArkaService arkaService;
    private final ServiceCapacityService serviceCapacityService;
    private final ServiceCapacityRepository serviceCapacityRepository;
    private final NotificationService notificationService;
    private final BigDecimal lowCapacityThreshold;

    /**
     * @return true if amount reserved successfully
     */
    public boolean reserveTransferCapacity(Long transferPid) {
        // Lock service capacity and update available balance if available
        ServiceCapacityEntity serviceCapacityEntity = serviceCapacityService.getLockedCapacityEntity();

        TransferEntity transferEntity = transferRepository.findOneForUpdate(transferPid);
        BigDecimal transferAmount = transferEntity.getArkbSendAmount().add(Constants.ARK_TRANSACTION_FEE);
        BigDecimal newAvailableAmount = serviceCapacityEntity.getAvailableAmount().subtract(transferAmount);
        BigDecimal newUnsettledAmount = serviceCapacityEntity.getUnsettledAmount().add(transferAmount);
        if (newAvailableAmount.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        
        serviceCapacityEntity.setAvailableAmount(newAvailableAmount);
        serviceCapacityEntity.setUnsettledAmount(newUnsettledAmount);
        serviceCapacityRepository.save(serviceCapacityEntity);

        if (serviceCapacityEntity.getAvailableAmount().compareTo(lowCapacityThreshold) <= 0) {
            notificationService.notifyLowCapacity(serviceCapacityEntity.getAvailableAmount(), serviceCapacityEntity.getUnit());
        }

        return true;
    }
    
    public void settleTransferCapacity(Long transferPid) {
        ServiceCapacityEntity serviceCapacityEntity = serviceCapacityService.getLockedCapacityEntity();

        TransferEntity transferEntity = transferRepository.findOne(transferPid);
        BigDecimal transferAmount = transferEntity.getArkbSendAmount().add(Constants.ARK_TRANSACTION_FEE);

        serviceCapacityEntity.setUnsettledAmount(serviceCapacityEntity.getUnsettledAmount().subtract(transferAmount));
        serviceCapacityEntity.setTotalAmount(serviceCapacityEntity.getTotalAmount().subtract(transferAmount));

        serviceCapacityRepository.save(serviceCapacityEntity);
    }
    
    public void processNewTransfer(Long transferPid) {
        TransferEntity transferEntity = transferRepository.findOneForUpdate(transferPid);
        ContractEntity contractEntity = transferEntity.getContractEntity();

        if (transferEntity.getArkbSendAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal arkSendAmount = transferEntity.getArkbSendAmount();
            String recipientArkbAddress = contractEntity.getRecipientArkbAddress();
            String arkTransactionId = arkService.sendTransaction(recipientArkbAddress, arkSendAmount);
            transferEntity.setArkbTransactionId(arkTransactionId);

            log.info("Sent " + arkSendAmount + " ark to " + contractEntity.getRecipientArkbAddress()
                + ", arkb transaction id " + arkTransactionId + ", arka transaction " + transferEntity.getArkaTransactionId());
        } 
        
        transferEntity.setStatus(TransferStatus.COMPLETE);
        transferRepository.save(transferEntity);

        log.info("Saved transfer id " + transferEntity.getId() + " to contract " + contractEntity.getId());

        notificationService.notifySuccessfulTransfer(
                transferEntity.getContractEntity().getId(),
                transferEntity.getId()
        );
    }

    /**
     * Process a full return due to insufficient capacity
     * @param transferPid
     */
    public void processReturn(Long transferPid) {
        TransferEntity transferEntity = transferRepository.findOneForUpdate(transferPid);

        log.info("Insufficient ark to send transfer id = " + transferEntity.getId());

        String returnArkaAddress = transferEntity.getReturnArkaAddress();
        if (returnArkaAddress != null) {
            BigDecimal returnAmount = transferEntity.getArkaAmount().subtract(Constants.ARK_TRANSACTION_FEE);
            String returnArkaTransactionId = arkaService.sendTransaction(returnArkaAddress, returnAmount,
                    transferEntity.getContractEntity().getDepositArkaAddressPassphrase());
            transferEntity.setStatus(TransferStatus.RETURNED);
            transferEntity.setReturnArkaTransactionId(returnArkaTransactionId);

            log.info("Sent " + transferEntity.getArkaAmount() + " ark to " + returnArkaAddress
                    + ", arka transaction id " + returnArkaTransactionId + " for arka deposit " + transferEntity.getArkaTransactionId());
        } else {
            log.warn("Ark return could not be processed for transfer " + transferPid);
            transferEntity.setStatus(TransferStatus.FAILED);
        }

        transferRepository.save(transferEntity);
    }
    
    public void processFailedTransfer(Long transferPid, String message) {
        TransferEntity transferEntity = transferRepository.findOneForUpdate(transferPid);
        transferEntity.setStatus(TransferStatus.FAILED);
        transferRepository.save(transferEntity);

        notificationService.notifyFailedTransfer(
                transferEntity.getContractEntity().getId(),
                transferEntity.getId(),
                message
        );
    }
    
}
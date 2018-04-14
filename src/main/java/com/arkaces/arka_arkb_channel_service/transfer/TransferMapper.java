package com.arkaces.arka_arkb_channel_service.transfer;

import org.springframework.stereotype.Service;

import java.time.ZoneOffset;

@Service
public class TransferMapper {
    
    public Transfer map(TransferEntity transferEntity) {
        Transfer transfer = new Transfer();
        transfer.setId(transferEntity.getId());
        transfer.setStatus(transferEntity.getStatus());
        transfer.setArkaTransactionId(transferEntity.getArkaTransactionId());
        transfer.setArkbSendAmount(transferEntity.getArkbSendAmount().toPlainString());
        transfer.setArkbTransactionId(transferEntity.getArkbTransactionId());
        transfer.setArkaAmount(transferEntity.getArkaAmount().toPlainString());
        transfer.setArkaFlatFee(transferEntity.getArkaFlatFee().toPlainString());
        transfer.setArkaPercentFee(transferEntity.getArkaPercentFee().toPlainString());
        transfer.setArkbPerArka(transferEntity.getArkbPerArka().toPlainString());
        transfer.setArkaTotalFee(transferEntity.getArkaTotalFee().toPlainString());
        transfer.setCreatedAt(transferEntity.getCreatedAt().atOffset(ZoneOffset.UTC).toString());
        
        return transfer;
    }
    
}

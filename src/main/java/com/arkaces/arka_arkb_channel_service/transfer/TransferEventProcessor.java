package com.arkaces.arka_arkb_channel_service.transfer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class TransferEventProcessor {
    
    private final TransferService transferService;

    @EventListener
    public void handleNewTransferEvent(NewTransferEvent newTransferEvent) {
        Long transferPid = newTransferEvent.getTransferPid();
        if (transferService.reserveTransferCapacity(transferPid)) {
            try {
                transferService.processNewTransfer(transferPid);
            } catch (Exception e) {
                log.error("Exception settling failed transfer event", e);
                transferService.processFailedTransfer(transferPid, e.getMessage());
            }
            transferService.settleTransferCapacity(transferPid);
        } else {
            transferService.processReturn(transferPid);
        }
    }
}
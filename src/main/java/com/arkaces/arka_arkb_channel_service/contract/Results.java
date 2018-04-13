package com.arkaces.arka_arkb_channel_service.contract;

import com.arkaces.arka_arkb_channel_service.transfer.Transfer;
import lombok.Data;

import java.util.List;

@Data
public class Results {
    private String recipientArkbAddress;
    private String depositArkaAddress;
    private List<Transfer> transfers;
}
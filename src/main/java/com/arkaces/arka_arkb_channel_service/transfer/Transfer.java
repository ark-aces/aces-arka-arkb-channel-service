package com.arkaces.arka_arkb_channel_service.transfer;

import lombok.Data;

@Data
public class Transfer {
    private String id;
    private String status;
    private String createdAt;
    private String arkaTransactionId;
    private String arkaAmount;
    private String arkbPerArka;
    private String arkaFlatFee;
    private String arkaPercentFee;
    private String arkaTotalFee;
    private String arkbSendAmount;
    private String arkbTransactionId;
}

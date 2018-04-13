package com.arkaces.arka_arkb_channel_service.transfer;

import lombok.Data;

@Data
public class ArkTransactionEventPayload {
    private String id;
    private String transactionId;
    private ArkTransaction data;
    private String createdAt;
    private String subscriptionId;
}


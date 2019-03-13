package com.arkaces.arka_arkb_channel_service.ark_listener;

import lombok.Data;

import ark_java_client.Transaction;

@Data
public class NewArkTransactionEvent {
    private Long contractPid;
    private String transactionId;
    private Transaction transaction;
}
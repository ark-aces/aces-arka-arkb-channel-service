package com.arkaces.arka_arkb_channel_service.exchange_rate;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExchangeRate {
    private BigDecimal rate;
    private String from;
    private String to;
}
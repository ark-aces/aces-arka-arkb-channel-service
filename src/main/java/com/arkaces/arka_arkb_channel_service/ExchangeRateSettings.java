package com.arkaces.arka_arkb_channel_service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Data
@Component
@ConfigurationProperties(prefix = "exchange-rate")
public class ExchangeRateSettings {
    private String fromSymbol;
    private String toSymbol;
    private BigDecimal multiplier;
    private BigDecimal fixedRate;
}

package com.arkaces.arka_arkb_channel_service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Data
@Component
@ConfigurationProperties(prefix = "fees")
public class FeeSettings {
    private BigDecimal arkaFlatFee;
    private BigDecimal arkaPercentFee;
}

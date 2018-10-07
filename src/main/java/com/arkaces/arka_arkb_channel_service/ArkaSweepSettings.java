package com.arkaces.arka_arkb_channel_service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "arkaSweep")
public class ArkaSweepSettings {
    private Boolean enabled;
    private String arkaAddress;
    private Integer runIntervalSec;
}

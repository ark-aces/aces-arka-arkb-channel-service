package com.arkaces.arka_arkb_channel_service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "serviceArkbAccount")
public class ServiceArkbAccountSettings {
    private String address;
    private String passphrase;
}

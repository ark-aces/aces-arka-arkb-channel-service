package com.arkaces.arka_arkb_channel_service.exchange_rate;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;


// todo: make ARKA-ARKB service rate dynamic using this service. We provide an API example as a placeholder.
@Service
public class ExchangeRateService
{
    private final RestTemplate restTemplate = new RestTemplateBuilder()
        .rootUri("https://min-api.cryptocompare.com/data/")
        .build();

    public BigDecimal getRate(String fromCurrencyCode, String toCurrencyCode) {
        return restTemplate
            .exchange(
                "/price?fsym={from}&tsyms={to}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, BigDecimal>>() {},
                fromCurrencyCode,
                toCurrencyCode
            )
            .getBody()
            .get(toCurrencyCode);
    }
}

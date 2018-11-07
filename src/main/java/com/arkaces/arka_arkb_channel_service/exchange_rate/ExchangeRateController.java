package com.arkaces.arka_arkb_channel_service.exchange_rate;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExchangeRateController {

    private final String arkaUnit;
    private final String arkbUnit;
    private final BigDecimal arkbPerArka;

    @GetMapping("/exchangeRate")
    public ExchangeRate getExchangeRate() {
        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setFrom(arkaUnit);
        exchangeRate.setTo(arkbUnit);
        exchangeRate.setRate(arkbPerArka);

        return exchangeRate;
    }
}

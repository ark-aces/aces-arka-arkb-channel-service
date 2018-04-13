package com.arkaces.arka_arkb_channel_service.ark;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;

@Service
public class ArkSatoshiService {

    private final BigInteger SATOSHIS_PER_ARK = new BigInteger("100000000");

    public Long toSatoshi(BigDecimal arkAmount) {
        return arkAmount
            .multiply(new BigDecimal(SATOSHIS_PER_ARK))
            .toBigIntegerExact()
            .longValue();
    }

    public BigDecimal toArk(Long satoshis) {
        return new BigDecimal(satoshis)
            .setScale(14, BigDecimal.ROUND_UP)
            .divide(new BigDecimal(SATOSHIS_PER_ARK), BigDecimal.ROUND_UP);
    }
}

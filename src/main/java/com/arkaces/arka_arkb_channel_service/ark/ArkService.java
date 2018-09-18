package com.arkaces.arka_arkb_channel_service.ark;

import ark_java_client.ArkClient;
import com.arkaces.arka_arkb_channel_service.ServiceArkbAccountSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ArkService {

    private final ArkClient arkbClient;
    private final ServiceArkbAccountSettings serviceArkAccountSettings;
    private final ArkSatoshiService arkSatoshiService;

    public BigDecimal getServiceArkBalance() {
        return arkSatoshiService.toArk(Long.parseLong(
            arkbClient.getBalance(serviceArkAccountSettings.getAddress())
                .getBalance()));
    }

    public String sendTransaction(String recipientArkAddress, BigDecimal amount) {
        return sendTransaction(recipientArkAddress, amount, serviceArkAccountSettings.getPassphrase());
    }

    public String sendTransaction(String recipientArkAddress, BigDecimal amount, String passphrase) {
        Long arktoshiAmount = arkSatoshiService.toSatoshi(amount);
        return arkbClient.broadcastTransaction(
                recipientArkAddress,
                arktoshiAmount,
                null,
                passphrase,
                10
        );
    }
}
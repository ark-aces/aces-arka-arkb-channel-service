package com.arkaces.arka_arkb_channel_service;

import com.arkaces.ApiException;
import com.arkaces.aces_listener_api.AcesListenerApi;
import com.arkaces.aces_server.aces_listener.subscription.SubscriptionEntity;
import com.arkaces.aces_server.aces_listener.subscription.SubscriptionRepository;
import com.arkaces.arka_arkb_channel_service.contract.ContractEntity;
import com.arkaces.arka_arkb_channel_service.contract.ContractRepository;
import io.swagger.client.model.Subscription;
import io.swagger.client.model.SubscriptionRequest;
import io.swagger.client.model.SubscriptionResubscribe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ResubscribeInitializer {

    private final ContractRepository contractRepository;
    private final AcesListenerApi arkaListener;

    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshed() {
        log.info("Starting resubscribe initialization");
        List<ContractEntity> contractEntities = contractRepository.findAll();
        for (ContractEntity contractEntity : contractEntities) {
            log.info("Resubscribing contract id {} subscription id {}", contractEntity.getId(), contractEntity.getSubscriptionId());
            try {
                SubscriptionResubscribe resubscribe = arkaListener.subscriptionsIdResubscribesPost(contractEntity.getSubscriptionId());
                log.info("Resubscribe created: " + resubscribe.getCreatedAt().toString());
            } catch (ApiException e) {
                throw new RuntimeException("Arka Listener re-subscription failed to POST", e);
            }
        }
    }

}

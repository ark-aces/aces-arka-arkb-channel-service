package com.arkaces.arka_arkb_channel_service.contract;

import ark_java_client.ArkClient;
import com.arkaces.ApiException;
import com.arkaces.aces_listener_api.AcesListenerApi;
import com.arkaces.aces_server.aces_service.contract.Contract;
import com.arkaces.aces_server.aces_service.contract.ContractStatus;
import com.arkaces.aces_server.aces_service.contract.CreateContractRequest;
import com.arkaces.aces_server.aces_service.error.ServiceErrorCodes;
import com.arkaces.aces_server.common.error.NotFoundException;
import com.arkaces.aces_server.common.identifer.IdentifierGenerator;
import io.swagger.client.model.Subscription;
import io.swagger.client.model.SubscriptionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ContractController {
    
    private final IdentifierGenerator identifierGenerator;
    private final ContractRepository contractRepository;
    private final ContractMapper contractMapper;
    private final AcesListenerApi arkaListener;
    private final String arkaEventCallbackUrl;
    private final ArkClient arkClient;

    @PostMapping("/contracts")
    public Contract<Results> postContract(@RequestBody CreateContractRequest<Arguments> createContractRequest) {
        ContractEntity contractEntity = new ContractEntity();
        contractEntity.setCorrelationId(createContractRequest.getCorrelationId());
        contractEntity.setRecipientArkbAddress(createContractRequest.getArguments().getRecipientArkbAddress());
        contractEntity.setCreatedAt(LocalDateTime.now());
        contractEntity.setId(identifierGenerator.generate());
        contractEntity.setStatus(ContractStatus.EXECUTED);
        
        // generate arka wallet for deposits
        String depositArkaAddressPassphrase = identifierGenerator.generate();

        String depositArkaAddress = arkClient.getAddress(depositArkaAddressPassphrase);
        contractEntity.setDepositArkaAddress(depositArkaAddress);
        contractEntity.setDepositArkaAddressPassphrase(depositArkaAddressPassphrase);

        // subscribe to arka listener on deposit arka address
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
        subscriptionRequest.setCallbackUrl(arkaEventCallbackUrl);
        subscriptionRequest.setMinConfirmations(1);
        subscriptionRequest.setRecipientAddress(depositArkaAddress);
        Subscription subscription;
        try {
            subscription = arkaListener.subscriptionsPost(subscriptionRequest);
            log.info("subscription: " + subscription.toString());
        } catch (ApiException e) {
            throw new RuntimeException("Arka Listener subscription failed to POST", e);
        }
        contractEntity.setSubscriptionId(subscription.getId());

        contractRepository.save(contractEntity);

        return contractMapper.map(contractEntity);
    }
    
    @GetMapping("/contracts/{contractId}")
    public Contract<Results> getContract(@PathVariable String contractId) {
        ContractEntity contractEntity = contractRepository.findOneById(contractId);
        if (contractEntity == null) {
            throw new NotFoundException(ServiceErrorCodes.CONTRACT_NOT_FOUND, "Contract not found with id = " + contractId);
        }
        
        return contractMapper.map(contractEntity);
    }
}

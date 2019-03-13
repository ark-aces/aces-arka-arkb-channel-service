package com.arkaces.arka_arkb_channel_service.contract;

import ark_java_client.ArkClient;
import com.arkaces.aces_server.aces_service.contract.Contract;
import com.arkaces.aces_server.aces_service.contract.ContractStatus;
import com.arkaces.aces_server.aces_service.contract.CreateContractRequest;
import com.arkaces.aces_server.aces_service.error.ServiceErrorCodes;
import com.arkaces.aces_server.common.error.NotFoundException;
import com.arkaces.aces_server.common.identifer.IdentifierGenerator;
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
    private final ArkClient arkaClient;
    private final CreateContractRequestValidator contractRequestValidator;

    @PostMapping("/contracts")
    public Contract<Results> postContract(@RequestBody CreateContractRequest<Arguments> createContractRequest) {
        contractRequestValidator.validate(createContractRequest);
        
        ContractEntity contractEntity = new ContractEntity();
        contractEntity.setCorrelationId(createContractRequest.getCorrelationId());
        contractEntity.setRecipientArkbAddress(createContractRequest.getArguments().getRecipientArkbAddress());
        contractEntity.setCreatedAt(LocalDateTime.now());
        contractEntity.setId(identifierGenerator.generate());
        contractEntity.setStatus(ContractStatus.NEW);
        
        // generate arka wallet for deposits
        String depositArkaAddressPassphrase = identifierGenerator.generate();

        String depositArkaAddress = arkaClient.getAddress(depositArkaAddressPassphrase);
        contractEntity.setDepositArkaAddress(depositArkaAddress);
        contractEntity.setDepositArkaAddressPassphrase(depositArkaAddressPassphrase);

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

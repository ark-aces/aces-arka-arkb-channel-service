package com.arkaces.arka_arkb_channel_service.contract;

import com.arkaces.aces_server.aces_service.contract.CreateContractRequest;
import lombok.RequiredArgsConstructor;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import com.arkaces.aces_server.common.error.ValidatorException;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CreateContractRequestValidator {

    private final ContractRepository contractRepository;

    public void validate(CreateContractRequest<Arguments> createContractRequest) {
        BindingResult bindingResult = new BeanPropertyBindingResult(createContractRequest, "createContractRequest");

        String correlationId = createContractRequest.getCorrelationId();
        if (! StringUtils.isEmpty(correlationId)) {
            ContractEntity contractEntity = contractRepository.findOneByCorrelationId(correlationId);
            if (contractEntity != null) {
                bindingResult.rejectValue("correlationId", FieldErrorCodes.DUPLICATE_CORRELATION_ID,
                    "A contract with the given correlation ID already exists.");
            }
        }

        String recipientArkAddress = createContractRequest.getArguments().getRecipientArkbAddress();
        if (StringUtils.isEmpty(recipientArkAddress)) {
            bindingResult.rejectValue("arguments.recipientArkbAddress", FieldErrorCodes.REQUIRED, "Recipient address required.");
        } else {
            try {
                Base58.decodeChecked(recipientArkAddress);
            } catch (AddressFormatException exception) {
                if (exception.getMessage().equals("Checksum does not validate")) {
                    bindingResult.rejectValue(
                        "arguments.recipientArkbAddress",
                        FieldErrorCodes.INVALID_ARK_ADDRESS_CHECKSUM,
                        "Invalid address checksum."
                    );
                } else {
                    bindingResult.rejectValue(
                        "arguments.recipientArkbAddress",
                        FieldErrorCodes.INVALID_ARK_ADDRESS,
                        "Invalid address."
                    );
                }

            }
        }

        if (bindingResult.hasErrors()) {
            throw new ValidatorException(bindingResult);
        }
    }

}
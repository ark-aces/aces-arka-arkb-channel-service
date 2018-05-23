package com.arkaces.arka_arkb_channel_service.contract;

import com.arkaces.arka_arkb_channel_service.transfer.TransferEntity;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "contracts")
public class ContractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pid;

    private String id;
    private String correlationId;
    private String status;
    private LocalDateTime createdAt;
    private String recipientArkbAddress;
    private String depositArkaAddress;
    private String depositArkaAddressPassphrase;
    private String subscriptionId;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "contractEntity")
    private List<TransferEntity> transferEntities = new ArrayList<>();
    
}

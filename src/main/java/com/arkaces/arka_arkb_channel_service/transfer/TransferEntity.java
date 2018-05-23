package com.arkaces.arka_arkb_channel_service.transfer;

import com.arkaces.arka_arkb_channel_service.contract.ContractEntity;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transfers")
public class TransferEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pid;

    private String id;
    private LocalDateTime createdAt;
    private String status;
    private String returnArkaAddress;
    private String arkaTransactionId;

    @Column(precision = 20, scale = 8)
    private BigDecimal arkaAmount;

    @Column(precision = 20, scale = 8)
    private BigDecimal arkbPerArka;

    @Column(precision = 20, scale = 8)
    private BigDecimal arkaFlatFee;

    @Column(precision = 20, scale = 8)
    private BigDecimal arkaPercentFee;

    @Column(precision = 20, scale = 8)
    private BigDecimal arkaTotalFee;

    @Column(precision = 20, scale = 8)
    private BigDecimal arkbSendAmount;

    private String arkbTransactionId;

    private Boolean needsArkbConfirmation;

    private String arkbConfirmationSubscriptionId;

    private Boolean needsArkaReturn;

    private String returnArkaTransactionId;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "contract_pid")
    private ContractEntity contractEntity;
}


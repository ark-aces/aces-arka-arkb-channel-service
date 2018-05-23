package com.arkaces.arka_arkb_channel_service.service_capacity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "service_capacities")
public class ServiceCapacityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pid;
    private BigDecimal availableAmount;
    private BigDecimal unsettledAmount;
    private BigDecimal totalAmount;
    private String unit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
package com.apogee.pricing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "price_audit")
@IdClass(PriceAuditId.class)
@Getter
@Setter
public class PriceAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Id
    @Column(name = "changed_at", nullable = false)
    private OffsetDateTime changedAt;

    @Column(name = "sku_id", nullable = false)
    private UUID skuId;

    @Column(name = "old_price", precision = 13, scale = 2)
    private BigDecimal oldPrice;

    @Column(name = "new_price", precision = 13, scale = 2)
    private BigDecimal newPrice;

    @Column(name = "changed_by", length = 100)
    private String changedBy;

    @Column(name = "change_reason", length = 1000)
    private String changeReason;
}


package com.apogee.pricing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tier_prices", indexes = {
        @Index(name = "idx_tier_prices_lookup", columnList = "sku_id, min_quantity")
})
@Getter
@Setter
public class TierPriceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "sku_id", nullable = false)
    private UUID skuId;

    @Column(name = "min_quantity", nullable = false)
    private Integer minQuantity;

    @Column(name = "max_quantity")
    private Integer maxQuantity;

    @Column(name = "unit_price", precision = 13, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}


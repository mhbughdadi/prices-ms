package com.apogee.pricing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "region_prices", indexes = {
        @Index(name = "idx_region_prices_lookup", columnList = "sku_id, country_code")
})
@Getter
@Setter
public class RegionPriceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "sku_id", nullable = false)
    private UUID skuId;

    @Column(name = "country_code", length = 2, nullable = false)
    private String countryCode;

    @Column(name = "region_code", length = 50)
    private String regionCode;

    @Column(name = "price", precision = 13, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}


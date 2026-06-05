package com.apogee.pricing.entity;

import com.apogee.pricing.entity.enums.TaxType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tax_rules", indexes = {
        @Index(name = "idx_tax_rules_country", columnList = "country_code")
})
@Getter
@Setter
public class TaxRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "country_code", length = 2, nullable = false)
    private String countryCode;

    @Column(name = "tax_name", length = 100, nullable = false)
    private String taxName;

    @Column(name = "percentage", precision = 5, scale = 2, nullable = false)
    private BigDecimal percentage;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_mode", nullable = false)
    private TaxType taxMode;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}


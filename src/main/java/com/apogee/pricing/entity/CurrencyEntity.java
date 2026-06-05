package com.apogee.pricing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "currencies", indexes = {
        @Index(name = "idx_currency_code", columnList = "code")
})
@Getter
@Setter
public class CurrencyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "code", length = 3, nullable = false, unique = true)
    private String code;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "symbol", length = 10)
    private String symbol;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "sourceCurrency", fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    private List<ExchangeRateEntity> sourceExchangeRates = new ArrayList<>();

    @OneToMany(mappedBy = "targetCurrency", fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    private List<ExchangeRateEntity> targetExchangeRates = new ArrayList<>();

    @OneToMany(mappedBy = "currency", fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    private List<PriceListEntity> priceLists = new ArrayList<>();
}



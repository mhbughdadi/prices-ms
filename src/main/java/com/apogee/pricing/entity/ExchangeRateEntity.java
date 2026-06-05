package com.apogee.pricing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "exchange_rates", indexes = {
        @Index(name = "idx_exchange_rates_lookup", columnList = "source_currency_id, target_currency_id")
})
@Getter
@Setter
public class ExchangeRateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_currency_id", nullable = false)
    @BatchSize(size = 50)
    private CurrencyEntity sourceCurrency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_currency_id", nullable = false)
    @BatchSize(size = 50)
    private CurrencyEntity targetCurrency;

    @Column(name = "rate", precision = 18, scale = 8, nullable = false)
    private BigDecimal rate;

    @Column(name = "effective_date", nullable = false)
    private OffsetDateTime effectiveDate;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}


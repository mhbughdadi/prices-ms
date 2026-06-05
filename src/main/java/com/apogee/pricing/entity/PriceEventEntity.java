package com.apogee.pricing.entity;

import com.apogee.pricing.entity.enums.PriceEventType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "price_events", indexes = {
        @Index(name = "idx_price_events_lookup", columnList = "sku_id, start_date, end_date")
})
@Getter
@Setter
public class PriceEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "sku_id", nullable = false)
    private UUID skuId;

    @Column(name = "event_name", length = 255, nullable = false)
    private String eventName;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private PriceEventType eventType;

    @Column(name = "future_price", precision = 13, scale = 2, nullable = false)
    private BigDecimal futurePrice;

    @Column(name = "priority", nullable = false)
    private Integer priority = 100;

    @Column(name = "start_date", nullable = false)
    private OffsetDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private OffsetDateTime endDate;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}


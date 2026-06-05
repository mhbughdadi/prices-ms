package com.apogee.pricing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLDelete;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "sku_prices", indexes = {
        @Index(name = "idx_sku_prices_sku", columnList = "sku_id"),
        @Index(name = "idx_sku_prices_lookup", columnList = "sku_id, active, start_date, end_date"),
        @Index(name = "idx_sku_prices_pricelist", columnList = "price_list_id")
})
@Getter
@Setter
@SQLDelete(sql = "UPDATE sku_prices SET deleted = TRUE WHERE id = ?")
public class SkuPriceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "sku_id", nullable = false)
    private UUID skuId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_list_id", nullable = false)
    @BatchSize(size = 50)
    private PriceListEntity priceList;

    @Column(name = "regular_price", precision = 13, scale = 2, nullable = false)
    private BigDecimal regularPrice;

    @Column(name = "sale_price", precision = 13, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "cost_price", precision = 13, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "start_date")
    private OffsetDateTime startDate;

    @Column(name = "end_date")
    private OffsetDateTime endDate;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
}


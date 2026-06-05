package com.apogee.pricing.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class SkuPriceDto {
    public UUID id;
    public UUID skuId;
    public BigDecimal regularPrice;
    public BigDecimal salePrice;
    public BigDecimal costPrice;
}


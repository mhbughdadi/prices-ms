package com.apogee.pricing.dto;

import java.util.List;
import java.util.UUID;

public class PriceListDto {
    public UUID id;
    public String code;
    public String name;
    public List<SkuPriceDto> skuPrices;
}


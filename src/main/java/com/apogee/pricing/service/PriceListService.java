package com.apogee.pricing.service;

import com.apogee.pricing.dto.PriceListDto;
import com.apogee.pricing.dto.SkuPriceDto;
import com.apogee.pricing.entity.PriceListEntity;
import com.apogee.pricing.entity.SkuPriceEntity;
import com.apogee.pricing.repository.PriceListRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PriceListService {

    private final PriceListRepository priceListRepository;

    public PriceListService(PriceListRepository priceListRepository) {
        this.priceListRepository = priceListRepository;
    }

    /**
     * Example method that loads a PriceList with skuPrices using @EntityGraph to avoid N+1
     */
    public Optional<PriceListDto> getPriceListWithSkuPrices(UUID id) {
        return priceListRepository.findByIdAndNotDeletedWithSkuPrices(id)
                .map(this::toDto);
    }

    private PriceListDto toDto(PriceListEntity p) {
        PriceListDto dto = new PriceListDto();
        dto.id = p.getId();
        dto.code = p.getCode();
        dto.name = p.getName();
        dto.skuPrices = p.getSkuPrices().stream().map(this::skuToDto).collect(Collectors.toList());
        return dto;
    }

    private SkuPriceDto skuToDto(SkuPriceEntity s) {
        SkuPriceDto d = new SkuPriceDto();
        d.id = s.getId();
        d.skuId = s.getSkuId();
        d.regularPrice = s.getRegularPrice();
        d.salePrice = s.getSalePrice();
        d.costPrice = s.getCostPrice();
        return d;
    }
}


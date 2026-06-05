package com.apogee.pricing.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apogee.pricing.dto.PriceListDto;
import com.apogee.pricing.service.PriceListService;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class PricingController {

    private final PriceListService priceListService;

    public PricingController(PriceListService priceListService) {
        this.priceListService = priceListService;
    }

    @GetMapping("/price")
    public ResponseEntity<String> price() {
        return new ResponseEntity<String>("Hello World", HttpStatus.OK);
    }

    @GetMapping("/pricelists/{id}")
    public ResponseEntity<PriceListDto> getPriceList(@PathVariable("id") UUID id) {
        Optional<PriceListDto> dto = priceListService.getPriceListWithSkuPrices(id);
        return dto.map(d -> new ResponseEntity<>(d, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}

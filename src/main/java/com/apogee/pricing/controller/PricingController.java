package com.apogee.pricing.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PricingController {

    @GetMapping("/price")
    public ResponseEntity<String> price() {
        return new ResponseEntity<String>("Hello World", HttpStatus.OK);
    }
}

package com.apogee.pricing.filter;

import com.apogee.pricing.constant.PricingConstant;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

import static com.apogee.spring.common.constant.CommonConstant.REQUEST_ID;
import static com.apogee.spring.common.constant.CommonConstant.SERVICE_ID;
import static com.apogee.spring.common.constant.CommonConstant.X_REQUEST_ID;


@Component
public class CorrelationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestID = UUID.randomUUID().toString();

        MDC.put(REQUEST_ID, requestID);
        MDC.put(SERVICE_ID, PricingConstant.SERVICE_NAME); // Add service name to MDC for logging
        response.setHeader(X_REQUEST_ID, requestID);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}

package com.shopsphere.dto;
import java.math.BigDecimal;
public record OrderConfirmation(Long orderId, BigDecimal total, String accessToken) {}

package com.dragons.application.coupon.dto;

public record CouponStockResult(
    Long couponId,
    Integer remainingQuantity
) {
}

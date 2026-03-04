package com.dragons.interfaces.api.coupon.dto;

public record CouponStockResult(
    Long couponId,
    Integer remainingQuantity
) {
}

package com.dragons.interfaces.api.coupon.dto;

public record CouponIssueCommand(
    Long couponId,
    Long userId
) {
}

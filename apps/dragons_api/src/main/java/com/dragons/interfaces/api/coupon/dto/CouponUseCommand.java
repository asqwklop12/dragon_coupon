package com.dragons.interfaces.api.coupon.dto;

public record CouponUseCommand(
    Long issuedCouponId,
    Long orderId,
    Integer orderAmount
) {
  public CouponUseCommand {
    if (issuedCouponId == null || issuedCouponId <= 0) {
      throw new IllegalArgumentException("issuedCouponId must be positive");
    }
    if (orderId == null || orderId <= 0) {
      throw new IllegalArgumentException("orderId must be positive");
    }
    if (orderAmount == null || orderAmount < 0) {
      throw new IllegalArgumentException("orderAmount must be zero or positive");
    }
  }
}

package com.dragons.application.coupon.dto;

public record CouponIssueCommand(
    Long couponId,
    Long userId,
    String issueRequestId
) {
  public CouponIssueCommand(Long couponId, Long userId) {
    this(couponId, userId, null);
  }
}

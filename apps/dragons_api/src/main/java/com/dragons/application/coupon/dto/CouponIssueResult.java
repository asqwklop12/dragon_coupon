package com.dragons.application.coupon.dto;

import com.dragons.domain.coupon.IssuedCouponStatus;
import java.time.LocalDateTime;

public record CouponIssueResult(
    Long issuedCouponId,
    Long couponId,
    Long userId,
    IssuedCouponStatus status,
    LocalDateTime issuedAt,
    LocalDateTime expiredAt
) {
}

package com.dragons.application.coupon.dto;

import com.dragons.domain.coupon.IssuedCouponStatus;
import java.time.ZonedDateTime;

public record CouponIssueResult(
    Long issuedCouponId,
    Long couponId,
    Long userId,
    IssuedCouponStatus status,
    ZonedDateTime issuedAt,
    ZonedDateTime expiredAt
) {
}

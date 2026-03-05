package com.dragons.application.coupon.dto;

import com.dragons.domain.coupon.IssuedCouponStatus;
import java.time.ZonedDateTime;

public record CouponUseResult(
    Long issuedCouponId,
    Long orderId,
    Integer discountAmount,
    IssuedCouponStatus status,
    ZonedDateTime usedAt
) {
}

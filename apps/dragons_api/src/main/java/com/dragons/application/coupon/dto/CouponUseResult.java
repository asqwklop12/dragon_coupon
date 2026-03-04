package com.dragons.application.coupon.dto;

import com.dragons.domain.coupon.IssuedCouponStatus;
import java.time.LocalDateTime;

public record CouponUseResult(
    Long issuedCouponId,
    Long orderId,
    Integer discountAmount,
    IssuedCouponStatus status,
    LocalDateTime usedAt
) {
}

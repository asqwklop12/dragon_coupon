package com.dragons.application.coupon.dto;

import com.dragons.domain.coupon.CouponStatus;
import com.dragons.domain.coupon.CouponType;
import java.time.ZonedDateTime;

public record CouponCreateResult(
    Long couponId,
    String name,
    String description,
    CouponType couponType,
    CouponStatus status,
    Integer discountValue,
    Integer minOrderAmount,
    Integer maxDiscountAmount,
    Integer totalQuantity,
    Integer issuedQuantity,
    Integer validDays,
    ZonedDateTime startDate,
    ZonedDateTime endDate
) {
}

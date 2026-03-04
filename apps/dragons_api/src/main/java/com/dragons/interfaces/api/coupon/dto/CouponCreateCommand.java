package com.dragons.interfaces.api.coupon.dto;

import com.dragons.domain.coupon.CouponStatus;
import com.dragons.domain.coupon.CouponType;
import java.time.OffsetDateTime;

public record CouponCreateCommand(
    String name,
    String description,
    CouponType couponType,
    CouponStatus status,
    Integer discountValue,
    Integer minOrderAmount,
    Integer maxDiscountAmount,
    Integer totalQuantity,
    Integer validDays,
    OffsetDateTime startDate,
    OffsetDateTime endDate
) {
  public CouponCreateCommand {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("name must not be blank");
    }
    if (description == null || description.isBlank()) {
      throw new IllegalArgumentException("description must not be blank");
    }
    if (couponType == null) {
      throw new IllegalArgumentException("couponType must not be null");
    }
    if (status == null) {
      throw new IllegalArgumentException("status must not be null");
    }
    if (status != CouponStatus.ACTIVE && status != CouponStatus.INACTIVE) {
      throw new IllegalArgumentException("status must be ACTIVE or INACTIVE");
    }
    if (discountValue == null || discountValue <= 0) {
      throw new IllegalArgumentException("discountValue must be positive");
    }
    if (couponType == CouponType.PERCENTAGE && discountValue > 100) {
      throw new IllegalArgumentException("percentage discountValue must be less than or equal to 100");
    }
    if (minOrderAmount != null && minOrderAmount <= 0) {
      throw new IllegalArgumentException("minOrderAmount must be positive");
    }
    if (maxDiscountAmount != null && maxDiscountAmount <= 0) {
      throw new IllegalArgumentException("maxDiscountAmount must be positive");
    }
    if (totalQuantity == null || totalQuantity <= 0) {
      throw new IllegalArgumentException("totalQuantity must be positive");
    }
    if (validDays == null || validDays <= 0) {
      throw new IllegalArgumentException("validDays must be positive");
    }
    if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("startDate and endDate must not be null");
    }
    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("startDate must not be after endDate");
    }
  }
}

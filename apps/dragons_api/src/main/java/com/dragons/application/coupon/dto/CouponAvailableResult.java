package com.dragons.application.coupon.dto;

import com.dragons.domain.coupon.CouponType;
import java.time.ZonedDateTime;
import java.util.List;

public record CouponAvailableResult(List<CouponItem> coupons) {

  public record CouponItem(
      Long couponId,
      String name,
      String description,
      CouponType couponType,
      Integer discountValue,
      Integer minOrderAmount,
      Integer maxDiscountAmount,
      Integer remainingQuantity,
      ZonedDateTime startDate,
      ZonedDateTime endDate
  ) {
  }
}

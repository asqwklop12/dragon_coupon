package com.dragons.interfaces.api.coupon.dto;

import com.dragons.domain.coupon.IssuedCouponStatus;
import java.time.OffsetDateTime;
import java.util.List;

public record CouponUserCouponsResult(List<CouponItem> coupons) {
  public CouponUserCouponsResult {
    coupons = coupons == null ? List.of() : List.copyOf(coupons);
  }
  public record CouponItem(
      Long issuedCouponId,
      Long couponId,
      String couponName,
      IssuedCouponStatus status,
      OffsetDateTime issuedAt,
      OffsetDateTime expiredAt,
      OffsetDateTime usedAt
  ) {
  }
}

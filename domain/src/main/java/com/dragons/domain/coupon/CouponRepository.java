package com.dragons.domain.coupon;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface CouponRepository {
  Coupon store(Coupon coupon);

  List<Coupon> readIssuableCoupons(ZonedDateTime now);

  Optional<Coupon> readCoupon(Long couponId);

  Optional<Coupon> readCouponForUpdate(Long couponId);

  Optional<Coupon> readActiveCoupon(Long couponId);
}

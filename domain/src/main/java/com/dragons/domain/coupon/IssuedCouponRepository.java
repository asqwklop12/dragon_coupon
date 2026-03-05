package com.dragons.domain.coupon;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface IssuedCouponRepository {
  IssuedCoupon store(IssuedCoupon issuedCoupon);

  Optional<IssuedCoupon> readIssuedCoupon(Long issuedCouponId);

  boolean existsByCouponIdAndUserId(Long couponId, Long userId);

  List<IssuedCoupon> readUserCoupons(Long userId);

  List<IssuedCoupon> readUsableUserCoupons(Long userId, ZonedDateTime now);
}

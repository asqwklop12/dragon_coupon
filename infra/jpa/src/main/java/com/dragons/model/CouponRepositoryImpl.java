package com.dragons.model;

import com.dragons.domain.coupon.Coupon;
import com.dragons.domain.coupon.CouponRepository;
import com.dragons.domain.coupon.CouponStatus;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class CouponRepositoryImpl implements CouponRepository {
  private final JpaCouponRepository jpaCouponRepository;

  @Override
  public Coupon store(Coupon coupon) {
    return jpaCouponRepository.save(coupon);
  }

  @Override
  public List<Coupon> readIssuableCoupons(ZonedDateTime now) {
    return jpaCouponRepository.findAllIssuable(now, CouponStatus.ACTIVE);
  }

  @Override
  public Optional<Coupon> readCoupon(Long couponId) {
    return jpaCouponRepository.findById(couponId);
  }

  @Override
  public Optional<Coupon> readActiveCoupon(Long couponId) {
    return jpaCouponRepository.findByIdAndStatus(couponId, CouponStatus.ACTIVE);
  }
}

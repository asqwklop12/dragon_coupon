package com.dragons.model;

import com.dragons.domain.coupon.IssuedCoupon;
import com.dragons.domain.coupon.IssuedCouponRepository;
import com.dragons.domain.coupon.IssuedCouponStatus;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class IssuedCouponRepositoryImpl implements IssuedCouponRepository {
  private final JpaIssuedCouponRepository jpaIssuedCouponRepository;

  @Override
  public IssuedCoupon store(IssuedCoupon issuedCoupon) {
    return jpaIssuedCouponRepository.save(issuedCoupon);
  }

  @Override
  public Optional<IssuedCoupon> readIssuedCoupon(Long issuedCouponId) {
    return jpaIssuedCouponRepository.findById(issuedCouponId);
  }

  @Override
  public boolean existsByCouponIdAndUserId(Long couponId, Long userId) {
    return jpaIssuedCouponRepository.existsByCoupon_IdAndUserId(couponId, userId);
  }

  @Override
  public List<IssuedCoupon> readUserCoupons(Long userId) {
    return jpaIssuedCouponRepository.findAllByUserIdOrderByIssuedAtDesc(userId);
  }

  @Override
  public List<IssuedCoupon> readUsableUserCoupons(Long userId, ZonedDateTime now) {
    return jpaIssuedCouponRepository.findAllByUserIdAndStatusAndExpiredAtGreaterThanEqualOrderByIssuedAtDesc(
        userId,
        IssuedCouponStatus.ISSUED,
        now
    );
  }
}

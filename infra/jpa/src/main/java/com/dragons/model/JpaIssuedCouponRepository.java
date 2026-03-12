package com.dragons.model;

import com.dragons.domain.coupon.IssuedCoupon;
import com.dragons.domain.coupon.IssuedCouponStatus;
import java.time.ZonedDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

interface JpaIssuedCouponRepository extends JpaRepository<IssuedCoupon, Long> {
  boolean existsByCoupon_IdAndUserId(Long couponId, Long userId);

  long countByCoupon_Id(Long couponId);

  @EntityGraph(attributePaths = "coupon")
  List<IssuedCoupon> findAllByUserIdOrderByIssuedAtDesc(Long userId);

  @EntityGraph(attributePaths = "coupon")
  List<IssuedCoupon> findAllByUserIdAndStatusAndExpiredAtGreaterThanEqualOrderByIssuedAtDesc(
      Long userId,
      IssuedCouponStatus status,
      ZonedDateTime now
  );
}

package com.dragons.model;

import com.dragons.domain.coupon.Coupon;
import com.dragons.domain.coupon.CouponStatus;
import jakarta.persistence.LockModeType;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface JpaCouponRepository extends JpaRepository<Coupon, Long> {
  @Query("""
      SELECT c
      FROM Coupon c
      WHERE c.status = :status
      AND c.startDate <= :now
      AND c.endDate >= :now
      """)
  List<Coupon> findAllIssuable(
      @Param("now") ZonedDateTime now,
      @Param("status") CouponStatus status
  );

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
      SELECT c
      FROM Coupon c
      WHERE c.id = :couponId
      """)
  Optional<Coupon> findByIdForUpdate(@Param("couponId") Long couponId);

  Optional<Coupon> findByIdAndStatus(Long couponId, CouponStatus status);
}

package com.dragons.model;

import com.dragons.domain.coupon.CouponUsageHistory;
import com.dragons.domain.coupon.CouponUsageHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class CouponUsageHistoryRepositoryImpl implements CouponUsageHistoryRepository {
  private final JpaCouponUsageHistoryRepository jpaCouponUsageHistoryRepository;

  @Override
  public CouponUsageHistory add(CouponUsageHistory couponUsageHistory) {
    return jpaCouponUsageHistoryRepository.save(couponUsageHistory);
  }
}

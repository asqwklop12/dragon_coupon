package com.dragons.model;

import com.dragons.domain.coupon.CouponUsageHistory;
import org.springframework.data.jpa.repository.JpaRepository;

interface JpaCouponUsageHistoryRepository extends JpaRepository<CouponUsageHistory, Long> {
}

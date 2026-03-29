package com.dragons.domain.coupon;

public interface CouponStockRepository {
  void initializeStockIfAbsent(Long couponId, int stock);

  boolean decreaseStock(Long couponId);

  void increaseStock(Long couponId);

  void clearStock(Long couponId);

  Integer readStock(Long couponId);
}

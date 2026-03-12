package com.dragons.model;

import com.dragons.config.RedisConfig;
import com.dragons.domain.coupon.CouponStockRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CouponStockRepositoryImpl implements CouponStockRepository {
  private final RedisTemplate<String, String> redisTemplate;

  public CouponStockRepositoryImpl(
      @Qualifier(RedisConfig.REDIS_TEMPLATE_MASTER) RedisTemplate<String, String> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public void initializeStockIfAbsent(Long couponId, int stock) {
    redisTemplate.opsForValue().setIfAbsent(stockKey(couponId), Integer.toString(stock));
  }

  @Override
  public boolean decreaseStock(Long couponId) {
    Long decreased = redisTemplate.opsForValue().decrement(stockKey(couponId));
    if (decreased == null) {
      return false;
    }
    if (decreased >= 0) {
      return true;
    }

    redisTemplate.opsForValue().increment(stockKey(couponId));
    return false;
  }

  @Override
  public void increaseStock(Long couponId) {
    redisTemplate.opsForValue().increment(stockKey(couponId));
  }

  @Override
  public Integer readStock(Long couponId) {
    String value = redisTemplate.opsForValue().get(stockKey(couponId));
    return value == null ? null : Integer.valueOf(value);
  }

  private String stockKey(Long couponId) {
    return "coupon:stock:" + couponId;
  }
}

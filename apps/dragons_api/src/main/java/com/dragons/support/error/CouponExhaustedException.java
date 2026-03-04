package com.dragons.support.error;

public class CouponExhaustedException extends RuntimeException {
  public CouponExhaustedException() {
    super("재고가 소진되었습니다.");
  }
}

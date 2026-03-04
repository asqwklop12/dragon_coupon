package com.dragons.support.error;

public class CouponNotAvailableException extends RuntimeException {
  public CouponNotAvailableException() {
    super("발급 기간이 아닙니다.");
  }

}

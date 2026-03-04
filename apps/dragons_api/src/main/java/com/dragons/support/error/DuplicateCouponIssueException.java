package com.dragons.support.error;

public class DuplicateCouponIssueException extends RuntimeException {
  public DuplicateCouponIssueException() {
    super("중복 발급 시도가 있었습니다.");
  }

}

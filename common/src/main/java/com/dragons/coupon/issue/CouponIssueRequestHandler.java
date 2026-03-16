package com.dragons.coupon.issue;

public interface CouponIssueRequestHandler {

  void handle(CouponIssueRequestedEvent event);
}

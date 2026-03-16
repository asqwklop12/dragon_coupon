package com.dragons.coupon.issue;

public interface CouponIssueRequestProducer {

  void send(CouponIssueRequestedEvent event);
}

package com.dragons.coupon.issue;

public interface CouponIssueRequestProducer {

  void producer(CouponIssueRequestedEvent event);
}

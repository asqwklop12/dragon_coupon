package com.dragons.consumer;

import com.dragons.coupon.issue.CouponIssueRequestHandler;
import com.dragons.coupon.issue.CouponIssueRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponIssueRequestedEventConsumer {

  private static final String COUPON_ISSUE_REQUEST_TOPIC = "coupon-issue-requests-v3";

  private final CouponIssueRequestHandler couponIssueRequestHandler;

  @KafkaListener(topics = COUPON_ISSUE_REQUEST_TOPIC,
      groupId = "coupon-issue-consumer-group",
      concurrency = "3")
  public void handle(CouponIssueRequestedEvent event) {
    couponIssueRequestHandler.handle(event);
  }
}

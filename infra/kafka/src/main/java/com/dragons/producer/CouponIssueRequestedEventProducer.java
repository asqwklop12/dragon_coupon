package com.dragons.producer;

import com.dragons.coupon.issue.CouponIssueRequestProducer;
import com.dragons.coupon.issue.CouponIssueRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponIssueRequestedEventProducer implements CouponIssueRequestProducer {

  private static final String COUPON_ISSUE_REQUEST_TOPIC = "coupon-issue-requests";

  private final KafkaTemplate<Object, Object> kafkaTemplate;

  @Override
  public void producer(CouponIssueRequestedEvent event) {
    kafkaTemplate.send(COUPON_ISSUE_REQUEST_TOPIC, String.valueOf(event.couponId()), event);
  }
}

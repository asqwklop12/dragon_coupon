package com.dragons.consumer;

import com.dragons.coupon.issue.CouponIssueRequestHandler;
import com.dragons.coupon.issue.CouponIssueRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponIssueRequestedEventConsumer {

  private static final String COUPON_ISSUE_REQUEST_TOPIC = "coupon-issue-requests-v3";

  private final CouponIssueRequestHandler couponIssueRequestHandler;

  @RetryableTopic(
      attempts = "3",
      kafkaTemplate =  "kafkaAtLeastTemplate",
      backOff = @BackOff(delay = 1000, multiplier = 2.0),
      dltTopicSuffix = "-dlt"
  )
  @KafkaListener(topics = COUPON_ISSUE_REQUEST_TOPIC,
      groupId = "coupon-issue-consumer-group",
      concurrency = "7")
  public void handle(CouponIssueRequestedEvent event) {
    couponIssueRequestHandler.handle(event);
  }

  @DltHandler
  public void dltHandle(CouponIssueRequestedEvent event) {
    log.error("DLT 이동 eventId={}", event.eventId());
  }
}

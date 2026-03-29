package com.dragons.producer;

import com.dragons.coupon.issue.CouponIssueRequestProducer;
import com.dragons.coupon.issue.CouponIssueRequestedEvent;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CouponIssueRequestedEventProducer implements CouponIssueRequestProducer {

  private static final String COUPON_ISSUE_REQUEST_TOPIC = "coupon-issue-requests-v3";

  private final KafkaTemplate<Object, Object> kafkaTemplate;

  public CouponIssueRequestedEventProducer(
      @Qualifier("kafkaAtLeastTemplate") KafkaTemplate<Object, Object> kafkaTemplate
  ) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @Override
  public void send(CouponIssueRequestedEvent event) {
    try {
      kafkaTemplate.send(COUPON_ISSUE_REQUEST_TOPIC, String.valueOf(event.userId()), event).get();
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      log.error(
          "Kafka publish interrupted. topic={}, eventId={}, couponId={}, userId={}, errorType={}, errorMessage={}",
          COUPON_ISSUE_REQUEST_TOPIC,
          event.eventId(),
          event.couponId(),
          event.userId(),
          exception.getClass().getName(),
          exception.getMessage(),
          exception
      );
      throw new IllegalStateException("Interrupted while publishing coupon issue request event.", exception);
    } catch (ExecutionException exception) {
      Throwable cause = exception.getCause() == null ? exception : exception.getCause();
      log.error(
          "Kafka publish failed. topic={}, eventId={}, couponId={}, userId={}, errorType={}, errorMessage={}",
          COUPON_ISSUE_REQUEST_TOPIC,
          event.eventId(),
          event.couponId(),
          event.userId(),
          cause.getClass().getName(),
          cause.getMessage(),
          exception
      );
      throw new IllegalStateException("Failed to publish coupon issue request event.", exception);
    }
  }
}

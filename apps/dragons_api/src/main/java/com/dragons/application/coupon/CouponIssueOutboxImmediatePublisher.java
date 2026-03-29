package com.dragons.application.coupon;

import com.dragons.coupon.issue.CouponIssueRequestProducer;
import com.dragons.coupon.issue.CouponIssueRequestedEvent;
import com.dragons.domain.outbox.OutboxEvent;
import com.dragons.domain.outbox.OutboxEventRepository;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponIssueOutboxImmediatePublisher {

  private final CouponIssueRequestProducer couponIssueRequestProducer;
  private final OutboxEventRepository outboxEventRepository;

  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void publish(OutboxEvent outboxEvent, CouponIssueRequestedEvent event) {
    try {
      couponIssueRequestProducer.send(event);
      outboxEvent.markSent(ZonedDateTime.now());
      outboxEventRepository.store(outboxEvent);
    } catch (Exception exception) {
      outboxEvent.markFailed();
      outboxEventRepository.store(outboxEvent);
      log.error(
          "Failed to immediately publish coupon issue outbox event. eventId={}, retryCount={}, errorType={}, errorMessage={}",
          outboxEvent.getEventId(),
          outboxEvent.getRetryCount(),
          exception.getClass().getName(),
          exception.getMessage(),
          exception
      );
    }
  }
}

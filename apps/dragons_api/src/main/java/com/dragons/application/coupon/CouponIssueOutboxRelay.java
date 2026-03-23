package com.dragons.application.coupon;

import com.dragons.coupon.issue.CouponIssueRequestProducer;
import com.dragons.coupon.issue.CouponIssueRequestedEvent;
import com.dragons.domain.outbox.OutboxEvent;
import com.dragons.domain.outbox.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponIssueOutboxRelay {

  private static final int BATCH_SIZE = 100;

  private final OutboxEventRepository outboxEventRepository;
  private final CouponIssueRequestProducer couponIssueRequestProducer;
  private final ObjectMapper objectMapper;

  @Transactional
  @Scheduled(fixedDelayString = "${coupon.issue.outbox.retry-delay-ms:1000}")
  public void relay() {
    List<OutboxEvent> retryTargets = outboxEventRepository.readRetryTargets(BATCH_SIZE);

    for (OutboxEvent outboxEvent : retryTargets) {
      try {
        CouponIssueRequestedEvent event = objectMapper.readValue(outboxEvent.getPayload(), CouponIssueRequestedEvent.class);
        couponIssueRequestProducer.send(event);
        outboxEvent.markSent(ZonedDateTime.now());
      } catch (Exception exception) {
        outboxEvent.markFailed();
        log.error(
            "Failed to relay outbox event. eventId={}, retryCount={}, errorType={}, errorMessage={}",
            outboxEvent.getEventId(),
            outboxEvent.getRetryCount(),
            exception.getClass().getName(),
            exception.getMessage(),
            exception
        );
      }
    }
  }
}

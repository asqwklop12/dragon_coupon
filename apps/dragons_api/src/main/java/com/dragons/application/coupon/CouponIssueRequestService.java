package com.dragons.application.coupon;

import com.dragons.application.coupon.dto.CouponIssueCommand;
import com.dragons.coupon.issue.CouponIssueRequestedEvent;
import com.dragons.domain.outbox.OutboxEvent;
import com.dragons.domain.outbox.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponIssueRequestService {

  private static final String COUPON_ISSUE_REQUEST_TOPIC = "coupon-issue-requests-v3";

  private final OutboxEventRepository outboxEventRepository;
  private final CouponIssueOutboxImmediatePublisher couponIssueOutboxImmediatePublisher;
  private final ObjectMapper objectMapper;

  @Transactional
  public CouponIssueRequestedEvent requestIssue(CouponIssueCommand command) {
    ZonedDateTime requestedAt = ZonedDateTime.now();
    CouponIssueRequestedEvent event = new CouponIssueRequestedEvent(
        command.couponId(),
        command.userId(),
        requestedAt,
        UUID.randomUUID().toString()
    );

    OutboxEvent outboxEvent = outboxEventRepository.store(OutboxEvent.create(
        event.eventId(),
        COUPON_ISSUE_REQUEST_TOPIC,
        String.valueOf(event.userId()),
        serialize(event),
        requestedAt
    ));

    couponIssueOutboxImmediatePublisher.publish(outboxEvent, event);
    return event;
  }

  private String serialize(CouponIssueRequestedEvent event) {
    try {
      return objectMapper.writeValueAsString(event);
    } catch (JsonProcessingException exception) {
      log.error(
          "Failed to serialize coupon issue request event. eventId={}, couponId={}, userId={}, errorType={}, errorMessage={}",
          event.eventId(),
          event.couponId(),
          event.userId(),
          exception.getClass().getName(),
          exception.getMessage(),
          exception
      );
      throw new IllegalStateException("Failed to serialize coupon issue request event.", exception);
    }
  }
}

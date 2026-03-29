package com.dragons.application.coupon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.dragons.coupon.issue.CouponIssueRequestProducer;
import com.dragons.coupon.issue.CouponIssueRequestedEvent;
import com.dragons.domain.outbox.OutboxEvent;
import com.dragons.domain.outbox.OutboxEventRepository;
import com.dragons.domain.outbox.OutboxEventStatus;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CouponIssueOutboxImmediatePublisherTest {

  @Mock
  private CouponIssueRequestProducer couponIssueRequestProducer;

  @Mock
  private OutboxEventRepository outboxEventRepository;

  @InjectMocks
  private CouponIssueOutboxImmediatePublisher couponIssueOutboxImmediatePublisher;

  @Test
  void publish_marksSent_whenPublishSucceeds() {
    OutboxEvent outboxEvent = OutboxEvent.create("evt-1", "coupon-issue-requests-v3", "101", "{}", ZonedDateTime.now());
    CouponIssueRequestedEvent event = new CouponIssueRequestedEvent(1L, 101L, ZonedDateTime.now(), "evt-1");

    couponIssueOutboxImmediatePublisher.publish(outboxEvent, event);

    verify(couponIssueRequestProducer).send(event);
    verify(outboxEventRepository).store(outboxEvent);
    assertThat(outboxEvent.getStatus()).isEqualTo(OutboxEventStatus.SENT);
    assertThat(outboxEvent.getPublishedAt()).isNotNull();
  }

  @Test
  void publish_marksFailed_whenPublishFails() {
    OutboxEvent outboxEvent = OutboxEvent.create("evt-1", "coupon-issue-requests-v3", "101", "{}", ZonedDateTime.now());
    CouponIssueRequestedEvent event = new CouponIssueRequestedEvent(1L, 101L, ZonedDateTime.now(), "evt-1");
    doThrow(new IllegalStateException("kafka down")).when(couponIssueRequestProducer).send(event);

    couponIssueOutboxImmediatePublisher.publish(outboxEvent, event);

    verify(outboxEventRepository).store(outboxEvent);
    assertThat(outboxEvent.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
    assertThat(outboxEvent.getRetryCount()).isEqualTo(1);
  }
}

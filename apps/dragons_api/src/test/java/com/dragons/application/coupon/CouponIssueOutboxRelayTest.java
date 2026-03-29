package com.dragons.application.coupon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dragons.coupon.issue.CouponIssueRequestProducer;
import com.dragons.coupon.issue.CouponIssueRequestedEvent;
import com.dragons.domain.outbox.OutboxEvent;
import com.dragons.domain.outbox.OutboxEventRepository;
import com.dragons.domain.outbox.OutboxEventStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CouponIssueOutboxRelayTest {

  @Mock
  private OutboxEventRepository outboxEventRepository;

  @Mock
  private CouponIssueRequestProducer couponIssueRequestProducer;

  @Mock
  private ObjectMapper objectMapper;

  @InjectMocks
  private CouponIssueOutboxRelay couponIssueOutboxRelay;

  @Test
  void relay_marksSent_whenPublishSucceeds() throws Exception {
    OutboxEvent outboxEvent = OutboxEvent.create("evt-1", "coupon-issue-requests-v3", "101", "{}", ZonedDateTime.now());
    CouponIssueRequestedEvent event = new CouponIssueRequestedEvent(1L, 101L, ZonedDateTime.now(), "evt-1");
    when(outboxEventRepository.readRetryTargets(any(Integer.class))).thenReturn(List.of(outboxEvent));
    when(objectMapper.readValue(outboxEvent.getPayload(), CouponIssueRequestedEvent.class)).thenReturn(event);

    couponIssueOutboxRelay.relay();

    verify(couponIssueRequestProducer).send(event);
    assertThat(outboxEvent.getStatus()).isEqualTo(OutboxEventStatus.SENT);
    assertThat(outboxEvent.getPublishedAt()).isNotNull();
  }

  @Test
  void relay_marksFailed_whenPublishFails() throws Exception {
    OutboxEvent outboxEvent = OutboxEvent.create("evt-1", "coupon-issue-requests-v3", "101", "{}", ZonedDateTime.now());
    CouponIssueRequestedEvent event = new CouponIssueRequestedEvent(1L, 101L, ZonedDateTime.now(), "evt-1");
    when(outboxEventRepository.readRetryTargets(any(Integer.class))).thenReturn(List.of(outboxEvent));
    when(objectMapper.readValue(outboxEvent.getPayload(), CouponIssueRequestedEvent.class)).thenReturn(event);
    doThrow(new IllegalStateException("kafka down")).when(couponIssueRequestProducer).send(event);

    couponIssueOutboxRelay.relay();

    assertThat(outboxEvent.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
    assertThat(outboxEvent.getRetryCount()).isEqualTo(1);
  }
}

package com.dragons.application.coupon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dragons.application.coupon.dto.CouponIssueCommand;
import com.dragons.coupon.issue.CouponIssueRequestProducer;
import com.dragons.domain.outbox.OutboxEvent;
import com.dragons.domain.outbox.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CouponIssueRequestServiceTest {

  @Mock
  private OutboxEventRepository outboxEventRepository;

  @Mock
  private ObjectMapper objectMapper;

  @Mock
  private CouponIssueRequestProducer couponIssueRequestProducer;

  @InjectMocks
  private CouponIssueRequestService couponIssueRequestService;

  @Test
  void requestIssue_storesOutboxEvent_thenPublishesEvent() throws Exception {
    when(objectMapper.writeValueAsString(any())).thenReturn("{\"eventId\":\"evt-1\"}");

    var result = couponIssueRequestService.requestIssue(new CouponIssueCommand(101L, 202L, null));

    assertThat(result.couponId()).isEqualTo(101L);
    assertThat(result.userId()).isEqualTo(202L);
    assertThat(result.requestedAt()).isNotNull();
    assertThat(result.eventId()).isNotBlank();
    verify(outboxEventRepository).store(any(OutboxEvent.class));
    verify(couponIssueRequestProducer).send(result);
  }
}

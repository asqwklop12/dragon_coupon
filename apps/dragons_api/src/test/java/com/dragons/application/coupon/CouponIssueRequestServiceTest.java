package com.dragons.application.coupon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dragons.application.coupon.dto.CouponIssueCommand;
import com.dragons.domain.outbox.OutboxEvent;
import com.dragons.domain.outbox.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@ExtendWith(MockitoExtension.class)
class CouponIssueRequestServiceTest {

  @Mock
  private OutboxEventRepository outboxEventRepository;

  @Mock
  private ObjectMapper objectMapper;

  @Mock
  private CouponIssueOutboxImmediatePublisher couponIssueOutboxImmediatePublisher;

  @InjectMocks
  private CouponIssueRequestService couponIssueRequestService;

  @AfterEach
  void tearDown() {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.clearSynchronization();
    }
  }

  @Test
  void requestIssue_registersImmediatePublishAfterCommit() throws Exception {
    when(objectMapper.writeValueAsString(any())).thenReturn("{\"eventId\":\"evt-1\"}");
    when(outboxEventRepository.store(any(OutboxEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

    TransactionSynchronizationManager.initSynchronization();

    var result = couponIssueRequestService.requestIssue(new CouponIssueCommand(101L, 202L, null));

    assertThat(result.couponId()).isEqualTo(101L);
    assertThat(result.userId()).isEqualTo(202L);
    assertThat(result.requestedAt()).isNotNull();
    assertThat(result.eventId()).isNotBlank();
    verify(outboxEventRepository).store(any(OutboxEvent.class));
    assertThat(TransactionSynchronizationManager.getSynchronizations()).hasSize(1);

    TransactionSynchronization synchronization = TransactionSynchronizationManager.getSynchronizations().get(0);
    synchronization.afterCommit();

    verify(couponIssueOutboxImmediatePublisher).publish(any(OutboxEvent.class), any());
  }
}

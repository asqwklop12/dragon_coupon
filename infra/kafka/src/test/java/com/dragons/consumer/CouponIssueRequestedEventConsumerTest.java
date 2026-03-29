package com.dragons.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.dragons.coupon.issue.CouponIssueRequestHandler;
import com.dragons.coupon.issue.CouponIssueRequestedEvent;
import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;

@ExtendWith(MockitoExtension.class)
class CouponIssueRequestedEventConsumerTest {

  @Mock
  private CouponIssueRequestHandler couponIssueRequestHandler;

  @InjectMocks
  private CouponIssueRequestedEventConsumer couponIssueRequestedEventConsumer;

  @Test
  void handle_delegatesToApplicationHandler() {
    CouponIssueRequestedEvent event = new CouponIssueRequestedEvent(101L, 202L, ZonedDateTime.now(), "evt-1");

    couponIssueRequestedEventConsumer.handle(event);

    verify(couponIssueRequestHandler).handle(event);
  }

  @Test
  void handle_hasRetryableTopicConfiguration() throws Exception {
    Method method = CouponIssueRequestedEventConsumer.class.getDeclaredMethod("handle", CouponIssueRequestedEvent.class);

    RetryableTopic retryableTopic = method.getAnnotation(RetryableTopic.class);
    KafkaListener kafkaListener = method.getAnnotation(KafkaListener.class);

    assertThat(retryableTopic).isNotNull();
    assertThat(kafkaListener).isNotNull();
    assertThat(kafkaListener.topics()).containsExactly("coupon-issue-requests-v3");
    assertThat(kafkaListener.groupId()).isEqualTo("coupon-issue-consumer-group");
    assertThat(kafkaListener.concurrency()).isEqualTo("7");
  }

  @Test
  void dltHandle_hasDltHandlerAnnotation() throws Exception {
    Method method = CouponIssueRequestedEventConsumer.class.getDeclaredMethod("dltHandle", CouponIssueRequestedEvent.class);

    DltHandler dltHandler = method.getAnnotation(DltHandler.class);

    assertThat(dltHandler).isNotNull();
  }
}

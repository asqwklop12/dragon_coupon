package com.dragons.producer;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dragons.coupon.issue.CouponIssueRequestedEvent;
import java.time.ZonedDateTime;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@ExtendWith(MockitoExtension.class)
class CouponIssueRequestedEventProducerTest {

  @Mock
  private KafkaTemplate<Object, Object> kafkaTemplate;

  @InjectMocks
  private CouponIssueRequestedEventProducer couponIssueRequestedEventProducer;

  @Test
  void publish_sendsEventToKafka() {
    CouponIssueRequestedEvent event = new CouponIssueRequestedEvent(101L, 202L, ZonedDateTime.now(), "evt-1");
    when(kafkaTemplate.send("coupon-issue-requests-v3", "202", event))
        .thenReturn(CompletableFuture.<SendResult<Object, Object>>completedFuture(null));

    couponIssueRequestedEventProducer.send(event);

    verify(kafkaTemplate).send("coupon-issue-requests-v3", "202", event);
  }
}

package com.dragons.producer;

import static org.mockito.Mockito.verify;

import com.dragons.coupon.issue.CouponIssueRequestedEvent;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class CouponIssueRequestedEventProducerTest {

  @Mock
  private KafkaTemplate<Object, Object> kafkaTemplate;

  @InjectMocks
  private CouponIssueRequestedEventProducer couponIssueRequestedEventProducer;

  @Test
  void publish_sendsEventToKafka() {
    CouponIssueRequestedEvent event = new CouponIssueRequestedEvent(101L, 202L, ZonedDateTime.now());

    couponIssueRequestedEventProducer.send(event);

    verify(kafkaTemplate).send("coupon-issue-requests", "101", event);
  }
}

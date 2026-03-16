package com.dragons.consumer;

import static org.mockito.Mockito.verify;

import com.dragons.coupon.issue.CouponIssueRequestHandler;
import com.dragons.coupon.issue.CouponIssueRequestedEvent;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CouponIssueRequestedEventConsumerTest {

  @Mock
  private CouponIssueRequestHandler couponIssueRequestHandler;

  @InjectMocks
  private CouponIssueRequestedEventConsumer couponIssueRequestedEventConsumer;

  @Test
  void handle_delegatesToApplicationHandler() {
    CouponIssueRequestedEvent event = new CouponIssueRequestedEvent(101L, 202L, ZonedDateTime.now());

    couponIssueRequestedEventConsumer.handle(event);

    verify(couponIssueRequestHandler).handle(event);
  }
}

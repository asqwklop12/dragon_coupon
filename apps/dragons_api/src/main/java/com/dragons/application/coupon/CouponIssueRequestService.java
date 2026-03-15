package com.dragons.application.coupon;

import com.dragons.application.coupon.dto.CouponIssueCommand;
import com.dragons.application.coupon.event.CouponIssueRequestedEvent;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponIssueRequestService {

  private final ApplicationEventPublisher applicationEventPublisher;

  public CouponIssueRequestedEvent requestIssue(CouponIssueCommand command) {
    CouponIssueRequestedEvent event = new CouponIssueRequestedEvent(
        command.couponId(),
        command.userId(),
        ZonedDateTime.now()
    );
    applicationEventPublisher.publishEvent(event);
    return event;
  }
}

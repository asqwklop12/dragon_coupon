package com.dragons.application.coupon;

import com.dragons.application.coupon.dto.CouponIssueCommand;
import com.dragons.coupon.issue.CouponIssueRequestProducer;
import com.dragons.coupon.issue.CouponIssueRequestedEvent;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponIssueRequestService {

  private final CouponIssueRequestProducer couponIssueRequestProducer;

  public CouponIssueRequestedEvent requestIssue(CouponIssueCommand command) {
    CouponIssueRequestedEvent event = new CouponIssueRequestedEvent(
        command.couponId(),
        command.userId(),
        ZonedDateTime.now(),
        UUID.randomUUID().toString()
    );
    couponIssueRequestProducer.send(event);
    return event;
  }
}

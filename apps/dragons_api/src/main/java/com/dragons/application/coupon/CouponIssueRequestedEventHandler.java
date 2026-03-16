package com.dragons.application.coupon;

import com.dragons.application.coupon.dto.CouponIssueCommand;
import com.dragons.coupon.issue.CouponIssueRequestHandler;
import com.dragons.coupon.issue.CouponIssueRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponIssueRequestedEventHandler implements CouponIssueRequestHandler {

  private final CouponService couponService;

  @Override
  public void handle(CouponIssueRequestedEvent event) {
    try {
      couponService.issueCoupon(new CouponIssueCommand(event.couponId(), event.userId()));
    } catch (RuntimeException exception) {
      log.error(
          "Failed to process coupon issue request. couponId={}, userId={}, requestedAt={}",
          event.couponId(),
          event.userId(),
          event.requestedAt(),
          exception
      );
    }
  }
}

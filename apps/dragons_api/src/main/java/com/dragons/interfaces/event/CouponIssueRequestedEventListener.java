package com.dragons.interfaces.event;

import com.dragons.application.coupon.CouponService;
import com.dragons.application.coupon.dto.CouponIssueCommand;
import com.dragons.application.coupon.event.CouponIssueRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponIssueRequestedEventListener {

  private final CouponService couponService;

  @Async
  @EventListener
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

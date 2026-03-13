package com.dragons.application.coupon.event;

import java.time.ZonedDateTime;

public record CouponIssueRequestedEvent(
    Long couponId,
    Long userId,
    ZonedDateTime requestedAt
) {
}

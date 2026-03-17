package com.dragons.coupon.issue;

import java.time.ZonedDateTime;

public record CouponIssueRequestedEvent(
    Long couponId,
    Long userId,
    ZonedDateTime requestedAt
) {
}

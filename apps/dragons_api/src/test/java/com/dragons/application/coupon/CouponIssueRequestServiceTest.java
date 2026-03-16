package com.dragons.application.coupon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.dragons.application.coupon.dto.CouponIssueCommand;
import com.dragons.coupon.issue.CouponIssueRequestProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CouponIssueRequestServiceTest {

  @Mock
  private CouponIssueRequestProducer couponIssueRequestProducer;

  @InjectMocks
  private CouponIssueRequestService couponIssueRequestService;

  @Test
  void requestIssue_publishesEventWithoutInfraDependency() {
    var result = couponIssueRequestService.requestIssue(new CouponIssueCommand(101L, 202L));

    assertThat(result.couponId()).isEqualTo(101L);
    assertThat(result.userId()).isEqualTo(202L);
    assertThat(result.requestedAt()).isNotNull();
    verify(couponIssueRequestProducer).send(result);
  }
}

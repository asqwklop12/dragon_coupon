package com.dragons.application.coupon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dragons.application.coupon.dto.CouponCreateCommand;
import com.dragons.application.coupon.dto.CouponIssueCommand;
import com.dragons.domain.coupon.Coupon;
import com.dragons.domain.coupon.CouponRepository;
import com.dragons.domain.coupon.CouponStatus;
import com.dragons.domain.coupon.CouponStockRepository;
import com.dragons.domain.coupon.CouponType;
import com.dragons.domain.coupon.CouponUsageHistoryRepository;
import com.dragons.domain.coupon.IssuedCoupon;
import com.dragons.domain.coupon.IssuedCouponRepository;
import com.dragons.domain.coupon.IssuedCouponStatus;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

  @Mock
  private CouponRepository couponRepository;

  @Mock
  private IssuedCouponRepository issuedCouponRepository;

  @Mock
  private CouponStockRepository couponStockRepository;

  @Mock
  private CouponUsageHistoryRepository couponUsageHistoryRepository;

  @InjectMocks
  private CouponService couponService;

  @Test
  void createCoupon_initializesRedisStock() {
    Coupon coupon = createCouponEntity(11L, 10, 0);
    when(couponRepository.store(any(Coupon.class))).thenReturn(coupon);

    var result = couponService.createCoupon(new CouponCreateCommand(
        "테스트 쿠폰",
        "설명",
        CouponType.FIXED_AMOUNT,
        CouponStatus.ACTIVE,
        1000,
        10000,
        1000,
        10,
        7,
        OffsetDateTime.now().minusDays(1),
        OffsetDateTime.now().plusDays(1)
    ));

    assertThat(result.couponId()).isEqualTo(11L);
    verify(couponStockRepository).initializeStockIfAbsent(11L, 10);
  }

  @Test
  void issueCoupon_usesDbLock_andStoresIssue() {
    Coupon coupon = createCouponEntity(1L, 10, 0);
    IssuedCoupon issuedCoupon = createIssuedCouponEntity(100L, coupon, 55L, "evt-1");

    when(issuedCouponRepository.readByIssueRequestId("evt-1")).thenReturn(Optional.empty());
    when(couponRepository.readCouponForUpdate(1L)).thenReturn(Optional.of(coupon));
    when(issuedCouponRepository.existsByCouponIdAndUserId(1L, 55L)).thenReturn(false);
    when(issuedCouponRepository.store(any(IssuedCoupon.class))).thenReturn(issuedCoupon);

    var result = couponService.issueCoupon(new CouponIssueCommand(1L, 55L, "evt-1"));

    assertThat(result.issuedCouponId()).isEqualTo(100L);
    assertThat(result.couponId()).isEqualTo(1L);
    assertThat(result.userId()).isEqualTo(55L);
    assertThat(coupon.getIssuedQuantity()).isEqualTo(1);
    verify(couponRepository).readCouponForUpdate(1L);
    verify(couponStockRepository).clearStock(1L);
  }

  @Test
  void issueCoupon_returnsExistingIssue_whenInsertFailsByDuplicateRequestId() {
    Coupon coupon = createCouponEntity(1L, 10, 0);
    IssuedCoupon issuedCoupon = createIssuedCouponEntity(100L, coupon, 55L, "evt-1");

    when(issuedCouponRepository.readByIssueRequestId("evt-1")).thenReturn(Optional.empty(), Optional.of(issuedCoupon));
    when(couponRepository.readCouponForUpdate(1L)).thenReturn(Optional.of(coupon));
    when(issuedCouponRepository.existsByCouponIdAndUserId(1L, 55L)).thenReturn(false);
    when(issuedCouponRepository.store(any(IssuedCoupon.class)))
        .thenThrow(new DataIntegrityViolationException("duplicate"));

    var result = couponService.issueCoupon(new CouponIssueCommand(1L, 55L, "evt-1"));

    assertThat(result.issuedCouponId()).isEqualTo(100L);
    assertThat(coupon.getIssuedQuantity()).isEqualTo(0);
    verify(couponStockRepository, never()).clearStock(1L);
  }

  @Test
  void issueCoupon_returnsExistingIssue_whenSameRequestIdAlreadyProcessed() {
    Coupon coupon = createCouponEntity(1L, 10, 0);
    IssuedCoupon issuedCoupon = createIssuedCouponEntity(100L, coupon, 55L, "evt-1");

    when(issuedCouponRepository.readByIssueRequestId("evt-1")).thenReturn(Optional.of(issuedCoupon));

    var result = couponService.issueCoupon(new CouponIssueCommand(1L, 55L, "evt-1"));

    assertThat(result.issuedCouponId()).isEqualTo(100L);
    verify(couponRepository, never()).readCouponForUpdate(1L);
    verify(couponStockRepository, never()).clearStock(1L);
  }

  @Test
  void getStock_initializesRedisFromIssuedCouponCount_whenCacheMiss() {
    Coupon coupon = createCouponEntity(1L, 10, 0);

    when(couponRepository.readCoupon(1L)).thenReturn(Optional.of(coupon));
    when(couponStockRepository.readStock(1L)).thenReturn(null, 7);
    when(issuedCouponRepository.countByCouponId(1L)).thenReturn(3L);

    var result = couponService.getStock(1L);

    assertThat(result.remainingQuantity()).isEqualTo(7);
    verify(couponStockRepository).initializeStockIfAbsent(1L, 7);
    verify(couponStockRepository, times(2)).readStock(1L);
  }

  private Coupon createCouponEntity(Long id, int totalQuantity, int issuedQuantity) {
    Coupon coupon = Coupon.create(
        "테스트 쿠폰",
        "설명",
        CouponType.FIXED_AMOUNT,
        CouponStatus.ACTIVE,
        1000,
        10000,
        1000,
        totalQuantity,
        7,
        ZonedDateTime.now().minusDays(1),
        ZonedDateTime.now().plusDays(1)
    );
    ReflectionTestUtils.setField(coupon, "id", id);
    ReflectionTestUtils.setField(coupon, "issuedQuantity", issuedQuantity);
    return coupon;
  }

  private IssuedCoupon createIssuedCouponEntity(Long id, Coupon coupon, Long userId, String issueRequestId) {
    IssuedCoupon issuedCoupon = IssuedCoupon.issue(coupon, userId, ZonedDateTime.now(), issueRequestId);
    ReflectionTestUtils.setField(issuedCoupon, "id", id);
    ReflectionTestUtils.setField(issuedCoupon, "status", IssuedCouponStatus.ISSUED);
    return issuedCoupon;
  }
}

package com.dragons.application.coupon;

import com.dragons.application.coupon.dto.CouponAvailableResult;
import com.dragons.application.coupon.dto.CouponCreateCommand;
import com.dragons.application.coupon.dto.CouponCreateResult;
import com.dragons.application.coupon.dto.CouponIssueCommand;
import com.dragons.application.coupon.dto.CouponIssueResult;
import com.dragons.application.coupon.dto.CouponStockResult;
import com.dragons.application.coupon.dto.CouponUseCommand;
import com.dragons.application.coupon.dto.CouponUseResult;
import com.dragons.application.coupon.dto.CouponUserCouponsResult;
import com.dragons.domain.coupon.Coupon;
import com.dragons.domain.coupon.CouponRepository;
import com.dragons.domain.coupon.CouponStatus;
import com.dragons.domain.coupon.CouponStockRepository;
import com.dragons.domain.coupon.CouponType;
import com.dragons.domain.coupon.CouponUsageHistory;
import com.dragons.domain.coupon.CouponUsageHistoryRepository;
import com.dragons.domain.coupon.IssuedCoupon;
import com.dragons.domain.coupon.IssuedCouponRepository;
import com.dragons.support.error.CouponExhaustedException;
import com.dragons.support.error.CouponNotAvailableException;
import com.dragons.support.error.CouponNotFoundException;
import com.dragons.support.error.DuplicateCouponIssueException;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {
  private final CouponRepository couponRepository;
  private final IssuedCouponRepository issuedCouponRepository;
  private final CouponStockRepository couponStockRepository;
  private final CouponUsageHistoryRepository couponUsageHistoryRepository;

  @Transactional(readOnly = true)
  public CouponAvailableResult getAvailableCoupons() {
    ZonedDateTime now = ZonedDateTime.now();
    List<CouponAvailableResult.CouponItem> coupons = couponRepository.readIssuableCoupons(now).stream()
        .map(coupon -> new CouponAvailability(coupon, getCurrentStock(coupon)))
        .filter(availability -> availability.remainingQuantity() > 0)
        .map(availability -> {
          Coupon coupon = availability.coupon();
          return new CouponAvailableResult.CouponItem(
              coupon.getId(),
              coupon.getName(),
              coupon.getDescription(),
              coupon.getCouponType(),
              coupon.getDiscountValue(),
              coupon.getMinOrderAmount(),
              coupon.getMaxDiscountAmount(),
              availability.remainingQuantity(),
              coupon.getStartDate(),
              coupon.getEndDate()
          );
        })
        .toList();
    return new CouponAvailableResult(coupons);
  }

  @Transactional
  public CouponCreateResult createCoupon(CouponCreateCommand command) {
    Coupon coupon = couponRepository.store(Coupon.create(
        command.name(),
        command.description(),
        command.couponType(),
        command.status(),
        command.discountValue(),
        command.minOrderAmount(),
        command.maxDiscountAmount(),
        command.totalQuantity(),
        command.validDays(),
        command.startDate().toZonedDateTime(),
        command.endDate().toZonedDateTime()
    ));
    couponStockRepository.initializeStockIfAbsent(coupon.getId(), coupon.getRemainingQuantity());

    return new CouponCreateResult(
        coupon.getId(),
        coupon.getName(),
        coupon.getDescription(),
        coupon.getCouponType(),
        coupon.getStatus(),
        coupon.getDiscountValue(),
        coupon.getMinOrderAmount(),
        coupon.getMaxDiscountAmount(),
        coupon.getTotalQuantity(),
        coupon.getIssuedQuantity(),
        coupon.getValidDays(),
        coupon.getStartDate(),
        coupon.getEndDate()
    );
  }

  @Transactional
  public CouponIssueResult issueCoupon(CouponIssueCommand command) {
    ZonedDateTime now = ZonedDateTime.now();
    Coupon coupon = couponRepository.readCoupon(command.couponId())
        .orElseThrow(CouponNotFoundException::new);

    if (issuedCouponRepository.existsByCouponIdAndUserId(command.couponId(), command.userId())) {
      throw new DuplicateCouponIssueException();
    }

    int currentStock = getCurrentStock(coupon);
    if (!isCouponIssuableAt(coupon, now)) {
      if (currentStock <= 0) {
        throw new CouponExhaustedException();
      }
      throw new CouponNotAvailableException();
    }

    if (!couponStockRepository.decreaseStock(coupon.getId())) {
      throw new CouponExhaustedException();
    }

    IssuedCoupon issuedCoupon;
    try {
      issuedCoupon = issuedCouponRepository.store(IssuedCoupon.issue(coupon, command.userId(), now));
    } catch (DataIntegrityViolationException e) {
      couponStockRepository.increaseStock(coupon.getId());
      throw new DuplicateCouponIssueException();
    } catch (RuntimeException e) {
      couponStockRepository.increaseStock(coupon.getId());
      throw e;
    }

    return new CouponIssueResult(
        issuedCoupon.getId(),
        coupon.getId(),
        issuedCoupon.getUserId(),
        issuedCoupon.getStatus(),
        issuedCoupon.getIssuedAt(),
        issuedCoupon.getExpiredAt()
    );
  }

  @Transactional(readOnly = true)
  public CouponStockResult getStock(Long couponId) {
    Coupon coupon = couponRepository.readCoupon(couponId)
        .orElseThrow(CouponNotFoundException::new);
    return new CouponStockResult(coupon.getId(), getCurrentStock(coupon));
  }

  @Transactional(readOnly = true)
  public CouponUserCouponsResult getUserCoupons(Long userId) {
    List<CouponUserCouponsResult.CouponItem> coupons = issuedCouponRepository.readUserCoupons(userId).stream()
        .map(issuedCoupon -> new CouponUserCouponsResult.CouponItem(
            issuedCoupon.getId(),
            issuedCoupon.getCoupon().getId(),
            issuedCoupon.getCoupon().getName(),
            issuedCoupon.getStatus(),
            issuedCoupon.getIssuedAt(),
            issuedCoupon.getExpiredAt(),
            issuedCoupon.getUsedAt()
        ))
        .toList();
    return new CouponUserCouponsResult(coupons);
  }

  @Transactional(readOnly = true)
  public CouponUserCouponsResult getUsableCoupons(Long userId) {
    ZonedDateTime now = ZonedDateTime.now();
    List<CouponUserCouponsResult.CouponItem> coupons = issuedCouponRepository.readUsableUserCoupons(userId, now)
        .stream()
        .filter(issuedCoupon -> issuedCoupon.isUsableAt(now))
        .map(issuedCoupon -> new CouponUserCouponsResult.CouponItem(
            issuedCoupon.getId(),
            issuedCoupon.getCoupon().getId(),
            issuedCoupon.getCoupon().getName(),
            issuedCoupon.getStatus(),
            issuedCoupon.getIssuedAt(),
            issuedCoupon.getExpiredAt(),
            issuedCoupon.getUsedAt()
        ))
        .toList();
    return new CouponUserCouponsResult(coupons);
  }

  @Transactional
  public CouponUseResult useCoupon(CouponUseCommand command) {
    ZonedDateTime now = ZonedDateTime.now();
    IssuedCoupon issuedCoupon = issuedCouponRepository.readIssuedCoupon(command.issuedCouponId())
        .orElseThrow(CouponNotFoundException::new);

    if (!issuedCoupon.isUsableAt(now)) {
      throw new CouponNotAvailableException();
    }

    int orderAmount = command.orderAmount();
    Coupon coupon = issuedCoupon.getCoupon();
    if (coupon.getMinOrderAmount() != null && orderAmount < coupon.getMinOrderAmount()) {
      throw new CouponNotAvailableException();
    }

    int discountAmount = calculateDiscountAmount(coupon, orderAmount);
    issuedCoupon.use(now);
    couponUsageHistoryRepository.add(CouponUsageHistory.create(
        issuedCoupon,
        issuedCoupon.getUserId(),
        command.orderId(),
        discountAmount,
        now
    ));

    return new CouponUseResult(
        issuedCoupon.getId(),
        command.orderId(),
        discountAmount,
        issuedCoupon.getStatus(),
        issuedCoupon.getUsedAt()
    );
  }

  private int calculateDiscountAmount(Coupon coupon, int orderAmount) {
    int discountAmount = 0;
    if (coupon.getCouponType() == CouponType.FIXED_AMOUNT) {
      discountAmount = Math.min(coupon.getDiscountValue(), orderAmount);
    }
    if (coupon.getCouponType() == CouponType.PERCENTAGE) {
      long rawDiscount = ((long) orderAmount * (long) coupon.getDiscountValue()) / 100L;
      discountAmount = (int) Math.min(rawDiscount, orderAmount);
    }

    Integer maxDiscountAmount = coupon.getMaxDiscountAmount();
    if (maxDiscountAmount != null) {
      discountAmount = Math.min(discountAmount, maxDiscountAmount);
    }
    return discountAmount;
  }

  private int getCurrentStock(Coupon coupon) {
    Integer stock = couponStockRepository.readStock(coupon.getId());
    if (stock != null) {
      return stock;
    }

    int remainingStock = Math.max(
        coupon.getTotalQuantity() - Math.toIntExact(issuedCouponRepository.countByCouponId(coupon.getId())),
        0
    );
    couponStockRepository.initializeStockIfAbsent(coupon.getId(), remainingStock);

    Integer initializedStock = couponStockRepository.readStock(coupon.getId());
    return initializedStock == null ? remainingStock : initializedStock;
  }

  private boolean isCouponIssuableAt(Coupon coupon, ZonedDateTime now) {
    return coupon.getStatus() == CouponStatus.ACTIVE
        && (coupon.getStartDate().isBefore(now) || coupon.getStartDate().isEqual(now))
        && (coupon.getEndDate().isAfter(now) || coupon.getEndDate().isEqual(now));
  }

  private record CouponAvailability(Coupon coupon, int remainingQuantity) {
  }
}

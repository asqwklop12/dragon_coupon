package com.dragons.interfaces.api.coupon;

import com.dragons.application.coupon.CouponIssueRequestService;
import com.dragons.application.coupon.CouponService;
import com.dragons.application.coupon.dto.CouponCreateCommand;
import com.dragons.application.coupon.dto.CouponIssueCommand;
import com.dragons.application.coupon.dto.CouponUseCommand;
import com.dragons.coupon.issue.CouponIssueRequestedEvent;
import com.dragons.interfaces.api.coupon.dto.CouponV1Dto;
import com.dragons.support.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coupons")
public class CouponV1Controller implements CouponV1Spec {
  private final CouponIssueRequestService couponIssueRequestService;
  private final CouponService couponService;

  @Override
  @PostMapping
  public ApiResponse<CouponV1Dto.Create.Response> createCoupon(
      @RequestBody @Valid CouponV1Dto.Create.Request request
  ) {
    var result = couponService.createCoupon(new CouponCreateCommand(
        request.name(),
        request.description(),
        request.couponType(),
        request.status(),
        request.discountValue(),
        request.minOrderAmount(),
        request.maxDiscountAmount(),
        request.totalQuantity(),
        request.validDays(),
        request.startDate(),
        request.endDate()
    ));
    return ApiResponse.successResponse(new CouponV1Dto.Create.Response(
        result.couponId(),
        result.name(),
        result.description(),
        result.couponType(),
        result.status(),
        result.discountValue(),
        result.minOrderAmount(),
        result.maxDiscountAmount(),
        result.totalQuantity(),
        result.issuedQuantity(),
        result.validDays(),
        result.startDate().toOffsetDateTime(),
        result.endDate().toOffsetDateTime()
    ));
  }

  @Override
  @GetMapping("/available")
  public ApiResponse<CouponV1Dto.Available.Response> getAvailableCoupons() {
    var result = couponService.getAvailableCoupons();
    return ApiResponse.successResponse(new CouponV1Dto.Available.Response(
        result.coupons().stream()
            .map(coupon -> new CouponV1Dto.Available.Coupon(
                coupon.couponId(),
                coupon.name(),
                coupon.description(),
                coupon.couponType(),
                coupon.discountValue(),
                coupon.minOrderAmount(),
                coupon.maxDiscountAmount(),
                coupon.remainingQuantity(),
                coupon.startDate().toLocalDateTime(),
                coupon.endDate().toLocalDateTime()
            ))
            .toList()
    ));
  }

  @Override
  @PostMapping("/{couponId}/issue")
  public ApiResponse<CouponV1Dto.Issue.Response> issueCoupon(
      @PathVariable Long couponId,
      @RequestBody @Valid CouponV1Dto.Issue.Request request
  ) {
    CouponIssueRequestedEvent event = couponIssueRequestService.requestIssue(
        new CouponIssueCommand(couponId, request.userId(), null));
    return ApiResponse.successResponse(new CouponV1Dto.Issue.Response(
        event.couponId(),
        event.userId(),
        event.requestedAt().toLocalDateTime()
    ));
  }

  @Override
  @GetMapping("/{couponId}/stock")
  public ApiResponse<CouponV1Dto.Stock.Response> getCouponStock(@PathVariable Long couponId) {
    var result = couponService.getStock(couponId);
    return ApiResponse.successResponse(new CouponV1Dto.Stock.Response(
        result.couponId(),
        result.remainingQuantity()
    ));
  }

  @Override
  @GetMapping("/users/{userId}")
  public ApiResponse<CouponV1Dto.UserCoupon.Response> getUserCoupons(@PathVariable Long userId) {
    var result = couponService.getUserCoupons(userId);
    return ApiResponse.successResponse(new CouponV1Dto.UserCoupon.Response(
        result.coupons().stream()
            .map(coupon -> new CouponV1Dto.UserCoupon.Item(
                coupon.issuedCouponId(),
                coupon.couponId(),
                coupon.couponName(),
                coupon.status(),
                coupon.issuedAt().toLocalDateTime(),
                coupon.expiredAt().toLocalDateTime(),
                coupon.usedAt() == null ? null : coupon.usedAt().toLocalDateTime()
            ))
            .toList()
    ));
  }

  @Override
  @GetMapping("/users/{userId}/usable")
  public ApiResponse<CouponV1Dto.UserCoupon.Response> getUsableCoupons(@PathVariable Long userId) {
    var result = couponService.getUsableCoupons(userId);
    return ApiResponse.successResponse(new CouponV1Dto.UserCoupon.Response(
        result.coupons().stream()
            .map(coupon -> new CouponV1Dto.UserCoupon.Item(
                coupon.issuedCouponId(),
                coupon.couponId(),
                coupon.couponName(),
                coupon.status(),
                coupon.issuedAt().toLocalDateTime(),
                coupon.expiredAt().toLocalDateTime(),
                coupon.usedAt() == null ? null : coupon.usedAt().toLocalDateTime()
            ))
            .toList()
    ));
  }

  @Override
  @PostMapping("/{issuedCouponId}/use")
  public ApiResponse<CouponV1Dto.Use.Response> useCoupon(
      @PathVariable Long issuedCouponId,
      @RequestBody @Valid CouponV1Dto.Use.Request request
  ) {
    var result = couponService.useCoupon(new CouponUseCommand(
        issuedCouponId,
        request.orderId(),
        request.orderAmount()
    ));
    return ApiResponse.successResponse(new CouponV1Dto.Use.Response(
        result.issuedCouponId(),
        result.orderId(),
        result.discountAmount(),
        result.status(),
        result.usedAt().toLocalDateTime()
    ));
  }




}

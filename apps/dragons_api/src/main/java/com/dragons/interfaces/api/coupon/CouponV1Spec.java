package com.dragons.interfaces.api.coupon;


import com.dragons.interfaces.api.coupon.dto.CouponV1Dto;
import com.dragons.interfaces.api.coupon.dto.CouponV1Dto.Create.Response;
import com.dragons.support.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Coupon V1 API", description = "쿠폰 발급/사용 API")
public interface CouponV1Spec {

  @Operation(summary = "쿠폰 생성", description = "이벤트 쿠폰을 생성합니다.")
  ApiResponse<Response> createCoupon(CouponV1Dto.Create.Request request);

  @Operation(summary = "발급 가능 쿠폰 조회", description = "현재 발급 가능한 쿠폰 목록을 조회합니다.")
  ApiResponse<CouponV1Dto.Available.Response> getAvailableCoupons();

  @Operation(summary = "쿠폰 발급 요청", description = "선착순 쿠폰 발급을 비동기로 요청합니다.")
  ApiResponse<CouponV1Dto.Issue.Response> issueCoupon(Long couponId, CouponV1Dto.Issue.Request request);

  @Operation(summary = "쿠폰 재고 조회", description = "특정 쿠폰의 남은 수량을 조회합니다.")
  ApiResponse<CouponV1Dto.Stock.Response> getCouponStock(Long couponId);

  @Operation(summary = "사용자 쿠폰 조회", description = "특정 사용자가 보유한 전체 쿠폰을 조회합니다.")
  ApiResponse<CouponV1Dto.UserCoupon.Response> getUserCoupons(
      Long userId);

  @Operation(summary = "사용자 사용 가능 쿠폰 조회", description = "특정 사용자가 보유한 사용 가능 쿠폰을 조회합니다.")
  ApiResponse<CouponV1Dto.UserCoupon.Response> getUsableCoupons(
      Long userId);

  @Operation(summary = "쿠폰 사용", description = "발급된 쿠폰을 사용 처리합니다.")
  ApiResponse<CouponV1Dto.Use.Response> useCoupon(
      Long issuedCouponId,
      CouponV1Dto.Use.Request request);
}

package com.dragons.interfaces.api.coupon;

import com.dragons.interfaces.api.coupon.dto.CouponAvailableResult;
import com.dragons.interfaces.api.coupon.dto.CouponIssueResult;
import com.dragons.interfaces.api.coupon.dto.CouponStockResult;
import com.dragons.interfaces.api.coupon.dto.CouponUseCommand;
import com.dragons.interfaces.api.coupon.dto.CouponUseResult;
import com.dragons.interfaces.api.coupon.dto.CouponUserCouponsResult;
import com.dragons.support.api.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coupons")
public class CouponV1Controller {

  @GetMapping("/available")
  public ApiResponse<CouponAvailableResult> getAvailableCoupons() {
    return ApiResponse.successResponse(new CouponAvailableResult(List.of()));
  }

  @PostMapping("/{couponId}/issue")
  public ApiResponse<CouponIssueResult> issueCoupon(
      @PathVariable Long couponId,
      @RequestParam Long userId
  ) {
    return ApiResponse.successResponse(null);
  }

  @GetMapping("/{couponId}/stock")
  public ApiResponse<CouponStockResult> getCouponStock(@PathVariable Long couponId) {
    return ApiResponse.successResponse(null);
  }

  @GetMapping("/users/{userId}")
  public ApiResponse<CouponUserCouponsResult> getUserCoupons(@PathVariable Long userId) {
    return ApiResponse.successResponse(new CouponUserCouponsResult(List.of()));
  }

  @GetMapping("/users/{userId}/usable")
  public ApiResponse<CouponUserCouponsResult> getUsableCoupons(@PathVariable Long userId) {
    return ApiResponse.successResponse(new CouponUserCouponsResult(List.of()));
  }

  @PostMapping("/{issuedCouponId}/use")
  public ApiResponse<CouponUseResult> useCoupon(
      @PathVariable Long issuedCouponId,
      @RequestBody CouponUseCommand request
  ) {
    return ApiResponse.successResponse(null);
  }
}

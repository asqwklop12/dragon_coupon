package com.dragons.interfaces.api.coupon.dto;

import com.dragons.domain.coupon.CouponStatus;
import com.dragons.domain.coupon.CouponType;
import com.dragons.domain.coupon.IssuedCouponStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Schema(name = "CouponV1Dto", description = "쿠폰 API v1 DTO")
public class CouponV1Dto {

  private CouponV1Dto() {
  }

  public static class Create {
    @Schema(name = "CouponCreateRequest")
    public record Request(
        @NotBlank @Schema(description = "쿠폰명", example = "신규가입 10% 할인") String name,
        @NotBlank @Schema(description = "쿠폰 설명", example = "신규가입 고객 대상 할인 쿠폰") String description,
        @NotNull @Schema(description = "쿠폰 타입", example = "PERCENTAGE") CouponType couponType,
        @Schema(description = "쿠폰 상태(기본 ACTIVE)", example = "ACTIVE") CouponStatus status,
        @NotNull @Positive @Schema(description = "할인 값", example = "10") Integer discountValue,
        @Positive @Schema(description = "최소 주문 금액", example = "30000") Integer minOrderAmount,
        @Positive @Schema(description = "최대 할인 금액", example = "5000") Integer maxDiscountAmount,
        @NotNull @Positive @Schema(description = "총 발급 수량", example = "100") Integer totalQuantity,
        @NotNull @Positive @Schema(description = "발급 후 유효 일수", example = "7") Integer validDays,
        @NotNull @Schema(description = "발급 시작일(OffsetDateTime)", example = "2026-03-03T10:00:00+09:00") OffsetDateTime startDate,
        @NotNull @Schema(description = "발급 종료일(OffsetDateTime)", example = "2026-03-10T23:59:59+09:00") OffsetDateTime endDate
    ) {
      public Request {
        if (status == null) {
          status = CouponStatus.ACTIVE;
        }
      }
    }

    @Schema(name = "CouponCreateResponse")
    public record Response(
        Long couponId,
        String name,
        String description,
        CouponType couponType,
        CouponStatus status,
        Integer discountValue,
        Integer minOrderAmount,
        Integer maxDiscountAmount,
        Integer totalQuantity,
        Integer issuedQuantity,
        Integer validDays,
        @Schema(example = "2026-03-03T10:00:00+09:00") OffsetDateTime startDate,
        @Schema(example = "2026-03-10T23:59:59+09:00") OffsetDateTime endDate
    ) {
    }
  }

  public static class Available {

    @Schema(name = "CouponAvailableItem")
    public record Coupon(
        Long couponId,
        String name,
        String description,
        CouponType couponType,
        Integer discountValue,
        Integer minOrderAmount,
        Integer maxDiscountAmount,
        Integer remainingQuantity,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
    }

    @Schema(name = "CouponAvailableResponse")
    public record Response(List<Coupon> coupons) {
    }
  }

  public static class Issue {

    @Schema(name = "CouponIssueRequest")
    public record Request(
        @NotNull @Schema(description = "유저 ID", example = "1") Long userId
    ) {
    }

    @Schema(name = "CouponIssueResponse")
    public record Response(
        Long couponId,
        Long userId,
        LocalDateTime requestedAt
    ) {
    }
  }

  public static class Stock {

    @Schema(name = "CouponStockResponse")
    public record Response(
        Long couponId,
        Integer remainingQuantity
    ) {
    }
  }

  public static class UserCoupon {

    @Schema(name = "CouponUserCouponItem")
    public record Item(
        Long issuedCouponId,
        Long couponId,
        String couponName,
        IssuedCouponStatus status,
        LocalDateTime issuedAt,
        LocalDateTime expiredAt,
        LocalDateTime usedAt
    ) {
    }

    @Schema(name = "CouponUserCouponResponse")
    public record Response(List<Item> coupons) {
    }
  }

  public static class Use {

    @Schema(name = "CouponUseRequest")
    public record Request(
        @NotNull @Schema(description = "주문 ID", example = "101") Long orderId,
        @NotNull @Positive @Schema(description = "주문 금액", example = "30000") Integer orderAmount
    ) {
    }

    @Schema(name = "CouponUseResponse")
    public record Response(
        Long issuedCouponId,
        Long orderId,
        Integer discountAmount,
        IssuedCouponStatus status,
        LocalDateTime usedAt
    ) {
    }
  }
}

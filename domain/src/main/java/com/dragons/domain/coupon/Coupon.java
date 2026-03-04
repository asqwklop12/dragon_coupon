package com.dragons.domain.coupon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "coupon")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false, length = 1000)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private CouponType couponType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private CouponStatus status;

  @Column(nullable = false)
  private Integer discountValue;

  @Column
  private Integer minOrderAmount;

  @Column
  private Integer maxDiscountAmount;

  @Column(nullable = false)
  private Integer totalQuantity;

  @Column(nullable = false)
  private Integer issuedQuantity;

  @Column(nullable = false)
  private Integer validDays;

  @Column(nullable = false)
  private LocalDateTime startDate;

  @Column(nullable = false)
  private LocalDateTime endDate;
}

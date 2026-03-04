package com.dragons.domain.coupon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "coupon_usage_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponUsageHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "issued_coupon_id", nullable = false)
  private IssuedCoupon issuedCoupon;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  private Long orderId;

  @Column(nullable = false)
  private Integer discountAmount;

  @Column(nullable = false)
  private LocalDateTime usedAt;
}

package com.dragons.domain.outbox;

public enum OutboxEventStatus {
  PENDING,
  SENT,
  FAILED
}

package com.dragons.domain.outbox;

public interface OutboxEventRepository {
  OutboxEvent store(OutboxEvent outboxEvent);
}

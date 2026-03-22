package com.dragons.model;

import com.dragons.domain.outbox.OutboxEvent;
import com.dragons.domain.outbox.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class OutboxEventRepositoryImpl implements OutboxEventRepository {
  private final JpaOutboxEventRepository jpaOutboxEventRepository;

  @Override
  public OutboxEvent store(OutboxEvent outboxEvent) {
    return jpaOutboxEventRepository.save(outboxEvent);
  }
}

package com.dragons.model;

import com.dragons.domain.outbox.OutboxEvent;
import com.dragons.domain.outbox.OutboxEventRepository;
import com.dragons.domain.outbox.OutboxEventStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class OutboxEventRepositoryImpl implements OutboxEventRepository {
  private static final List<OutboxEventStatus> RETRYABLE_STATUSES = List.of(
      OutboxEventStatus.PENDING,
      OutboxEventStatus.FAILED
  );

  private final JpaOutboxEventRepository jpaOutboxEventRepository;

  @Override
  public OutboxEvent store(OutboxEvent outboxEvent) {
    return jpaOutboxEventRepository.save(outboxEvent);
  }

  @Override
  public List<OutboxEvent> readRetryTargets(int size) {
    return jpaOutboxEventRepository.findByStatusInOrderByIdAsc(RETRYABLE_STATUSES, PageRequest.of(0, size));
  }
}

package com.dragons.model;

import com.dragons.domain.outbox.OutboxEvent;
import com.dragons.domain.outbox.OutboxEventStatus;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface JpaOutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
  List<OutboxEvent> findByStatusInOrderByIdAsc(List<OutboxEventStatus> statuses, Pageable pageable);
}

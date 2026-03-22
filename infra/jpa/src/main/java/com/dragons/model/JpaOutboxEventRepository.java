package com.dragons.model;

import com.dragons.domain.outbox.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

interface JpaOutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
}

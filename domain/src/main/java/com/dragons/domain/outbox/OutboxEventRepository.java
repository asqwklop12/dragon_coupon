package com.dragons.domain.outbox;

import java.util.List;

public interface OutboxEventRepository {
  OutboxEvent store(OutboxEvent outboxEvent);

  List<OutboxEvent> readRetryTargets(int size);
}

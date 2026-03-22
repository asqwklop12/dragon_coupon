package com.dragons.domain.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.ZonedDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "outbox_event",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_outbox_event_event_id",
            columnNames = "event_id"
        )
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, updatable = false, length = 36)
  private String eventId;

  @Column(nullable = false, updatable = false, length = 255)
  private String topic;

  @Column(nullable = false, updatable = false, length = 100)
  private String partitionKey;

  @Lob
  @Column(nullable = false, updatable = false)
  private String payload;

  @Column(name = "created_at", nullable = false, updatable = false)
  private ZonedDateTime createdAt;

  public static OutboxEvent create(
      String eventId,
      String topic,
      String partitionKey,
      String payload,
      ZonedDateTime createdAt
  ) {
    OutboxEvent outboxEvent = new OutboxEvent();
    outboxEvent.eventId = eventId;
    outboxEvent.topic = topic;
    outboxEvent.partitionKey = partitionKey;
    outboxEvent.payload = payload;
    outboxEvent.createdAt = createdAt;
    return outboxEvent;
  }
}

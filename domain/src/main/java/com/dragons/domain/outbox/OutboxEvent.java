package com.dragons.domain.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private OutboxEventStatus status;

  @Column(nullable = false)
  private int retryCount;

  @Column(name = "published_at")
  private ZonedDateTime publishedAt;

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
    outboxEvent.status = OutboxEventStatus.PENDING;
    outboxEvent.retryCount = 0;
    outboxEvent.publishedAt = null;
    outboxEvent.createdAt = createdAt;
    return outboxEvent;
  }

  public void markSent(ZonedDateTime publishedAt) {
    this.status = OutboxEventStatus.SENT;
    this.publishedAt = publishedAt;
  }

  public void markFailed() {
    this.status = OutboxEventStatus.FAILED;
    this.retryCount += 1;
  }
}

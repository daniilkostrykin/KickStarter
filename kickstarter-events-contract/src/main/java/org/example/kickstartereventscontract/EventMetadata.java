package org.example.kickstartereventscontract;

import java.time.Instant;
import java.util.UUID;

/**
 * Метаданные каждого события это его паспорт, которая сопровождает payload.
 *
 * В прод системах вдохновлена спецификацией CloudEvents (cloudevents.io),
 * Мы упростили для лабораторных.Позволяет:
 * - идентифицировать каждое событие (eventId) для дедупликации и трассировки,
 * - знать когда оно произошло (timestamp),
 * - знать кто его отправил (source),
 * - определить тип события (eventType) без десериализации payload.
 */
public record EventMetadata(

        // Уникальный идентификатор события (UUID v4).
        // Позволяет обнаружить повторную доставку и реализовать idempotent consumer.
        String eventId,

        // Момент создания события в формате ISO-8601 (UTC).
        Instant timestamp,

        // Источник события — имя сервиса, который его породил.
        String source,

        // Тип события: "book.created", "author.deleted" и т.д.
        // Совпадает с routing key в RabbitMQ — удобно для логирования.
        String eventType
) {
    /**
     * Фабричный метод для создания метаданных на стороне publisher'а.
     * Генерирует UUID и ставит текущее время автоматически.
     */
    public static EventMetadata create(String source, String eventType) {
        return new EventMetadata(
                UUID.randomUUID().toString(),
                Instant.now(),
                source,
                eventType
        );
    }
}

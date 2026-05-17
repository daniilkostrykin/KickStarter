package org.example.kickstartereventscontract;

/**
 * Обёртка для любого доменного события.
 *
 * Так мы делаем в продакшене часто, чтобы отделять метаданные доставки от бизнес-содержимого.
 * Это позволяет инфраструктурному коду (логирование, дедупликация, маршрутизация)
 * работать с метаданными, не зная ничего о payload :)
 *
 * @param <T> тип полезной нагрузки (BookEvent.Created, AuthorEvent.Deleted и т.д.)
 */
public record EventEnvelope<T>(
        EventMetadata metadata,
        T payload
) {
    /**
     * Фабричный метод - оборачивает payload в конверт с автоматически
     * заполненными метаданными (eventId, timestamp).
     */
    public static <T> EventEnvelope<T> wrap(T payload, String source, String eventType) {
        return new EventEnvelope<>(
                EventMetadata.create(source, eventType),
                payload
        );
    }
}


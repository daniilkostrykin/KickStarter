package org.example.kickstarterauditservice.listener;

import org.example.kickstarterauditservice.model.AuditEntry;
import org.example.kickstarterauditservice.storage.AuditStorage;
import org.example.kickstartereventscontract.*;
import org.example.kickstartereventscontract.EventMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

/**
 * Единый слушатель всех доменных событий из RabbitMQ.
 *
 * Принимает «сырое» AMQP-сообщение (Message) и десериализует его вручную.
 * Это необходимо, потому что EventEnvelope<T> — generic тип, и Jackson
 * не может определить конкретный подтип T при автоматической десериализации.
 *
 */
@Component
public class AuditEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuditEventListener.class);

    private final AuditStorage auditStorage;
    private final JsonMapper jsonMapper;

    public AuditEventListener(AuditStorage auditStorage, JsonMapper jsonMapper) {
        this.auditStorage = auditStorage;
        this.jsonMapper = jsonMapper;
    }

    /**
     * Принимает все события из очереди q.audit.events.
     *
     * Десериализация выполняется в два этапа:
     * 1. Парсим JSON в дерево узлов (JsonNode) — быстро и безопасно.
     * 2. Извлекаем metadata и определяем тип payload по полю eventType.
     * 3. Десериализуем payload в конкретный record по выявленному типу.
     */
    @RabbitListener(queues = "q.audit.events", messageConverter = "")
    public void handleEvent(Message message) {
        try {
            byte[] body = message.getBody();
            JsonNode root = jsonMapper.readTree(body);

            // Извлекаем метаданные из JSON-конверта
            JsonNode metaNode = root.get("metadata");
            EventMetadata metadata = jsonMapper.treeToValue(metaNode, EventMetadata.class);

            // Дедупликация — если событие уже обработано, пропускаем
            if (auditStorage.isDuplicate(metadata.eventId())) {
                log.warn("Дубликат события пропущен: eventId={}", metadata.eventId());
                return;
            }

            // Определяем тип события и формируем описание
            JsonNode payloadNode = root.get("payload");
            String description = buildDescription(metadata.eventType(), payloadNode);

            AuditEntry entry = auditStorage.save(new AuditEntry(
                    0,
                    metadata.eventId(),
                    metadata.eventType(),
                    metadata.source(),
                    metadata.timestamp(),
                    Instant.now(),
                    description
            ));

            log.info("[AUDIT #{}] {} | {}", entry.sequenceNumber(), metadata.eventType(), description);

        } catch (Exception e) {
            log.error("Ошибка обработки события: {}", e.getMessage(), e);
            // Исключение пробросится, сообщение уйдёт в DLQ после исчерпания retries
            throw new RuntimeException("Не удалось обработать событие", e);
        }
    }

    /**
     * Формирует человекочитаемое описание события для аудит-лога.
     *
     * Десериализует payload в конкретный тип на основе eventType,
     * затем формирует описание через pattern matching по sealed interface.
     */
    private String buildDescription(String eventType, JsonNode payloadNode) throws Exception {
        return switch (eventType) {
            case "project.created" -> {
                ProjectEvent.Created e = jsonMapper.treeToValue(payloadNode, ProjectEvent.Created.class);
                yield String.format("Создан проект «%s» (цель: %s, автор id=%d)",
                        e.title(), e.goal(), e.authorId());
            }
            case "project.updated" -> {
                ProjectEvent.Updated e = jsonMapper.treeToValue(payloadNode, ProjectEvent.Updated.class);
                yield String.format("Обновлен проект id=%d «%s» (статус: %s, собрано: %s)",
                        e.projectId(), e.title(), e.status(), e.pledged());
            }
            case "project.deleted" -> {
                ProjectEvent.Deleted e = jsonMapper.treeToValue(payloadNode, ProjectEvent.Deleted.class);
                yield String.format("Удален проект id=%d «%s»",
                        e.projectId(), e.title());
            }
            case "pledge.created" -> {
                PledgeEvent.Created e = jsonMapper.treeToValue(payloadNode, PledgeEvent.Created.class);
                yield String.format("Новый взнос id=%d на сумму %s (проект id=%d, статус: %s)",
                        e.pledgeId(), e.amount(), e.projectId(), e.status());
            }
            case "reward.created" -> {
                RewardEvent.Created e = jsonMapper.treeToValue(payloadNode, RewardEvent.Created.class);
                yield String.format("Добавлена награда «%s» (от %s) для проекта id=%d",
                        e.title(), e.minPrice(), e.projectId());
            }
            case "user.created" -> {
                UserEvent.Created e = jsonMapper.treeToValue(payloadNode, UserEvent.Created.class);
                yield String.format("Зарегистрирован пользователь «%s» (email: %s)",
                        e.username(), e.email());
            }
            default -> "Неизвестное событие: " + eventType;
        };
    }
}

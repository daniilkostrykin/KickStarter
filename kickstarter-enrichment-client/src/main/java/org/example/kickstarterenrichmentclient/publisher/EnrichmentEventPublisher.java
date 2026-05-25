package org.example.kickstarterenrichmentclient.publisher;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class EnrichmentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public EnrichmentEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishEnriched(org.example.kickstarter.grpc.ProjectAnalysisResponse response) {
        Map<String, Object> event = new HashMap<>();

        // Метаданные события
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("eventId", UUID.randomUUID().toString());
        metadata.put("eventType", "project.enriched");
        metadata.put("source", "kickstarter-enrichment-client");
        metadata.put("timestamp", Instant.now().toString());

        // Полезная нагрузка (то, что посчитал gRPC сервер)
        Map<String, Object> payload = new HashMap<>();
        payload.put("projectId", response.getProjectId());
        payload.put("successProbability", response.getSuccessProbability());
        payload.put("hypeLevel", response.getHypeLevel());
        payload.put("estimatedBackers", response.getEstimatedBackers());

        event.put("metadata", metadata);
        event.put("payload", payload);

        // Отправляем в наш стандартный обменник
        rabbitTemplate.convertAndSend("kickstarter.events", "project.enriched", event);
        System.out.println("🚀 Аналитика готова! Событие project.enriched улетело в RabbitMQ для ID: " + response.getProjectId());
    }
}
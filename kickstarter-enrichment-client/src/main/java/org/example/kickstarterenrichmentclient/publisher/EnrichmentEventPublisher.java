package org.example.kickstarterenrichmentclient.publisher;

import org.example.kickstartereventscontract.EventEnvelope;
import org.example.kickstartereventscontract.ProjectEvent;
import org.example.kickstartereventscontract.RoutingKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EnrichmentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EnrichmentEventPublisher.class);
    private static final String SOURCE = "grpc-enrichment-client";

    private final RabbitTemplate rabbitTemplate;

    public EnrichmentEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishEnriched(Long projectId, Double successProbability) {
        try {
            ProjectEvent.Enriched event = new ProjectEvent.Enriched(projectId, successProbability);

            EventEnvelope<ProjectEvent> envelope = EventEnvelope.wrap(
                    event, SOURCE, "project.enriched"
            );

            rabbitTemplate.convertAndSend(RoutingKeys.EXCHANGE, "project.enriched", envelope);

            log.info("Событие отправлено: project.enriched [projectId={}]", projectId);
        } catch (Exception e) {
            log.error("Не удалось отправить событие: {}", e.getMessage());
        }
    }
}
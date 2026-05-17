package org.example.kickstarterrest.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kickstarterapicontract.dto.ProjectResponse;
import org.example.kickstartereventscontract.EventEnvelope;
import org.example.kickstartereventscontract.RoutingKeys;
import org.example.kickstartereventscontract.ProjectEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectEventPublisher {

    private static final String SOURCE = "kickstarter-rest";
    private final RabbitTemplate rabbitTemplate;

    public void publishCreated(ProjectResponse project) {
        var event = new ProjectEvent.Created(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getGoal(),
                project.getDeadline(),
                project.getAuthorId()
        );
        send(RoutingKeys.PROJECT_CREATED, event);
    }

    public void publishUpdated(ProjectResponse project) {
        var event = new ProjectEvent.Updated(
                project.getId(),
                project.getTitle(),
                project.getPledged(),
                project.getStatus() != null ? project.getStatus() : "UNKNOWN"
                );
        send(RoutingKeys.PROJECT_UPDATED, event);
    }

    public void publishDeleted(Long projectId, String title) {
        var event = new ProjectEvent.Deleted(
                projectId,
                title
        );
        send(RoutingKeys.PROJECT_DELETED, event);
    }

    private void send(String routingKey, ProjectEvent event) {
        try {
            EventEnvelope<ProjectEvent> envelope = EventEnvelope.wrap(event, SOURCE, routingKey);
            rabbitTemplate.convertAndSend(RoutingKeys.EXCHANGE, routingKey, envelope);
            log.info("Событие отправлено: {} [eventId={}]", routingKey, envelope.metadata().eventId());
        } catch (Exception e) {
            log.error("Не удалось отправить событие {}: {}", routingKey, e.getMessage());
        }
    }
}
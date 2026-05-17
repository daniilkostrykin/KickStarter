package org.example.kickstarterrest.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kickstarterapicontract.dto.UserResponse;
import org.example.kickstartereventscontract.EventEnvelope;
import org.example.kickstartereventscontract.RoutingKeys;
import org.example.kickstartereventscontract.UserEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private static final String SOURCE = "kickstarter-rest";
    private final RabbitTemplate rabbitTemplate;

    public void publishCreated(UserResponse user) {
        var event = new UserEvent.Created(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
        send(RoutingKeys.USER_CREATED, event);
    }

    private void send(String routingKey, UserEvent event) {
        try {
            EventEnvelope<UserEvent> envelope = EventEnvelope.wrap(event, SOURCE, routingKey);
            rabbitTemplate.convertAndSend(RoutingKeys.EXCHANGE, routingKey, envelope);
            log.info("Событие отправлено: {} [eventId={}]", routingKey, envelope.metadata().eventId());
        } catch (Exception e) {
            log.error("Не удалось отправить событие {}: {}", routingKey, e.getMessage());
        }
    }
}
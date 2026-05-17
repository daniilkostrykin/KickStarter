package org.example.kickstarterrest.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kickstarterapicontract.dto.PledgeResponse;
import org.example.kickstartereventscontract.EventEnvelope;
import org.example.kickstartereventscontract.RoutingKeys;
import org.example.kickstartereventscontract.PledgeEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PledgeEventPublisher {

    private static final String SOURCE = "kickstarter-rest";
    private final RabbitTemplate rabbitTemplate;

    public void publishCreated(PledgeResponse pledge) {
        var event = new PledgeEvent.Created(
                pledge.getPledgeId(),
                pledge.getProjectId(),
                pledge.getRewardId(),
                pledge.getUserId(),
                pledge.getAmount(),
                pledge.getStatus() != null ? pledge.getStatus().name() : null,
                pledge.getTransactionDate()
        );
        send(RoutingKeys.PLEDGE_CREATED, event);
    }

    private void send(String routingKey, PledgeEvent event) {
        try {
            EventEnvelope<PledgeEvent> envelope = EventEnvelope.wrap(event, SOURCE, routingKey);
            rabbitTemplate.convertAndSend(RoutingKeys.EXCHANGE, routingKey, envelope);
            log.info("Событие отправлено: {} [eventId={}]", routingKey, envelope.metadata().eventId());
        } catch (Exception e) {
            log.error("Не удалось отправить событие {}: {}", routingKey, e.getMessage());
        }
    }
}
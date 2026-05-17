package org.example.kickstarterrest.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kickstarterapicontract.dto.RewardResponse;
import org.example.kickstartereventscontract.EventEnvelope;
import org.example.kickstartereventscontract.RoutingKeys;
import org.example.kickstartereventscontract.RewardEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RewardEventPublisher {

    private static final String SOURCE = "kickstarter-rest";
    private final RabbitTemplate rabbitTemplate;

    public void publishCreated(RewardResponse reward) {
        var event = new RewardEvent.Created(
                reward.getId(), // если у тебя в DTO другое имя (например, getRewardId), поправь под себя
                reward.getTitle(),
                reward.getDescription(),
                reward.getMinPrice(),
                reward.getProjectId()
        );
        // Если в RoutingKeys у тебя нет константы REWARD_CREATED, можно передать строку "reward.created"
        send(RoutingKeys.REWARD_CREATED, event);
    }

    private void send(String routingKey, RewardEvent event) {
        try {
            EventEnvelope<RewardEvent> envelope = EventEnvelope.wrap(event, SOURCE, routingKey);
            rabbitTemplate.convertAndSend(RoutingKeys.EXCHANGE, routingKey, envelope);
            log.info("Событие отправлено: {} [eventId={}]", routingKey, envelope.metadata().eventId());
        } catch (Exception e) {
            log.error("Не удалось отправить событие {}: {}", routingKey, e.getMessage());
        }
    }
}
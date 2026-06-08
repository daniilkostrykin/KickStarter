package org.example.kickstarterenrichmentclient.listener;

import org.example.kickstarter.grpc.AnalyzeProjectRequest;
import org.example.kickstarter.grpc.ProjectAnalysisResponse;
import org.example.kickstarter.grpc.ProjectAnalyticsGrpc;
import org.example.kickstarterenrichmentclient.publisher.EnrichmentEventPublisher;
import org.example.kickstartereventscontract.EventMetadata;
import org.example.kickstartereventscontract.ProjectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Слушатель событий project.created из RabbitMQ.
 *
 * Десериализация — ручная (как в audit-service), потому что EventEnvelope<T>
 * является generic-типом, и Jackson не может определить конкретный подтип T.
 */
@Component
public class ProjectCreatedListener {

    private static final Logger log = LoggerFactory.getLogger(ProjectCreatedListener.class);

    private final ProjectAnalyticsGrpc.ProjectAnalyticsBlockingStub analyticsStub;
    private final EnrichmentEventPublisher enrichmentPublisher;
    private final JsonMapper jsonMapper;

    public ProjectCreatedListener(ProjectAnalyticsGrpc.ProjectAnalyticsBlockingStub analyticsStub,
                                  EnrichmentEventPublisher enrichmentPublisher,
                                  JsonMapper jsonMapper) {
        this.analyticsStub = analyticsStub;
        this.enrichmentPublisher = enrichmentPublisher;
        this.jsonMapper = jsonMapper;
    }

    /**
     * Обрабатывает событие project.created:
     * 1. Десериализует событие из JSON
     * 2. Формирует gRPC-запрос
     * 3. Вызывает gRPC-сервер (синхронно)
     * 4. Публикует результат как событие project.enriched
     */
    @RabbitListener(queues = "q.enrichment.project-created", messageConverter = "")
    public void handleProjectCreated(Message message) {
        try {
            // 1. Парсим JSON-конверт
            byte[] body = message.getBody();
            JsonNode root = jsonMapper.readTree(body);

            JsonNode metaNode = root.get("metadata");
            EventMetadata metadata = jsonMapper.treeToValue(metaNode, EventMetadata.class);

            JsonNode payloadNode = root.get("payload");
            ProjectEvent.Created projectCreated = jsonMapper.treeToValue(payloadNode, ProjectEvent.Created.class);

            log.info("Получено событие: project.created [id={}, eventId={}]",
                    projectCreated.projectId(), metadata.eventId());

            // 2. Формируем gRPC запрос
            AnalyzeProjectRequest grpcRequest = AnalyzeProjectRequest.newBuilder()
                    .setProjectId(projectCreated.projectId())
                    .setGoal(projectCreated.goal().doubleValue())
                    .setDescriptionLength(projectCreated.description().length())
                    .build();

            // 3. Вызываем gRPC-сервер синхронно
            log.info("Вызов gRPC: ProjectAnalytics.AnalyzeProject(projectId={})", projectCreated.projectId());
            ProjectAnalysisResponse grpcResponse = analyticsStub.analyzeProject(grpcRequest);

            log.info("gRPC ответ получен: projectId={}, вероятность успеха={}%",
                    grpcResponse.getProjectId(),
                    grpcResponse.getSuccessProbability());

            // 4. Публикуем событие project.enriched
            // Мы вызываем метод, который мы создали в EnrichmentEventPublisher
            enrichmentPublisher.publishEnriched(grpcResponse.getProjectId(), grpcResponse.getSuccessProbability());

            log.info("Проект успешно обогащен: projectId={}", projectCreated.projectId());

        } catch (io.grpc.StatusRuntimeException e) {
            log.error("gRPC ошибка при обогащении: {} ({})", e.getStatus().getDescription(), e.getStatus().getCode());
            throw new RuntimeException("gRPC-вызов завершился ошибкой", e);

        } catch (Exception e) {
            log.error("Ошибка обработки события project.created: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось обработать событие", e);
        }
    }
}
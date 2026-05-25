package org.example.kickstarterenrichmentclient.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.kickstarter.grpc.AnalyzeProjectRequest;
import org.example.kickstarter.grpc.ProjectAnalysisResponse;
import org.example.kickstarter.grpc.ProjectAnalyticsGrpc;
import org.example.kickstarterenrichmentclient.publisher.EnrichmentEventPublisher;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.time.Instant;

@Component
public class ProjectCreatedListener {

    private final ProjectAnalyticsGrpc.ProjectAnalyticsBlockingStub analyticsStub;
    private final EnrichmentEventPublisher enrichmentPublisher;
    private final ObjectMapper objectMapper;

    public ProjectCreatedListener(ProjectAnalyticsGrpc.ProjectAnalyticsBlockingStub analyticsStub,
                                  EnrichmentEventPublisher enrichmentPublisher,
                                  ObjectMapper objectMapper) {
        this.analyticsStub = analyticsStub;
        this.enrichmentPublisher = enrichmentPublisher;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "q.enrichment.project-created")
    public void handleProjectCreated(Message message) {
        try {
            // 1. Читаем сырой JSON и выводим в консоль, чтобы видеть реальную картину
            String rawJson = new String(message.getBody());
            System.out.println("====== ПРИЛЕТЕЛ JSON ИЗ REST API ======");
            System.out.println(rawJson);

            JsonNode root = objectMapper.readTree(rawJson);

            // 2. Умный поиск: если есть обертка "payload" — берем ее, если нет — читаем корень
            JsonNode payload = root.has("payload") ? root.get("payload") : root;

            // 3. Безопасное извлечение данных (метод .path() не кидает NullPointerException)
            long projectId = payload.has("id") ? payload.get("id").asLong() : payload.path("projectId").asLong(0);
            double goal = payload.path("goal").asDouble(0.0);
            String description = payload.path("description").asText("");

            // Вычисляем, сколько дней активен проект
            String deadlineStr = payload.path("deadline").asText();
            Instant deadline = Instant.parse(deadlineStr);
            int daysActive = (int) Duration.between(Instant.now(), deadline).toDays();
            daysActive = Math.max(1, daysActive); // Страховка от деления на 0

            // 2. Упаковываем данные в бинарный gRPC-запрос
            AnalyzeProjectRequest grpcRequest = AnalyzeProjectRequest.newBuilder()
                    .setProjectId(projectId)
                    .setGoal(goal)
                    .setDaysActive(daysActive)
                    .setDescriptionLength(description.length())
                    .build();

            // 3. Делаем синхронный gRPC-вызов к нашему серверу-калькулятору
            ProjectAnalysisResponse grpcResponse = analyticsStub.analyzeProject(grpcRequest);

            // 4. Публикуем результат обратно в шину RabbitMQ
            enrichmentPublisher.publishEnriched(grpcResponse);

        } catch (Exception e) {
            e.printStackTrace();
            // Выбрасываем ошибку, чтобы сработал DLQ, если gRPC сервер недоступен
            throw new RuntimeException("Крах при обогащении проекта через gRPC", e);
        }
    }
}
package org.example.kickstartergrpcserver.service;

import io.grpc.stub.StreamObserver;
import org.example.kickstarter.grpc.AnalyzeProjectRequest;
import org.example.kickstarter.grpc.ProjectAnalysisResponse;
import org.example.kickstarter.grpc.ProjectAnalyticsGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Реализация gRPC-сервиса ProjectAnalytics.
 *
 * Наследует сгенерированный базовый класс ProjectAnalyticsImplBase —
 * аналог того, как REST-контроллер реализует интерфейс контракта:
 *
 *   REST:    AuthorController implements AuthorApi
 *   GraphQL: ProjectDataFetcher с @DgsQuery
 *   gRPC:    ProjectAnalyticsServiceImpl extends ProjectAnalyticsGrpc.ProjectAnalyticsImplBase
 *
 * Ключевые отличия от REST/GraphQL:
 * - Бинарный протокол (protobuf) вместо JSON — компактнее и быстрее
 * - Строго типизированный контракт (.proto) — несовместимость обнаруживается при компиляции
 * - HTTP/2 с мультиплексированием — несколько запросов в одном TCP-соединении
 * - Поддержка streaming (server, client, bidirectional) — здесь используем unary (простой запрос-ответ)
 */
public class ProjectAnalyticsServiceImpl extends ProjectAnalyticsGrpc.ProjectAnalyticsImplBase {

    private static final Logger log = LoggerFactory.getLogger(ProjectAnalyticsServiceImpl.class);

    /**
     * Обрабатывает запрос на анализ.
     *
     * Паттерн gRPC: метод получает request и StreamObserver для ответа.
     * StreamObserver — это callback-интерфейс:
     *   - onNext(response) — отправить ответ (для unary RPC вызывается один раз)
     *   - onCompleted()    — завершить RPC
     *   - onError(t)       — сообщить об ошибке
     *
     * Для unary RPC (один запрос → один ответ) всегда:
     *   responseObserver.onNext(response);
     *   responseObserver.onCompleted();
     */
    @Override
    public void analyzeProject(AnalyzeProjectRequest request,
                               StreamObserver<ProjectAnalysisResponse> responseObserver) {

        log.info("gRPC запрос: анализ проекта id={} (цель: {}, дней: {}, длина описания: {})",
                request.getProjectId(), request.getGoal(),
                request.getDaysActive(), request.getDescriptionLength());

        // ─── Вычисление метрик (бизнес-логика) ─────────────
        double chance = calculateSuccessChance(request.getGoal(), request.getDaysActive(), request.getDescriptionLength());
        String hype = determineHypeLevel(chance);
        int estimatedBackers = estimateBackers(request.getGoal());

        // ─── Формируем ответ ─────────────────────────────────────────
        ProjectAnalysisResponse response = ProjectAnalysisResponse.newBuilder()
                .setProjectId(request.getProjectId())
                .setSuccessProbability(chance)
                .setHypeLevel(hype)
                .setEstimatedBackers(estimatedBackers)
                .build();

        log.info("gRPC ответ: проект id={}, шанс успеха={}%, хайп={}, ожидаемое число спонсоров={}",
                response.getProjectId(), chance, hype, estimatedBackers);

        // Отправляем ответ клиенту и завершаем RPC
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // ─── Изолированная бизнес-логика ──────────────────────────────

    /**
     * Расчет базового шанса на успех сбора средств (в процентах).
     */
    private double calculateSuccessChance(double goal, int days, int descLength) {
        // Эвристика: Скорость сбора (сколько нужно собирать в день)
        double velocityRequired = days > 0 ? goal / days : goal;

        double chance = 50.0;

        if (velocityRequired > 100000) {
            chance -= 20;
        } else if (velocityRequired < 10000) {
            chance += 20;
        }

        if (descLength > 500) {
            chance += 15;
        } else if (descLength < 100) {
            chance -= 25;
        }

        return Math.max(5.0, Math.min(95.0, chance));
    }

    /**
     * Определение уровня хайпа на основе шанса успеха.
     */
    private String determineHypeLevel(double chance) {
        if (chance > 70) return "HIGH";
        if (chance > 40) return "MEDIUM";
        return "LOW";
    }

    /**
     * Оценка нужного количества людей (исходя из среднего доната в 1500 руб).
     */
    private int estimateBackers(double goal) {
        return (int) (goal / 1500);
    }
}
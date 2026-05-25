package org.example.kickstartergrpcserver.service;

import io.grpc.stub.StreamObserver;
import org.example.kickstarter.grpc.AnalyzeProjectRequest;
import org.example.kickstarter.grpc.ProjectAnalysisResponse;
import org.example.kickstarter.grpc.ProjectAnalyticsGrpc;

public class ProjectAnalyticsServiceImpl extends ProjectAnalyticsGrpc.ProjectAnalyticsImplBase {

    @Override
    public void analyzeProject(AnalyzeProjectRequest request,
                               StreamObserver<ProjectAnalysisResponse> responseObserver) {

        double goal = request.getGoal();
        int days = request.getDaysActive();
        int descLength = request.getDescriptionLength();

        // 1. Эвристика: Скорость сбора (сколько нужно собирать в день)
        double velocityRequired = days > 0 ? goal / days : goal;

        // 2. Расчет шанса успеха (базовый 50%)
        double chance = 50.0;
        if (velocityRequired > 100000) chance -= 20; // Слишком жадно
        else if (velocityRequired < 10000) chance += 20; // Реалистично

        if (descLength > 500) chance += 15; // Хорошее описание
        else if (descLength < 100) chance -= 25; // Поленился описать

        // Ограничиваем от 5 до 95%
        chance = Math.max(5.0, Math.min(95.0, chance));

        // 3. Уровень хайпа
        String hype = chance > 70 ? "HIGH" : (chance > 40 ? "MEDIUM" : "LOW");

        // 4. Оценка нужного количества людей (допустим, средний донат 1500 руб)
        int estimatedBackers = (int) (goal / 1500);

        // Формируем ответ через Builder
        ProjectAnalysisResponse response = ProjectAnalysisResponse.newBuilder()
                .setProjectId(request.getProjectId())
                .setSuccessProbability(chance)
                .setHypeLevel(hype)
                .setEstimatedBackers(estimatedBackers)
                .build();

        // Отправляем ответ клиенту и закрываем соединение
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
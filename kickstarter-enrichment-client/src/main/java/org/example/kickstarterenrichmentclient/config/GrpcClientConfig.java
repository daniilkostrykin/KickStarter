package org.example.kickstarterenrichmentclient.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.example.kickstarter.grpc.ProjectAnalyticsGrpc;
import jakarta.annotation.PreDestroy;

/**
 * Конфигурация gRPC-клиента — создание канала и стаба.
 * ManagedChannel — соединение с gRPC-сервером. Аналог HttpClient для REST.
 * - Управляет пулом TCP-соединений (HTTP/2 мультиплексирование)
 * - Поддерживает reconnect при обрыве
 * - Требует shutdown при завершении приложения (утечка ресурсов!)
 *
 * BlockingStub — синхронный клиентский стаб. Аналог RestTemplate для REST.
 * - Вызов метода блокирует поток до получения ответа
 * - Подходит для простых unary RPC
 * - Для async используется ProjectAnalyticsGrpc.newFutureStub() или newStub()
 *
 * usePlaintext() — отключает TLS (В проде обязательно TLS с сертификатами).
 * Для лабы упростили...
 */
@Configuration
public class GrpcClientConfig {

    private static final Logger log = LoggerFactory.getLogger(GrpcClientConfig.class);

    @Value("${grpc.client.analytics-server.host:localhost}")
    private String grpcHost;

    @Value("${grpc.client.analytics-server.port:9090}")
    private int grpcPort;

    private ManagedChannel channel;

    /**
     * ManagedChannel — HTTP/2 соединение с gRPC-сервером.
     *
     * Один канал может обслуживать множество одновременных RPC-вызовов
     * благодаря мультиплексированию HTTP/2 (в отличие от HTTP/1.1,
     * где каждый запрос требует отдельного TCP-соединения).
     */
    @Bean
    public ManagedChannel managedChannel() {
        channel = ManagedChannelBuilder
                .forAddress(grpcHost, grpcPort)
                .usePlaintext()  // Без TLS — только для разработки!
                .build();

        log.info("gRPC канал создан: {}:{}", grpcHost, grpcPort);
        return channel;
    }

    /**
     * BlockingStub — синхронный клиент для вызова ProjectAnalytics.AnalyzeProject().
     *
     * Генерируется protoc из .proto файла. Типы аргументов и возвращаемых
     * значений строго соответствуют proto-определению — ошибка типов
     * обнаруживается при компиляции, а не в runtime.
     */
    @Bean
    public ProjectAnalyticsGrpc.ProjectAnalyticsBlockingStub projectAnalyticsStub(ManagedChannel channel) {
        return ProjectAnalyticsGrpc.newBlockingStub(channel);
    }

    /**
     * Парсер JSON. Создаем вручную, так как автоконфигурация Spring
     * почему-то его не подтянула.
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
    /**
     * Корректное закрытие gRPC-канала при остановке приложения.
     * Без этого — утечка TCP-соединений.
     */
    @PreDestroy
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            log.info("Закрытие gRPC канала...");
            channel.shutdown();
        }
    }
}


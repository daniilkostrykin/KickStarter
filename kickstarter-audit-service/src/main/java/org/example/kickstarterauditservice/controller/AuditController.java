package org.example.kickstarterauditservice.controller;

import org.example.kickstarterauditservice.model.AuditEntry;
import org.example.kickstarterauditservice.storage.AuditStorage;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST-контроллер для просмотра журнала аудита.
 * Защищен OAuth2 JWT Resource Server.
 */
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditStorage auditStorage;

    public AuditController(AuditStorage auditStorage) {
        this.auditStorage = auditStorage;
    }

    /**
     * Основной метод: просмотр аудит-лога.
     * Доступен только сервисам с ролью SERVICE (Client Credentials).
     */
    @GetMapping
    @PreAuthorize("hasRole('SERVICE')")
    public Map<String, Object> getAuditLog(
            @RequestParam(defaultValue = "100") int limit) {

        List<AuditEntry> entries = auditStorage.findLatest(limit);

        return Map.of(
                "totalEntries", auditStorage.count(),
                "showing", entries.size(),
                "entries", entries
        );
    }

    /**
     * Служебный метод: диагностика состояния сервиса.
     * Доступен только операторам с ролью OPERATOR.
     */
    @GetMapping("/admin/info")
    @PreAuthorize("hasRole('OPERATOR')")
    public Map<String, String> info() {
        return Map.of(
                "service", "kickstarter-audit-service",
                "status", "ok"
        );
    }
}
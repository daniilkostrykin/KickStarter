package org.example.kickstarterrest.service;

import lombok.RequiredArgsConstructor;
import org.example.kickstarterapicontract.dto.PagedResponse;
import org.example.kickstarterapicontract.dto.PatchProjectRequest;
import org.example.kickstarterapicontract.dto.ProjectRequest;
import org.example.kickstarterapicontract.dto.ProjectResponse;
import org.example.kickstarterrest.exception.ResourceNotFoundException;
import org.example.kickstarterrest.storage.InMemoryStorage;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final InMemoryStorage storage;

    public ProjectResponse findById(Long id) {
        if (!storage.projects.containsKey(id)) throw new ResourceNotFoundException("Проект", id);
        return storage.projects.get(id);
    }

    public PagedResponse<ProjectResponse> findAll(int page, int size) {
        List<ProjectResponse> all = storage.projects.values().stream()
                .sorted(Comparator.comparingLong(ProjectResponse::getId))
                .toList();
        int totalElements = all.size();
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        int from = page * size;
        int to = Math.min(from + size, totalElements);
        List<ProjectResponse> content = (from >= totalElements) ? List.of() : all.subList(from, to);
        return new PagedResponse<>(content, page, size, totalElements, totalPages, page >= totalPages - 1);

    }

    public ProjectResponse create(ProjectRequest request) {
        long id = storage.projectSequence.incrementAndGet();
        ProjectResponse project = ProjectResponse.builder()
                .id(id).title(request.title()).description(request.description())
                .goal(request.goal()).pledged(new BigDecimal("0"))
                .status("DRAFT").deadline(request.deadline()).build();
        storage.projects.put(id, project);
        return project;
    }

    public ProjectResponse patch(Long id, PatchProjectRequest request) {
        ProjectResponse existing = findById(id);
        ProjectResponse updated = ProjectResponse.builder()
                .id(existing.getId())
                .title(request.title() != null ? request.title() : existing.getTitle())
                .description(request.description() != null ? request.description() : existing.getDescription())
                .goal(existing.getGoal()).pledged(existing.getPledged())
                .status(request.status() != null ? request.status() : existing.getStatus())
                .deadline(existing.getDeadline()).build();
        storage.projects.put(id, updated);
        return updated;
    }

    public void addPledgedAmount(Long id, BigDecimal amount) {
        ProjectResponse existing = findById(id);
        ProjectResponse updated = ProjectResponse.builder()
                .id(existing.getId()).title(existing.getTitle()).description(existing.getDescription())
                .goal(existing.getGoal()).status(existing.getStatus()).deadline(existing.getDeadline())
                .pledged(existing.getPledged().add(amount)).build();
        storage.projects.put(id, updated);
    }
}
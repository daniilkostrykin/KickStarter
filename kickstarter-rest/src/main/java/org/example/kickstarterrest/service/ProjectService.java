package org.example.kickstarterrest.service;

import lombok.RequiredArgsConstructor;
import org.example.kickstarterapicontract.dto.PagedResponse;
import org.example.kickstarterapicontract.dto.PatchProjectRequest;
import org.example.kickstarterapicontract.dto.ProjectRequest;
import org.example.kickstarterapicontract.dto.ProjectResponse;
import org.example.kickstarterrest.entity.ProjectEntity;
import org.example.kickstarterrest.entity.UserEntity;
import org.example.kickstarterrest.event.ProjectEventPublisher;
import org.example.kickstarterrest.exception.DuplicateResourceException;
import org.example.kickstarterrest.exception.ResourceNotFoundException;
import org.example.kickstarterrest.repository.ProjectRepository;
import org.example.kickstarterrest.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public ProjectResponse findById(Long id) {
        return projectRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Проект", id));
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProjectResponse> findAll(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<ProjectEntity> entityPage = projectRepository.findAll(pageRequest);

        return new PagedResponse<>(
                entityPage.getContent().stream().map(this::toResponse).toList(),
                entityPage.getNumber(),
                entityPage.getSize(),
                entityPage.getTotalElements(),
                entityPage.getTotalPages(),
                entityPage.isLast()
        );
    }

    @Transactional
    public ProjectResponse create(ProjectRequest request) {
        if (projectRepository.existsByTitle(request.title())) {
            throw new DuplicateResourceException("Проект с названием '" + request.title() + "' уже существует");
        }

        UserEntity author = userRepository.findById(request.authorId())
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", request.authorId()));

        ProjectEntity project = ProjectEntity.builder()
                .title(request.title())
                .description(request.description())
                .goal(request.goal())
                .pledged(BigDecimal.ZERO)
                .status("DRAFT")
                .deadline(request.deadline())
                .author(author)
                .build();

        ProjectEntity saved = projectRepository.save(project);
        ProjectResponse response = toResponse(saved);
        eventPublisher.publishCreated(response);
        return response;
    }

    @Transactional
    public ProjectResponse patch(Long id, PatchProjectRequest request) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Проект", id));

        if (request.title() != null) {
            project.setTitle(request.title());
        }
        if (request.description() != null) {
            project.setDescription(request.description());
        }
        if (request.status() != null) {
            project.setStatus(request.status());
        }

        ProjectEntity updated = projectRepository.save(project);
        ProjectResponse response = toResponse(updated);
        eventPublisher.publishUpdated(response);
        return response;
    }

    @Transactional
    public void delete(Long id) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Проект", id));
        String titleForLog = project.getTitle();
        projectRepository.delete(project);
        eventPublisher.publishDeleted(id, titleForLog);
    }

    @Transactional
    public void addPledgedAmount(Long id, BigDecimal amount) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Проект", id));
        project.setPledged(project.getPledged().add(amount));
        projectRepository.save(project);
    }

    public ProjectResponse toResponse(ProjectEntity entity) {
        return ProjectResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .goal(entity.getGoal())
                .pledged(entity.getPledged())
                .status(entity.getStatus())
                .deadline(entity.getDeadline())
                .authorId(entity.getAuthor().getId())
                .build();
    }
}
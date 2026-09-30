package org.example.kickstarterrest.service;

import lombok.RequiredArgsConstructor;
import org.example.kickstarterapicontract.dto.PagedResponse;
import org.example.kickstarterapicontract.dto.UserRequest;
import org.example.kickstarterapicontract.dto.UserResponse;
import org.example.kickstarterrest.entity.UserEntity;
import org.example.kickstarterrest.event.UserEventPublisher;
import org.example.kickstarterrest.exception.DuplicateResourceException;
import org.example.kickstarterrest.exception.ResourceNotFoundException;
import org.example.kickstarterrest.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> findAll(String usernameSearch, String emailSearch, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<UserEntity> entityPage = userRepository.searchUsers(usernameSearch, emailSearch, pageRequest);

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
    public UserResponse create(UserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Пользователь с никнеймом '" + request.username() + "' уже существует");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Пользователь с почтой '" + request.email() + "' уже существует");
        }

        UserEntity entity = UserEntity.builder()
                .username(request.username())
                .email(request.email())
                .build();

        UserEntity saved = userRepository.save(entity);
        UserResponse response = toResponse(saved);
        eventPublisher.publishCreated(response);
        return response;
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return userRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", id));
    }

    public UserResponse toResponse(UserEntity entity) {
        return UserResponse.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .build();
    }
}
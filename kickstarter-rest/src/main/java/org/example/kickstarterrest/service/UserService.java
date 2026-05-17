package org.example.kickstarterrest.service;

import lombok.RequiredArgsConstructor;
import org.example.kickstarterapicontract.dto.PagedResponse;
import org.example.kickstarterapicontract.dto.UserRequest;
import org.example.kickstarterapicontract.dto.UserResponse;
import org.example.kickstarterrest.event.UserEventPublisher;
import org.example.kickstarterrest.exception.ResourceNotFoundException;
import org.example.kickstarterrest.storage.InMemoryStorage;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final InMemoryStorage storage;
    private final UserEventPublisher eventPublisher;

    public PagedResponse<UserResponse> findAll(String usernameSearch, String emailSearch, int page, int size) {
        java.util.stream.Stream<UserResponse> stream = storage.users.values().stream()
                .sorted(java.util.Comparator.comparingLong(UserResponse::getId));

        if (usernameSearch != null && !usernameSearch.isBlank()) {
            String q = usernameSearch.toLowerCase();
            stream = stream.filter(u -> u.getUsername() != null && u.getUsername().toLowerCase().contains(q));
        }

        if (emailSearch != null && !emailSearch.isBlank()) {
            String q = emailSearch.toLowerCase();
            stream = stream.filter(u -> u.getEmail() != null && u.getEmail().toLowerCase().contains(q));
        }

        java.util.List<UserResponse> allUsers = stream.toList();

        int totalElements = allUsers.size();
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;

        int from = page * size;
        int to = Math.min(from + size, totalElements);

        java.util.List<UserResponse> content = (from >= totalElements) ? java.util.List.of() : allUsers.subList(from, to);

        return new PagedResponse<>(content, page, size, totalElements, totalPages, page >= totalPages - 1);
    }

    public UserResponse create(UserRequest request) {
        Long id = storage.userSequence.incrementAndGet();
        UserResponse user = UserResponse.builder()
                .id(id)
                .username(request.username())
                .email(request.email())
                .build();

        storage.users.put(id, user);
        eventPublisher.publishCreated(user);
        return user;
    }

    public UserResponse findById(Long id) {
        if (!storage.users.containsKey(id)) {
            throw new ResourceNotFoundException("Пользователь", id);
        }
        return storage.users.get(id);
    }
}
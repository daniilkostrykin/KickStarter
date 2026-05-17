package org.example.kickstartereventscontract;

public sealed interface UserEvent {

    record Created(
            Long userId,
            String username,
            String email
    ) implements UserEvent {}

}
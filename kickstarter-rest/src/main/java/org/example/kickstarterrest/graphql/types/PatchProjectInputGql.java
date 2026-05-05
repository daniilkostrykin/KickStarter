package org.example.kickstarterrest.graphql.types;

public record PatchProjectInputGql(
        String title,
        String description,
        String status) {}

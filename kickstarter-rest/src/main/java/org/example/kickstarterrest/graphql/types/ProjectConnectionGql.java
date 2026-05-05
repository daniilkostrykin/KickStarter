package org.example.kickstarterrest.graphql.types;

import org.example.kickstarterapicontract.dto.ProjectResponse;

import java.util.List;

public record ProjectConnectionGql(
        List<ProjectResponse> content,
        PageInfoGql pageInfo,
        int totalElements
) {}
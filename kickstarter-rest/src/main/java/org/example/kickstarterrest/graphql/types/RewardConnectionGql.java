package org.example.kickstarterrest.graphql.types;

import org.example.kickstarterapicontract.dto.RewardResponse;

import java.util.List;

public record RewardConnectionGql(
        List<RewardResponse> content,
        PageInfoGql pageInfo,
        int totalElements
) {}

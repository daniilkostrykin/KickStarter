package org.example.kickstarterrest.graphql.types;

public record CreatePledgeInputGql(
        String projectId,
        String rewardId,
        java.math.BigDecimal pledge,
        String userId) {}
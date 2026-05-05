package org.example.kickstarterrest.graphql.types;

import org.example.kickstarterapicontract.dto.PledgeResponse;

import java.util.List;

public record PledgeConnectionGql(
        List<PledgeResponse> content,
        PageInfoGql pageInfo,
        Integer totalElements) {
}

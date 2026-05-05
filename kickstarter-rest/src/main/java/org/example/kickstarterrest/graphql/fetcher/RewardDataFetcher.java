package org.example.kickstarterrest.graphql.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import lombok.RequiredArgsConstructor;
import org.example.kickstarterapicontract.dto.RewardRequest;
import org.example.kickstarterapicontract.dto.RewardResponse;
import org.example.kickstarterrest.graphql.types.CreateRewardInputGql;
import org.example.kickstarterrest.service.RewardService;
import java.util.List;

@DgsComponent
@RequiredArgsConstructor
public class RewardDataFetcher {
    private final RewardService rewardService;

    @DgsQuery
    public RewardResponse reward(@InputArgument String id) {
        return rewardService.findById(Long.parseLong(id));
    }

    @DgsQuery
    public List<RewardResponse> rewards() {
        return rewardService.findAll();
    }

    @DgsMutation
    public RewardResponse createReward(@InputArgument CreateRewardInputGql input) {
        RewardRequest request = new RewardRequest(input.title(), input.description(), input.minPrice(), Long.parseLong(input.projectId()));
        return rewardService.create(request);
    }
}
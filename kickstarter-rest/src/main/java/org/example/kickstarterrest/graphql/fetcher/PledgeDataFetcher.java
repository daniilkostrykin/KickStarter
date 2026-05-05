package org.example.kickstarterrest.graphql.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import lombok.RequiredArgsConstructor;
import org.example.kickstarterapicontract.dto.PledgeRequest;
import org.example.kickstarterapicontract.dto.PledgeResponse;
import org.example.kickstarterrest.graphql.types.CreatePledgeInputGql;
import org.example.kickstarterrest.service.PledgeService;
import java.util.List;

@DgsComponent
@RequiredArgsConstructor
public class PledgeDataFetcher {
    private final PledgeService pledgeService;

    @DgsQuery
    public PledgeResponse pledge(@InputArgument String id) {
        return pledgeService.findById(Long.parseLong(id));
    }

    @DgsQuery
    public List<PledgeResponse> pledges() {
        return pledgeService.findAll();
    }

    @DgsMutation
    public PledgeResponse makePledge(@InputArgument CreatePledgeInputGql input) {
        PledgeRequest request = new PledgeRequest(Long.parseLong(input.projectId()), Long.parseLong(input.rewardId()), input.pledge());
        return pledgeService.create(request);
    }
}
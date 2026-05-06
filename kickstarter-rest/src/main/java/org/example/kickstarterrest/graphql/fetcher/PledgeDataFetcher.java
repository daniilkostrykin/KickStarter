package org.example.kickstarterrest.graphql.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import lombok.RequiredArgsConstructor;
import org.example.kickstarterapicontract.dto.PagedResponse;
import org.example.kickstarterapicontract.dto.PledgeRequest;
import org.example.kickstarterapicontract.dto.PledgeResponse;
import org.example.kickstarterapicontract.dto.UserResponse;
import org.example.kickstarterrest.graphql.types.CreatePledgeInputGql;
import org.example.kickstarterrest.graphql.types.PageInfoGql;
import org.example.kickstarterrest.graphql.types.PledgeConnectionGql;
import org.example.kickstarterrest.service.PledgeService;
import org.example.kickstarterrest.service.UserService;

import java.math.BigDecimal;

@DgsComponent
@RequiredArgsConstructor
public class PledgeDataFetcher {

    private final PledgeService pledgeService;
    private final UserService userService;

    @DgsQuery
    public PledgeResponse pledge(@InputArgument String id) {
        return pledgeService.findById(Long.parseLong(id));
    }

    @DgsQuery
    public PledgeConnectionGql pledges(
            @InputArgument Integer page,
            @InputArgument Integer size) {

        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;

        PagedResponse<PledgeResponse> paged = pledgeService.findAllPledges(null, pageNum, pageSize);

        return new PledgeConnectionGql(
                paged.content(),
                new PageInfoGql(paged.pageNumber(), paged.pageSize(), paged.totalPages(), paged.last()),
                (int) paged.totalElements()
        );
    }

    @DgsMutation
    public PledgeResponse makePledge(@InputArgument CreatePledgeInputGql input) {
        PledgeRequest request = new PledgeRequest(
                Long.parseLong(input.projectId()),
                Long.parseLong(input.rewardId()),
                input.pledge(),
                Long.parseLong(input.userId())
        );
        return pledgeService.create(request);
    }

    @DgsData(parentType = "Pledge", field = "sponsor")
    public UserResponse sponsor(DgsDataFetchingEnvironment dfe) {
        PledgeResponse pledge = dfe.getSource();
        if (pledge == null || pledge.getUserId() == null) {
            return null;
        }
        return userService.findById(pledge.getUserId());
    }
}
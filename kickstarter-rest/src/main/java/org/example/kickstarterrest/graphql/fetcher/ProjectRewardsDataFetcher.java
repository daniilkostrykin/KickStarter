package org.example.kickstarterrest.graphql.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.InputArgument;
import org.example.kickstarterapicontract.dto.PagedResponse;
import org.example.kickstarterapicontract.dto.ProjectResponse;
import org.example.kickstarterapicontract.dto.RewardResponse;
import org.example.kickstarterrest.graphql.types.PageInfoGql;
import org.example.kickstarterrest.graphql.types.RewardConnectionGql;
import org.example.kickstarterrest.service.RewardService;

@DgsComponent
public class ProjectRewardsDataFetcher {

    private final RewardService rewardService;

    public ProjectRewardsDataFetcher(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @DgsData(parentType = "Project", field = "rewards")
    public RewardConnectionGql rewards(
            DgsDataFetchingEnvironment dfe,
            @InputArgument Integer page,
            @InputArgument Integer size) {

        ProjectResponse project = dfe.getSource();

        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;

        PagedResponse<RewardResponse> paged = rewardService.findAllRewards(
                project.getId(), null, null, pageNum, pageSize);

        return new RewardConnectionGql(
                paged.content(),
                new PageInfoGql(paged.pageNumber(), paged.pageSize(), paged.totalPages(), paged.last()),
                (int) paged.totalElements());
    }
}

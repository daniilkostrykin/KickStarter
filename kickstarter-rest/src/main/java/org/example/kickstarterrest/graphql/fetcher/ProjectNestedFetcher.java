package org.example.kickstarterrest.graphql.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import org.example.kickstarterapicontract.dto.ProjectResponse;
import org.example.kickstarterapicontract.dto.RewardResponse;
import org.example.kickstarterrest.service.RewardService;

import java.util.List;
import java.util.stream.Collectors;

@DgsComponent
@RequiredArgsConstructor
public class ProjectNestedFetcher {

    private final RewardService rewardService;

    @DgsData(parentType = "Project", field = "rewards")
    public List<RewardResponse> getRewardsForProject(DgsDataFetchingEnvironment dfe) {
        ProjectResponse project = dfe.getSource();

        return rewardService.findAll().stream()
                .filter(reward -> reward.getProjectId().equals(project.getId()))
                .collect(Collectors.toList());
    }
}
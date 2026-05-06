package org.example.kickstarterrest.graphql.fetcher;

import com.netflix.graphql.dgs.*;
import lombok.RequiredArgsConstructor;
import org.example.kickstarterapicontract.dto.ProjectResponse;
import org.example.kickstarterapicontract.dto.UserRequest;
import org.example.kickstarterapicontract.dto.UserResponse;
import org.example.kickstarterrest.service.PledgeService;
import org.example.kickstarterrest.service.ProjectService;
import org.example.kickstarterrest.service.UserService;

import java.util.List;

@DgsComponent
@RequiredArgsConstructor
public class UserDataFetcher {

    private final UserService userService;
    private final PledgeService pledgeService;
    private final ProjectService projectService;

    @DgsQuery
    public UserResponse user(@InputArgument Long id) {
        return userService.findById(id);
    }

    @DgsMutation
    public UserResponse registerUser(@InputArgument("input") UserRequest input) {
        return userService.create(input);
    }

    @DgsData(parentType = "User", field = "backedProjects")
    public List<ProjectResponse> backedProjects(DgsDataFetchingEnvironment dfe) {
        UserResponse user = dfe.getSource();

        return pledgeService.findAllPledges(null, 0, 1000).content().stream()
                .filter(pledge -> pledge.getUserId().equals(user.getId()))
                .map(pledge -> projectService.findById(pledge.getProjectId()))
                .distinct()
                .toList();
    }
}
package org.example.kickstarterrest.graphql.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import graphql.relay.PageInfo;
import lombok.RequiredArgsConstructor;
import org.example.kickstarterapicontract.dto.PagedResponse;
import org.example.kickstarterapicontract.dto.PatchProjectRequest;
import org.example.kickstarterapicontract.dto.ProjectRequest;
import org.example.kickstarterapicontract.dto.ProjectResponse;
import org.example.kickstarterrest.graphql.types.CreateProjectInputGql;
import org.example.kickstarterrest.graphql.types.PageInfoGql;
import org.example.kickstarterrest.graphql.types.PatchProjectInputGql;
import org.example.kickstarterrest.graphql.types.ProjectConnectionGql;
import org.example.kickstarterrest.service.ProjectService;
import java.util.List;

@DgsComponent
@RequiredArgsConstructor
public class ProjectDataFetcher {
    private final ProjectService projectService;

    @DgsQuery
    public ProjectResponse project(@InputArgument String id) {
        return projectService.findById(Long.parseLong(id));
    }

    @DgsQuery
    public ProjectConnectionGql projects(
            @InputArgument Integer page,
            @InputArgument Integer size) {

        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;

        PagedResponse<ProjectResponse> paged = projectService.findAll(pageNum, pageSize);

        return new ProjectConnectionGql(
                paged.content(),
                new PageInfoGql(paged.pageNumber(), paged.pageSize(), paged.totalPages(), paged.last()),
                (int) paged.totalElements());

    }

    @DgsMutation
    public ProjectResponse createProject(@InputArgument CreateProjectInputGql input) {
        ProjectRequest request = new ProjectRequest(input.title(), input.description(), input.goal(), input.deadline());
        return projectService.create(request);
    }

    @DgsMutation
    public ProjectResponse updateProject(@InputArgument String id, @InputArgument PatchProjectInputGql input) {
        PatchProjectRequest request = new PatchProjectRequest(input.title(), input.description(), input.status());
        return projectService.patch(Long.parseLong(id), request);
    }
}
package org.example.kickstarterrest.graphql.fetcher;

import com.netflix.graphql.dgs.*;
import lombok.RequiredArgsConstructor;
import org.example.kickstarterapicontract.dto.*;
import org.example.kickstarterrest.graphql.types.CreateProjectInputGql;
import org.example.kickstarterrest.graphql.types.PageInfoGql;
import org.example.kickstarterrest.graphql.types.PatchProjectInputGql;
import org.example.kickstarterrest.graphql.types.ProjectConnectionGql;
import org.example.kickstarterrest.service.ProjectService;
import org.example.kickstarterrest.service.UserService;

@DgsComponent
@RequiredArgsConstructor
public class ProjectDataFetcher {
    private final ProjectService projectService;
    private final UserService userService;

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
        ProjectRequest request = new ProjectRequest(
                input.title(),
                input.description(),
                input.goal(),
                input.deadline(),
                Long.parseLong(input.authorId()));

        return projectService.create(request);
    }

    @DgsMutation
    public ProjectResponse updateProject(@InputArgument String id, @InputArgument PatchProjectInputGql input) {
        PatchProjectRequest request = new PatchProjectRequest(input.title(), input.description(), input.status());
        return projectService.patch(Long.parseLong(id), request);
    }

    @DgsData(parentType = "Project", field = "author")
    public UserResponse author(DgsDataFetchingEnvironment dfe) {
        ProjectResponse project = dfe.getSource();

        if (project.getAuthorId() == null) {
            System.out.println("Ошибка: authorId в проекте равен null!");
            return null;
        }

        UserResponse user = userService.findById(project.getAuthorId());

        return user;
    }
}
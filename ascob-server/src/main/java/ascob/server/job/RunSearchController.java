package ascob.server.job;

import ascob.job.SearchRunRequest;
import ascob.job.SearchRunResponse;
import ascob.security.Permission;
import ascob.server.ApiInfo;
import ascob.server.security.NotAuthorizedException;
import ascob.server.security.SecurityAssertionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search/runs")
@SecurityRequirements({@SecurityRequirement(name = ApiInfo.API_TOKEN_SCHEMA_NAME)})
public class RunSearchController {

    @Autowired
    SecurityAssertionService securityAssertionService;

    @Autowired
    JobService jobService;

    @Operation(description = "Search job runs")
    @ApiResponses(
            {
                    @ApiResponse( responseCode = "200", description = "Search results"),
                    @ApiResponse( responseCode = "403", description = "Not authorized", content = @Content(schema = @Schema()))
            }
    )
    @RequestMapping(method = RequestMethod.POST)
    public SearchRunResponse search(@RequestBody SearchRunRequest request, Authentication authentication) throws NotAuthorizedException {
        securityAssertionService.assertAuthorized(authentication, Permission.job_run_search);
        SearchRunResponse response = new SearchRunResponse();
        response.setItems(jobService.search(request, request.getMaxResults()));
        return response;
    }
}

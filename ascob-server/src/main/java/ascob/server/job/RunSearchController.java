package ascob.server.job;

import ascob.job.SearchRunRequest;
import ascob.job.SearchRunResponse;
import ascob.security.Permission;
import ascob.server.security.NotAuthorizedException;
import ascob.server.security.SecurityAssertionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search/runs")
public class RunSearchController {

    @Autowired
    SecurityAssertionService securityAssertionService;

    @Autowired
    JobService jobService;

    @RequestMapping(method = RequestMethod.POST)
    public SearchRunResponse search(@RequestBody SearchRunRequest request, Authentication authentication) throws NotAuthorizedException {
        securityAssertionService.assertAuthorized(authentication, Permission.job_run_search);
        SearchRunResponse response = new SearchRunResponse();
        response.setItems(jobService.search(request, request.getMaxResults()));
        return response;
    }
}

package ascob.server.job;

import ascob.security.Permission;
import ascob.server.security.NotAuthorizedException;
import ascob.server.security.SecurityAssertionService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ascob.api.RunInfo;
import ascob.api.job.SubmitRequest;
import ascob.api.job.SubmitResponse;
import ascob.server.backend.ExecutionBackendException;

import java.io.IOException;

@RestController
@RequestMapping("/api/runs")
public class RunController {

	@Autowired
	SecurityAssertionService securityAssertionService;
	@Autowired
	JobService jobService;
	
	@RequestMapping(method=RequestMethod.POST)
	public SubmitResponse submit (@RequestBody SubmitRequest request, Authentication authentication) throws ExecutionBackendException, NotAuthorizedException {
		securityAssertionService.assertAuthorized(authentication, Permission.job_submit);
		Long runId=jobService.submit(request.getJobSpec());
		SubmitResponse response = new SubmitResponse();
		response.setRunId(runId);
		return response;
	}
	
	
	@RequestMapping(method=RequestMethod.GET, path = "/{runId}")
	public RunInfo getRunInfo(@PathVariable("runId") Long runId, Authentication authentication) throws NotAuthorizedException {
		securityAssertionService.assertAuthorized(authentication, Permission.job_run_read);
		return jobService.getRunInfo(runId);
	}

	@RequestMapping(method=RequestMethod.GET, path = "/{runId}/refresh")
	public RunInfo refreshRunInfo(@PathVariable("runId") Long runId,Authentication authentication) throws ExecutionBackendException, NotAuthorizedException {
		securityAssertionService.assertAuthorized(authentication, Permission.job_run_refresh);
		return jobService.refresh(runId);
	}

	@RequestMapping(method=RequestMethod.GET, path = "/{runId}/output.txt")
	public void getRunOutput(@PathVariable("runId") Long runId, HttpServletResponse response, Authentication authentication) throws ExecutionBackendException, IOException, NotAuthorizedException {
		securityAssertionService.assertAuthorized(authentication, Permission.job_run_output);
		response.setContentType("text/plain");
		jobService.writeRunOutputInto(runId, response.getOutputStream());
	}
}

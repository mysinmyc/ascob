package ascob.server.job;

import ascob.job.RunInfo;
import ascob.job.SubmitRequest;
import ascob.job.SubmitResponse;
import ascob.security.Permission;
import ascob.server.backend.ExecutionBackendException;
import ascob.server.security.NotAuthorizedException;
import ascob.server.security.SecurityAssertionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/runs")
public class RunController {

	@Autowired
	SecurityAssertionService securityAssertionService;
	@Autowired
	JobService jobService;
	
	@RequestMapping(method=RequestMethod.POST)
	public SubmitResponse submit(@RequestBody SubmitRequest request, Authentication authentication) throws ExecutionBackendException, NotAuthorizedException, InvalidJobSpecException {
		securityAssertionService.assertAuthorized(authentication, Permission.job_submit);
		if (request.getJobSpec().isManualStart()) {
			securityAssertionService.assertAuthorized(authentication, Permission.job_run_manual_start);
		}
		Long runId=jobService.submit(request.getJobSpec());
		SubmitResponse response = new SubmitResponse();
		response.setRunId(runId);
		return response;
	}
	@RequestMapping(method=RequestMethod.GET, path = "/{runId}/resubmit")
	public SubmitResponse resubmit(@PathVariable("runId") Long sourceRunId, @RequestParam("submitter") String submitter, Authentication authentication) throws NotAuthorizedException, InvalidJobSpecException {
		securityAssertionService.assertAuthorized(authentication, Permission.job_run_manual_resubmit);
		Long runId=jobService.resubmitBy(sourceRunId, submitter);
		SubmitResponse response = new SubmitResponse();
		response.setRunId(runId);
		return response;
	}
	
	@RequestMapping(method=RequestMethod.GET, path = "/{runId}")
	public RunInfo getRunInfo(@PathVariable("runId") Long runId, Authentication authentication) throws NotAuthorizedException {
		securityAssertionService.assertAuthorized(authentication, Permission.job_run_read);
		return jobService.getRunInfo(runId);
	}

	@RequestMapping(method=RequestMethod.GET, path = "/{runId}/start")
	public void start(@PathVariable("runId") Long runId, Authentication authentication) throws NotAuthorizedException {
		securityAssertionService.assertAuthorized(authentication, Permission.job_run_manual_start);

		if (!jobService.start(runId)) {
			throw new RuntimeException("Job already started");
		}
	}

	@RequestMapping(method=RequestMethod.POST,  path = "/{runId}/files/{fileId}")
	public void upload(@PathVariable("runId") Long runId, @PathVariable("fileId") String fileId, HttpServletRequest request, Authentication authentication) throws NotAuthorizedException, IOException {
		securityAssertionService.assertAuthorized(authentication, Permission.job_run_upload_files);
		try (InputStream inputStream = request.getInputStream()) {
			jobService.uploadFile(runId, fileId, inputStream);
		}
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

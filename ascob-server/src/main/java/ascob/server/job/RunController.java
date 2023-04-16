package ascob.server.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ascob.api.RunInfo;
import ascob.api.job.SubmitRequest;
import ascob.api.job.SubmitResponse;
import ascob.server.backend.ExecutionBackendException;

@RestController
@RequestMapping("/api/runs")
public class RunController {

	@Autowired
	JobService jobService;
	
	@RequestMapping(method=RequestMethod.POST)
	public SubmitResponse submit (@RequestBody SubmitRequest request) throws ExecutionBackendException {
		Long runId=jobService.submit(request.getJobSpec());
		SubmitResponse response = new SubmitResponse();
		response.setRunId(runId);
		return response;
	}
	
	
	@RequestMapping(method=RequestMethod.GET, path = "/{runId}")
	public RunInfo getRunInfo (@PathVariable("runId") Long runId) {
		return jobService.getRunInfo(runId);
	}

	@RequestMapping(method=RequestMethod.GET, path = "/{runId}/refresh")
	public RunInfo refreshRunInfo(@PathVariable("runId") Long runId) throws ExecutionBackendException {
		return jobService.refresh(runId);
	}
}

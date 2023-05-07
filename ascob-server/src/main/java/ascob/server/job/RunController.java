package ascob.server.job;

import ascob.job.RunInfo;
import ascob.job.SubmitRequest;
import ascob.job.SubmitResponse;
import ascob.security.Permission;
import ascob.server.ApiInfo;
import ascob.server.backend.ExecutionBackendException;
import ascob.server.security.NotAuthorizedException;
import ascob.server.security.SecurityAssertionService;
import ascob.server.util.ErrorPayload;
import ascob.server.util.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;

@RestController
@SecurityRequirements({@SecurityRequirement(name = ApiInfo.API_TOKEN_SCHEMA_NAME)})
@RequestMapping("/api/runs")
public class RunController {

	@Autowired
	SecurityAssertionService securityAssertionService;
	@Autowired
	JobService jobService;
	
	@Operation(description = "Submit a job")
	@ApiResponses(
			{
					@ApiResponse(responseCode = "200", description = "Job submitted"),
					@ApiResponse(responseCode = "400", description = "Invalid job specs", content = @Content(schema = @Schema(implementation = ErrorPayload.class))),
					@ApiResponse(responseCode = "403", description = "User not authorized", content = @Content(schema = @Schema())),}
	)
	@RequestMapping(method=RequestMethod.POST)
	public SubmitResponse submit(@RequestBody SubmitRequest request, Authentication authentication) throws NotAuthorizedException, InvalidJobSpecException {
		securityAssertionService.assertAuthorized(authentication, Permission.job_submit);
		if (request.getJobSpec().isManualStart()) {
			securityAssertionService.assertAuthorized(authentication, Permission.job_run_manual_start);
		}
		Long runId=jobService.submit(request.getJobSpec());
		SubmitResponse response = new SubmitResponse();
		response.setRunId(runId);
		return response;
	}

	@Operation(description = "Resubmit a run. It create a new job with the same spec of the source run")
	@ApiResponses(
			{
					@ApiResponse(responseCode = "200", description = "Job resubmitted"),
					@ApiResponse(responseCode = "400", description = "Invalid job specs", content = @Content(schema = @Schema(implementation = ErrorPayload.class))),
					@ApiResponse(responseCode = "403", description = "User not authorized", content = @Content(schema = @Schema())),
					@ApiResponse(responseCode = "404", description = "Run not found", content = @Content(schema = @Schema()))
			}
	)
	@RequestMapping(method=RequestMethod.GET, path = "/{runId}/resubmit")
	public SubmitResponse resubmit(@Parameter(description = "Id of run to resubmit") @PathVariable("runId") Long sourceRunId, @Parameter(description="New submitter") @RequestParam("submitter") String submitter, Authentication authentication) throws NotAuthorizedException, InvalidJobSpecException, RunNotFoundException {
		securityAssertionService.assertAuthorized(authentication, Permission.job_run_resubmit);
		Long runId=jobService.resubmitBy(sourceRunId, submitter);
		SubmitResponse response = new SubmitResponse();
		response.setRunId(runId);
		return response;
	}

	@Operation(description = "Get status of run")
	@ApiResponses(
			{
					@ApiResponse(responseCode = "200", description = "Job submitted"),
					@ApiResponse(responseCode = "403", description = "User not authorized", content = @Content(schema = @Schema())),
					@ApiResponse(responseCode = "404", description = "Run not found", content = @Content(schema = @Schema()))
			}
	)
	@RequestMapping(method=RequestMethod.GET, path = "/{runId}")
	public RunInfo getRunInfo(@Parameter(description = "Run id") @PathVariable("runId") Long runId, Authentication authentication) throws NotAuthorizedException, RunNotFoundException {
		securityAssertionService.assertAuthorized(authentication, Permission.job_run_read);
		return jobService.getRunInfo(runId);
	}

	@Operation(description = "Start a run. It works only for job defined ad manual start")
	@ApiResponses(
			{
					@ApiResponse(responseCode = "200", description = "Run started"),
					@ApiResponse(responseCode = "403", description = "User not authorized", content = @Content(schema = @Schema())),
					@ApiResponse(responseCode = "404", description = "Run not found", content = @Content(schema = @Schema()))
			}
	)
	@RequestMapping(method=RequestMethod.GET, path = "/{runId}/start")
	public void start(@Parameter(description = "Run id") @PathVariable("runId") Long runId, Authentication authentication) throws NotAuthorizedException, RunNotFoundException {
		securityAssertionService.assertAuthorized(authentication, Permission.job_run_manual_start);

		if (!jobService.start(runId)) {
			throw new RuntimeException("Job already started");
		}
	}

	@Operation(description = "Stop a run")
	@ApiResponses(
			{
					@ApiResponse(responseCode = "200", description = "Run aborted"),
					@ApiResponse(responseCode = "403", description = "User not authorized", content = @Content(schema = @Schema())),
					@ApiResponse(responseCode = "404", description = "Run not found", content = @Content(schema = @Schema())),
					@ApiResponse(responseCode = "500", description = "An error occurred during stop operations", content = @Content(schema = @Schema(implementation = ErrorPayload.class)))
			}
	)
	@RequestMapping(method=RequestMethod.DELETE, path = "/{runId}")
	public void stop(@Parameter(description = "Run id")  @PathVariable("runId") Long runId, @Parameter(description = "When set force backend to kill running tasks") @RequestParam(name = "force", required = false, defaultValue = "false") boolean force, Authentication authentication) throws NotAuthorizedException, ExecutionBackendException, RunNotFoundException {
		securityAssertionService.assertAuthorized(authentication, Permission.job_run_abort);
		jobService.stop(runId,force);
	}

	@Operation(description = "Post an input file to the run. It works only if the run is not started")
	@ApiResponses(
			{
					@ApiResponse(responseCode = "200", description = "File uploaded"),
					@ApiResponse(responseCode = "400", description = "Cannot add files to the run", content = @Content(schema = @Schema(implementation = ErrorPayload.class))),
					@ApiResponse(responseCode = "403", description = "User not authorized", content = @Content(schema = @Schema())),
					@ApiResponse(responseCode = "404", description = "Run not found", content = @Content(schema = @Schema())),
					@ApiResponse(responseCode = "500", description = "An error occurred upload", content = @Content(schema = @Schema(implementation = ErrorPayload.class))),
			}
	)
	@RequestMapping(method=RequestMethod.POST,  path = "/{runId}/files/{fileId}")
	public void upload(@Parameter(description = "Run id")  @PathVariable("runId") Long runId, @Parameter(description = "File identifier") @PathVariable("fileId") String fileId, HttpServletRequest request, Authentication authentication) throws NotAuthorizedException, IOException, ValidationException, RunNotFoundException {
		securityAssertionService.assertAuthorized(authentication, Permission.job_run_upload_files);
		try (InputStream inputStream = request.getInputStream()) {
			jobService.uploadFile(runId, fileId, inputStream);
		}
	}

	@Operation(description = "Refresh job status")
	@ApiResponses(
			{
					@ApiResponse(responseCode = "200", description = "Refresh completed"),
					@ApiResponse(responseCode = "403", description = "User not authorized", content = @Content(schema = @Schema())),
					@ApiResponse(responseCode = "404", description = "Run not found", content = @Content(schema = @Schema())),
					@ApiResponse(responseCode = "500", description = "An error occurred during refresh", content = @Content(schema = @Schema(implementation = ErrorPayload.class))),
			}
	)

	@RequestMapping(method=RequestMethod.GET, path = "/{runId}/refresh")
	public RunInfo refreshRunInfo(@PathVariable("runId") Long runId,Authentication authentication) throws ExecutionBackendException, NotAuthorizedException, RunNotFoundException {
		securityAssertionService.assertAuthorized(authentication, Permission.job_run_refresh);
		return jobService.refresh(runId);
	}

	@Operation(description = "Get run output")
	@ApiResponses(
			{
					@ApiResponse(responseCode = "200", description = "Job output"),
					@ApiResponse(responseCode = "403", description = "User not authorized", content = @Content(schema = @Schema())),
					@ApiResponse(responseCode = "404", description = "Run not found", content = @Content(schema = @Schema())),
					@ApiResponse(responseCode = "500", description = "An error occurred during output retrieval", content = @Content(schema = @Schema(implementation = ErrorPayload.class))),
			}
	)
	@RequestMapping(method=RequestMethod.GET, path = "/{runId}/output.txt")
	public void getRunOutput(@PathVariable("runId") Long runId, HttpServletResponse response, Authentication authentication) throws ExecutionBackendException, IOException, NotAuthorizedException, RunNotFoundException {
		securityAssertionService.assertAuthorized(authentication, Permission.job_run_output);
		response.setContentType("text/plain");
		jobService.writeRunOutputInto(runId, response.getOutputStream());
	}
}

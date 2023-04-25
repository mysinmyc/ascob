package ascob.impl.tools.rundeck;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

public class RundeckClient {

	String url;
	String authToken;
	RestTemplate restTemplate;

	public RundeckClient(String url, String authToken) {
		this.url=url;
		this.authToken =authToken;
		this.restTemplate = buildRestTemplate();
	}

	public RundeckClient(RundeckInstanceConfiguration configuration) {
		this(configuration.getUrl(), configuration.getAuthToken());
	}

	private RestTemplate buildRestTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getInterceptors().add((request, body, execution) ->{
			request.getHeaders().add("X-Rundeck-Auth-Token", authToken);
			request.getHeaders().setAccept(List.of( new MediaType("application","json")));
			return execution.execute(request,body);
		});
		return restTemplate;
	}

	static class ListJobResponse extends ArrayList<RundeckJob> {

	}

	public RundeckJob findJobByProjectAndName(String project, String name) {
		Map<String,String> uriVariables = new HashMap<>();
		uriVariables.put("project",project);
		uriVariables.put("name",name);
		ListJobResponse response = restTemplate.getForObject(url+"/api/14/project/{project}/jobs?jobExactFilter={name}", ListJobResponse.class, uriVariables);
		if (response.size()==0) {
			return null;
		}
		if (response.size()>1) {
			throw new RuntimeException("Invalid number of jobs found with name "+name+": "+ response.size());
		}
		return response.get(0);
	}

	public String submitJobByProjectAndName(String project, String name, Map<String,String> options) {
		return submitJobById(findJobByProjectAndName(project,name).getId(), options);
	}

	public String submitJobById(String jobId, Map<String,String> options) {
		SubmitJobRequest request= new SubmitJobRequest();
		request.setOptions(options);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(new MediaType("application","json"));
		HttpEntity<SubmitJobRequest> requestEntity = new HttpEntity<>(request, headers);
		Map<String,String> uriVariables = new HashMap<>();
		uriVariables.put("jobId",jobId);
		return restTemplate.postForObject(url+"/api/19/job/{jobId}/run", requestEntity, Execution.class, uriVariables).getId();
	}

	public Execution getExecution(String executionId) {
		Map<String,String> uriVariables = new HashMap<>();
		uriVariables.put("executionId",executionId);
		return restTemplate.getForObject(url+"/api/19/execution/{executionId}", Execution.class, uriVariables);
	}


	public void writeExecutionOutputInto(String executionId, OutputStream outputStream, int lastLines ) throws IOException {
		Map<String,String> uriVariables = new HashMap<>();
		uriVariables.put("executionId",executionId);
		uriVariables.put("lastLines",""+lastLines);
		ResponseEntity<Resource> responseEntity =restTemplate.getForEntity(url+"/api/19/execution/{executionId}/output?lastlines={lastLines}&format=text", Resource.class, uriVariables);
        try (InputStream inputStream = responseEntity.getBody().getInputStream()) {
                StreamUtils.copy(inputStream, outputStream);
        }
	}

	public void abortExecution(String executionId) {
		Map<String,String> uriVariables = new HashMap<>();
		uriVariables.put("executionId",executionId);
		ResponseEntity<Object> response =restTemplate.getForEntity(url+"/api/19/execution/{executionId}/abort?forceIncomplete=true", Object.class, uriVariables);
}
}


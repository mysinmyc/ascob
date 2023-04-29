package ascob.impl.tools.jenkins;

import ascob.impl.tools.rundeck.RundeckInstanceConfiguration;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.Resource;

public class JenkinsClient {
    String url;

    String authorizationHeader;
    RestTemplate restTemplate;

    public JenkinsClient(String url, String userName, String apiToken) {
        this.url=url;
        this.authorizationHeader = "Basic "+Base64.getEncoder().encodeToString((userName+":"+apiToken).getBytes(StandardCharsets.US_ASCII));
        this.restTemplate = buildRestTemplate();
    }

    public JenkinsClient(JenkinsInstanceConfiguration configuration) {
        this(configuration.getUrl(),configuration.getUserName(), configuration.getApiToken());
    }

    private RestTemplate buildRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) ->{
            request.getHeaders().add( "Authorization",authorizationHeader);
            return execution.execute(request,body);
        });
        return restTemplate;
    }

    public void buildWithParameters(String project, Map<String,String> parameters){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String,String> jobParameters= new LinkedMultiValueMap<>();
        parameters.forEach(jobParameters::add);
        HttpEntity<MultiValueMap<String,String>> request = new HttpEntity<MultiValueMap<String,String>>(jobParameters,headers);
        Map<String,String> uriVariables = new HashMap<>();
        uriVariables.put("project", project);
        uriVariables.put("delay","0");
        restTemplate.postForLocation(url+"/job/{project}/buildWithParameters?delay={delay}sec",request, uriVariables);
    }

    public void build(String project){
        Map<String,String> uriVariables = new HashMap<>();
        uriVariables.put("project", project);
        uriVariables.put("delay","0");
        restTemplate.postForLocation(url+"/job/{project}/build?delay={delay}sec",null,uriVariables);
    }

    static class BuildResultResponse {
        BuildResult result;
        public BuildResult getResult() {
            return result;
        }
        public void setResult(BuildResult result) {
            this.result = result;
        }
    }

    public BuildResult getBuildResult(String project, String buildId) {
        Map<String,String> uriVariables = new HashMap<>();
        uriVariables.put("project", project);
        uriVariables.put("buildId",buildId);
        return restTemplate.getForObject(url + "/job/{project}/{buildId}/api/xml?tree=result",
                BuildResultResponse.class,uriVariables).getResult();
    }

    static class  BuildIdresponse {
        String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
    public String getLastBuildId(String project) {
        Map<String,String> uriVariables = new HashMap<>();
        uriVariables.put("project", project);
        return restTemplate.getForObject(url + "/job/{project}/api/xml?tree=lastBuild[id]&xpath=//lastBuild",
                BuildIdresponse.class,uriVariables).getId();
    }

    static class  NextBuildNumberResponse {

        String nextBuildNumber;
        public String getNextBuildNumber() {
            return nextBuildNumber;
        }

        public void setNextBuildNumber(String nextBuildNumber) {
            this.nextBuildNumber = nextBuildNumber;
        }
    }
    public String getNextBuildNumber(String project) {
        Map<String,String> uriVariables = new HashMap<>();
        uriVariables.put("project", project);
        return restTemplate.getForObject(url + "/job/{project}/api/xml?tree=nextBuildNumber",
                NextBuildNumberResponse.class,uriVariables).getNextBuildNumber();
    }
    public void  writeBuildOutputInto(String project, String buildId, OutputStream outputStream) throws IOException {
        Map<String,String> uriVariables = new HashMap<>();
        uriVariables.put("project", project);
        uriVariables.put("buildId",buildId);
        ResponseEntity<Resource> responseEntity =restTemplate.getForEntity(url+"/job/{project}/{buildId}/consoleText",Resource.class,uriVariables);
        try (InputStream inputStream = responseEntity.getBody().getInputStream()) {
            StreamUtils.copy(inputStream, outputStream);
        }
    }

    public void abortBuild(String project, String buildId) {
        abortBuild(project,buildId,AbortMode.stop);
    }
    public void abortBuild(String project, String buildId, AbortMode abortMode) {
        Map<String,String> uriVariables = new HashMap<>();
        uriVariables.put("project", project);
        uriVariables.put("buildId",buildId);
        uriVariables.put("abortMode", abortMode.toString());
        restTemplate.postForObject(url + "/job/{project}/{buildId}/{abortMode}",null,
                Object.class,uriVariables);
    }
}

package ascob.server;

import ascob.security.ApiTokenIdentity;
import ascob.security.ApiTokenStore;
import ascob.security.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Component
@Lazy
public class TestClients {

	@Value("${security.rootToken}")
	String rootToken;

	@Value("${security.tokens.jobManager}")
	List<String> jobMangerTokens;

	@Value("${security.tokens.webhook}")
	List<String> webhookTokens;

	@Autowired
	ApiTokenStore apiTokenStore;

	@Autowired
	TestRestTemplate restTemplate;

	Map<String, TestRestTemplate> restTemplates = new HashMap<>();

	public TestRestTemplate getOrCreate(String identifier, Supplier<String> tokenSecret) {
		TestRestTemplate result = restTemplates.get(identifier);
		if (result ==null)  {
			result= new TestRestTemplate(new RestTemplateBuilder(r-> r.getInterceptors().add(new ClientHttpRequestInterceptor() {
				@Override
				public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
					request.getHeaders().add("X-Api-Token", tokenSecret.get());
					return execution.execute(request,body);
				}
			})));
			result.setUriTemplateHandler(restTemplate.getRestTemplate().getUriTemplateHandler());
			restTemplates.put(identifier,result);
		}

		return result;
	}
	public TestRestTemplate withRootToken() {
		return getOrCreate("rootToken", ()->rootToken);

	}

	public TestRestTemplate withJobManagerToken() {
		return getOrCreate("jobManager", ()-> jobMangerTokens.get(0));
	}

	public TestRestTemplate withWebhookToken() {
		return getOrCreate("webhook", ()-> webhookTokens.get(0));
	}


	public TestRestTemplate withoutPrivilegesToken() {
		return getOrCreate("noprivileges", ()-> {
			ApiTokenIdentity identity = new ApiTokenIdentity();
			identity.setIdentifier("noprivileges");
			return apiTokenStore.newToken(identity, 0);
		});
	}

	public TestRestTemplate withExpiredToken() {
		return getOrCreate("expired", ()-> {
			ApiTokenIdentity identity = new ApiTokenIdentity();
			identity.setIdentifier("expired");
			return apiTokenStore.newToken(identity, -1);
		});
	}

	public TestRestTemplate withSecurityManagerToken() {
		return getOrCreate("securityManager", ()-> {
			ApiTokenIdentity identity = new ApiTokenIdentity();
			identity.setPermissions(Permission.byGroup("security"));
			identity.setIdentifier("securityManager");
			return apiTokenStore.newToken(identity, 0);
		});
	}
}

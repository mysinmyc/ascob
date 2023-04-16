package ascob.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class TestClients {

	@Autowired(required = false)
	TestRestTemplate testRestTemplate;
	
	public TestRestTemplate testUserRestTemplate() {
		return testRestTemplate.withBasicAuth("test", "test");
	}
}

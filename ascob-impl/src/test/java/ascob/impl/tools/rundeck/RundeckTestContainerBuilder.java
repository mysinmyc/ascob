package ascob.impl.tools.rundeck;

import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.MountableFile;

public class RundeckTestContainerBuilder {

	public static GenericContainer<?> buildAndStart() throws InterruptedException { 

		@SuppressWarnings("resource")
		GenericContainer<?> rundeckContainer = new GenericContainer<>("rundeck/rundeck:4.12.0")
				.withCopyFileToContainer(MountableFile.forClasspathResource("rundeck/tokens.properties"),
						"/tokens.properties")
				.withCopyFileToContainer(MountableFile.forClasspathResource("rundeck/grailsdb.mv.db"),
						"/home/rundeck/server/data/grailsdb.mv.db")
				.withEnv("RUNDECK_TOKENS_FILE", "/tokens.properties")
				.withEnv("GRAILS_SERVER_URL", "http://localhost:4440/").withNetworkMode("host");
		rundeckContainer.start();

		RestTemplate restTemplate = new RestTemplate();
		while (true) {
			try {
				if (restTemplate.getForEntity("http://localhost:4440", String.class).getStatusCode()
						.is2xxSuccessful()) {
					break;
				}
			} catch (Exception e) {
				Thread.sleep(1000);
			}
		}
		return rundeckContainer;
	}
}

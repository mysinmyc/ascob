package ascob.impl.tools.jenkins;

import com.github.dockerjava.api.model.Bind;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.util.Arrays;

public class JenkinsTestContainerBuilder {

	static final String PLUGINS = "configuration-as-code:latest";

	public static GenericContainer<?> buildAndStart(int jenkinsPort) throws Exception {

		@SuppressWarnings("resource")
		//GenericContainer<?> jenkinsContainer = new GenericContainer<>("jenkins/jenkins:2.401");
		GenericContainer<?> jenkinsContainer = new GenericContainer<>("mysinmyc/jenkins");
		jenkinsContainer.setPortBindings(Arrays.asList(jenkinsPort+":8080"));
		jenkinsContainer.withCopyFileToContainer(MountableFile.forClasspathResource("jenkins/init.groovy.txt"),
				"/var/jenkins_home/init.groovy");
		jenkinsContainer.withCopyFileToContainer(MountableFile.forClasspathResource("jenkins/jenkins.yaml"),
				"/var/jenkins_home/jenkins.yaml");

//		jenkinsContainer.withEnv("JAVA_OPTS", "-Djenkins.install.runSetupWizard=false");



		jenkinsContainer.start();

		RestTemplate restTemplate = new RestTemplate();
		while (true) {
			try {
				if (restTemplate.getForEntity("http://localhost:"+jenkinsPort+"/login", String.class).getStatusCode()
						.is2xxSuccessful()) {
					break;
				}
			} catch (Exception e) {
				Thread.sleep(1000);
			}
		}
		/*
		Container.ExecResult result=jenkinsContainer.execInContainer("jenkins-plugin-cli", "--plugins", PLUGINS);

		if (result.getExitCode()!= 0) {
			throw new Exception("Failed to install plugin " + result.getStdout() + " " + result.getStderr());
		}
		 */

		return jenkinsContainer;
	}
}


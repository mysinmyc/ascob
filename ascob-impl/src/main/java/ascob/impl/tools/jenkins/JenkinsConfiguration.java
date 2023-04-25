package ascob.impl.tools.jenkins;

import ascob.impl.tools.rundeck.RundeckInstanceConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "jenkins")
@ConfigurationPropertiesScan
public class JenkinsConfiguration {

	
	Map<String, JenkinsInstanceConfiguration> instances;

	public Map<String, JenkinsInstanceConfiguration> getInstances() {
		return instances;
	}

	public void setInstances(Map<String, JenkinsInstanceConfiguration> instances) {
		this.instances = instances;
	}
}

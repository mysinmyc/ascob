package ascob.impl.tools.rundeck;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rundeck")
@ConfigurationPropertiesScan
public class RundeckConfiguration {

	
	Map<String,RundeckInstanceConfiguration> instances;

	public Map<String, RundeckInstanceConfiguration> getInstances() {
		return instances;
	}

	public void setInstances(Map<String, RundeckInstanceConfiguration> instances) {
		this.instances = instances;
	}
}

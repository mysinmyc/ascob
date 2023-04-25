package ascob.impl.tools.rundeck;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(matchIfMissing = false, name= "rundeck.enabled", havingValue = "true")
@Component
public class RundeckClientManager {

	public RundeckClientManager(@Autowired RundeckConfiguration configuration) {
		
		Map<String,RundeckClient> clients = new HashMap<>();
		configuration.getInstances().forEach( (k,v) -> clients.put(k,  new RundeckClient(v)));
		this.clients =clients;
	}
	
	Map<String,RundeckClient> clients;
	
	public RundeckClient getClientByName(String name) {
		return clients.get(name);
	}

}

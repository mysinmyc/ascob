package ascob.impl.tools.jenkins;

import ascob.impl.tools.rundeck.RundeckClient;
import ascob.impl.tools.rundeck.RundeckConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@ConditionalOnProperty(matchIfMissing = false, name= "jenkins.enabled", havingValue = "true")
@Component
public class JenkinsClientManager {


    public JenkinsClientManager(@Autowired JenkinsConfiguration configuration) {

        Map<String, JenkinsClient> clients = new HashMap<>();
        configuration.getInstances().forEach( (k,v) -> clients.put(k,  new JenkinsClient(v)));
        this.clients =clients;
    }

    Map<String,JenkinsClient> clients;

    public JenkinsClient getClientByName(String name) {
        return clients.get(name);
    }

}

package ascob.server.backend;

import java.util.List;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ascob.backend.ExecutionBackend;

@Component
public class ExecutionBackendRegistry {

	@Autowired(required=false)
	List<ExecutionBackend> backends;
	
	public List<ExecutionBackend> getAllBackends() {
		if (backends==null) {
			return new ArrayList<ExecutionBackend>();
		}
		return backends;
	}
}

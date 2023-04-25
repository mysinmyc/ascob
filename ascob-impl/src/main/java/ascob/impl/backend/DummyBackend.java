package ascob.impl.backend;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import ascob.backend.BackendOutputWriter;
import org.springframework.stereotype.Component;

import ascob.api.JobSpec;
import ascob.backend.BackendRunStatus;

@Component
public class DummyBackend extends ExecutionBackendBase implements BackendOutputWriter {
	
	static AtomicLong counter = new AtomicLong();
	
	@Override
	public boolean isAvailableFor(JobSpec jobSpec) {
		return jobSpec.getDescription() != null && jobSpec.getDescription().toLowerCase().contains("dummy");
	}

	@Override
	public Map<String,String> submit(JobSpec jobSpec) throws Exception {	
		Map<String,String> keys= new HashMap<>();
		keys.put("id",""+counter.incrementAndGet());
		return keys;
	}

	@Override
	public BackendRunStatus getStatus(Map<String,String> backendRunId) throws Exception {
		return BackendRunStatus.SUCCEDED;
	}


	public void writeOutputInto(Map<String,String> backendRunId, OutputStream outputStream) throws Exception {
		outputStream.write(new String("Dummy output").getBytes());
	}
}

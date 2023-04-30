package ascob.impl.backend;

import ascob.backend.BackendOutputWriter;
import ascob.backend.BackendRunStatus;
import ascob.job.JobSpec;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@ConditionalOnProperty(matchIfMissing = true, name = "backend.dummy.enabled", havingValue = "true")
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

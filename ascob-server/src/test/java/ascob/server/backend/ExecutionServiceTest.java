package ascob.server.backend;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ascob.backend.BackendRunId;

@SpringBootTest
public class ExecutionServiceTest {

	@Autowired
	ExecutionService executionService;
	
	@Test
	public void testNoBackend() {
		
		BackendRunId runId= new BackendRunId();
		runId.setBackendId("_test");
		assertThrows(ExecutionBackendException.class, () -> {
			executionService.getStatus(runId);
		});

		HashMap<String,String> keys = new HashMap<>();
		runId.setIdentificationKeys(keys);
		assertThrows(ExecutionBackendException.class, () -> {
			executionService.getStatus(runId);
		});

		keys.put("key", "ciao");
		keys.put("status", "SUCCEDED");
		assertDoesNotThrow(() -> {
			executionService.getStatus(runId);
		});
		
		
	}
}

package ascob.server.lock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ascob.job.LockSpec;

@SpringBootTest
public class LockServiceTest {

	@Autowired
	LockManager lockManager;
	
	@Test
	public void testLocks() {
		List<LockSpec> locks = LockSpec.of("ciao","miao","bau");
		
		assertTrue(lockManager.acquireLocks("pippo", locks));
		assertTrue(lockManager.acquireLocks("pippo", locks));
		
		assertFalse(lockManager.acquireLocks("pluto", LockSpec.of("pluto","miao")));
		
		lockManager.releaseLocks("pippo", locks);
	
		assertTrue(lockManager.acquireLocks("pluto", LockSpec.of("pluto","miao")));
	}
}

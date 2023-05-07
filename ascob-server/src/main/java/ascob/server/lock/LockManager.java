package ascob.server.lock;

import ascob.job.LockSpec;
import ascob.kv.KvStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * Manages locks in a KV store
 */
@Component
public class LockManager {

	@Autowired
	KvStore kvStore;

	static String getKeyOwnerSuffix(String ownerId) {
		return "/owner/"+ownerId;
	}

	static String lockToKey(String ownerId,LockSpec lockSpec) {
		return "/ascob/locks/"+lockSpec.getKey()+getKeyOwnerSuffix(ownerId);
	}

	/**
	 * Try to acquire locks by setting keys in the KV store and checking that there are no other keys owner by others. If it fails remove keys
	 * @param ownerId = lock owner
	 * @param lockSpecs = locks
	 * @return true if the locks are acquired
	 */
	public boolean acquireLocks(String ownerId, List<LockSpec> lockSpecs) {
		if (lockSpecs==null || lockSpecs.isEmpty()) {
			return true;
		}
			for (LockSpec lock : lockSpecs) {
			kvStore.setKeyValue(lockToKey(ownerId,lock),"");
		}
		boolean acquired = true;
		for (LockSpec lock : lockSpecs) {
			Collection<String> currentLockKeys = kvStore.getKeysByPrefix(lockToKey("",lock));
			if (currentLockKeys != null) {
				for (String currentLockKey : currentLockKeys) {
					if (!currentLockKey.endsWith(getKeyOwnerSuffix(ownerId))) {
						acquired = false;
						break;
					}
				}
			}
		}
		if (!acquired) {
			releaseLocks(ownerId, lockSpecs);
		}
		return acquired;
	}

	/**
	 * Release locks by removing keys inside the KV store
	 * @param ownerId = Lock owner
	 * @param lockSpecs = locks
	 */
	public void releaseLocks(String ownerId, List<LockSpec> lockSpecs) {
		if (lockSpecs==null || lockSpecs.isEmpty()) {
			return;
		}
		for (LockSpec lock : lockSpecs) {
			kvStore.removeKey(lockToKey(ownerId,lock));
		}
	}
}

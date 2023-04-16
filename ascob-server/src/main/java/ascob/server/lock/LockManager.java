package ascob.server.lock;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ascob.api.LockSpec;
import ascob.kv.KvStore;

@Component
public class LockManager {
	
	@Autowired
	KvStore kvStore;
	
	String getKeyOwnerSuffix(String ownerId) {
		return "/"+ownerId;
	}
	
	String lockToKey(String ownerId,LockSpec lockSpec) {
		return "/ascob/locks/"+lockSpec.getKey()+getKeyOwnerSuffix(ownerId);
	}
	
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
	
	public void releaseLocks(String ownerId, List<LockSpec> lockSpecs) {
		if (lockSpecs==null || lockSpecs.isEmpty()) {
			return;
		}		
		for (LockSpec lock : lockSpecs) {
			kvStore.removeKey(lockToKey(ownerId,lock));
		}
	}
}

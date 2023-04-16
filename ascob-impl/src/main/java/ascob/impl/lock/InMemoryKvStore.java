package ascob.impl.lock;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import ascob.kv.KvStore;

@Component
public class InMemoryKvStore implements  KvStore {


	Map<String,String> store = new HashMap<>();

	@Override
	public String getKeyValue(String key) {
		return store.get(key);
	}

	@Override
	public void setKeyValue(String key, String value) {
		store.put(key,value);
	}

	@Override
	public void removeKey(String key) {
		store.remove(key);
	}

	@Override
	public Collection<String> getKeysByPrefix(String prefix) {
		return store.keySet().stream().filter(k -> k.startsWith(prefix)).toList();
	}
	

}

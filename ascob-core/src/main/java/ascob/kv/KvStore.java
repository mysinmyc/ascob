package ascob.kv;

import java.util.Collection;

public interface KvStore {

	String getKeyValue(String key);
	
	void setKeyValue(String key, String value);
	
	void removeKey(String key);
	
	public Collection<String> getKeysByPrefix(String prefix);
}

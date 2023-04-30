package ascob.job;

import java.util.ArrayList;
import java.util.List;

public class LockSpec {

	String key;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public static List<LockSpec> of(String... keys) {
		List<LockSpec> result = new ArrayList<LockSpec>();
		for (String key : keys) {
			LockSpec lock = new LockSpec();
			lock.setKey(key);
			result.add(lock);
		}
		return result;
	}
}

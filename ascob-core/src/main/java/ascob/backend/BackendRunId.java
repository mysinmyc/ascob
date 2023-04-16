package ascob.backend;

import java.util.Map;

public class BackendRunId {

	String backendId;
	
	Map<String,String> identificationKeys;
	
	public Map<String, String> getIdentificationKeys() {
		return identificationKeys;
	}

	public void setIdentificationKeys(Map<String, String> identificationKeys) {
		this.identificationKeys = identificationKeys;
	}

	public String getBackendId() {
		return backendId;
	}

	public void setBackendId(String backendId) {
		this.backendId = backendId;
	}



	
}

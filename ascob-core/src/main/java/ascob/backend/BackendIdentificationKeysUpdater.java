package ascob.backend;

import java.util.Map;

/**
 * Backend
 */
public interface BackendIdentificationKeysUpdater {

    Map<String,String> updateIdentificationKeys(Map<String,String> newIdentificationKeys, Map<String,String> oldIdentificationKeys) throws Exception;

}

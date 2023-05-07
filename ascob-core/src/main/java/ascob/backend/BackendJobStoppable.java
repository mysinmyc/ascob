package ascob.backend;

import java.util.Map;

public interface BackendJobStoppable {
    void stopRun(Map<String,String> identificationKeys, StopMode mode) throws Exception;
}

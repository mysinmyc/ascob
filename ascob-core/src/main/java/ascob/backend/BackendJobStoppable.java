package ascob.backend;

import java.io.OutputStream;
import java.util.Map;

public interface BackendJobStoppable {
    void stopRun(Map<String,String> identificationKeys) throws Exception;
}

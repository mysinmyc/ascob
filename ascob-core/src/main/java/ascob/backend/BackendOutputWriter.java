package ascob.backend;

import java.io.OutputStream;
import java.util.Map;

public interface BackendOutputWriter {
    void writeOutputInto(Map<String,String> identificationKeys, OutputStream outputStream) throws Exception;
}

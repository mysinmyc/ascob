package ascob.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface FileStore {

    void store(String path, InputStream inputStream) throws IOException;

    void retrieveInto(String path, OutputStream outputStream) throws IOException;

    boolean exists(String path);

    void delete(String path);
}

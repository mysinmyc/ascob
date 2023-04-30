package ascob.impl.file;

import jakarta.persistence.*;
import org.springframework.context.annotation.Lazy;

@Entity
public class InternalFile {
    @Id
    String path;

    @Lob
    @Lazy
    byte[] content;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}

package ascob.server.job;

import jakarta.persistence.*;

@Entity
public class InternalRunFileReference {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;

    Long parentRunId;

    String fileId;

    String filePath;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getParentRunId() {
        return parentRunId;
    }

    public void setParentRunId(Long parentRunId) {
        this.parentRunId = parentRunId;
    }
}

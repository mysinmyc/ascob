package ascob.job;

import java.time.LocalDateTime;
import java.util.List;

public interface RunSearchFilters {

    String getSubmitterFilter();

    LocalDateTime getCreatedBeforeFilter();

    LocalDateTime getCreatedAfterFilter();

    List<RunStatus> getStatusFilter();
}

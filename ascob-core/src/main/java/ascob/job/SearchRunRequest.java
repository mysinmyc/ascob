package ascob.job;

import ascob.StandardDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public class SearchRunRequest  implements  RunSearchFilters {

    String submitterFilter;

    List<RunStatus> statusFilter;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern= StandardDateTime.PATTERN, timezone = StandardDateTime.TIMEZONE)
    LocalDateTime createdBeforeFilter;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern= StandardDateTime.PATTERN, timezone = StandardDateTime.TIMEZONE)
    LocalDateTime createdAfterFilter;

    int maxResults;

    public String getSubmitterFilter() {
        return submitterFilter;
    }

    public void setSubmitterFilter(String submitterFilter) {
        this.submitterFilter = submitterFilter;
    }

    public List<RunStatus> getStatusFilter() {
        return statusFilter;
    }

    public void setStatusFilter(List<RunStatus> statusFilter) {
        this.statusFilter = statusFilter;
    }

    public LocalDateTime getCreatedBeforeFilter() {
        return createdBeforeFilter;
    }

    public void setCreatedBeforeFilter(LocalDateTime createdBeforeFilter) {
        this.createdBeforeFilter = createdBeforeFilter;
    }

    public LocalDateTime getCreatedAfterFilter() {
        return createdAfterFilter;
    }

    public void setCreatedAfterFilter(LocalDateTime createdAfterFilter) {
        this.createdAfterFilter = createdAfterFilter;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }



}

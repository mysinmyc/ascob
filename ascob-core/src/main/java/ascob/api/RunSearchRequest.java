package ascob.api;

import java.time.LocalDateTime;

public class RunSearchRequest {

    String submitter;

    LocalDateTime startedAfter;

    String descriptionPattern;

    int limit;

    int offset;


}

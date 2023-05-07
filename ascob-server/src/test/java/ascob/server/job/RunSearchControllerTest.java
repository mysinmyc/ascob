package ascob.server.job;

import ascob.backend.BackendRunStatus;
import ascob.job.*;
import ascob.server.TestClients;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("webtest")
public class RunSearchControllerTest {

    @Autowired
    JobStore jobStore;

    @Autowired
    JobService jobService;

    @Autowired
    TestClients testClients;

    @Test
    public void testSearchBySubmitter() {
        jobStore.clear();

        SubmitRequest submitRequest1 = new SubmitRequest();
        submitRequest1.setJobSpec(JobSpec.builder("submitter1").withDescription("test").build());
        for (int cnt=0;cnt < 5; cnt++) {
            testClients.withJobManagerToken().postForObject("/api/runs", submitRequest1, Object.class);
        }

        SubmitRequest submitRequest2 = new SubmitRequest();
        submitRequest2.setJobSpec(JobSpec.builder("submitter2").withDescription("test").build());
        for (int cnt=0;cnt < 5; cnt++) {
            testClients.withJobManagerToken().postForObject("/api/runs", submitRequest2, Object.class);
        }

        SearchRunRequest searchRunRequest = new SearchRunRequest();
        searchRunRequest.setSubmitterFilter("submitter1");
        SearchRunResponse searchRunResponse = testClients.withJobManagerToken().postForObject("/api/search/runs", searchRunRequest,SearchRunResponse.class);

        assertEquals(5,searchRunResponse.getItems().size());

        searchRunResponse.getItems().forEach( i->    assertEquals("submitter1",i.getSubmitter()));
    }


    @Test
    public void testSearchByDate() throws InterruptedException {

        jobStore.clear();

        SubmitRequest submitRequest1 = new SubmitRequest();
        submitRequest1.setJobSpec(JobSpec.builder("submitter1").withDescription("test").build());
        for (int cnt=0;cnt < 7; cnt++) {
            testClients.withJobManagerToken().postForObject("/api/runs", submitRequest1, Object.class);
        }
        Thread.sleep(200);
        LocalDateTime timestamp = LocalDateTime.now();
        Thread.sleep(200);
        SubmitRequest submitRequest2 = new SubmitRequest();
        submitRequest2.setJobSpec(JobSpec.builder("submitter1").withDescription("test").build());
        for (int cnt=0;cnt < 8; cnt++) {
            testClients.withJobManagerToken().postForObject("/api/runs", submitRequest2, Object.class);
        }

        SearchRunRequest searchRunRequestAfter = new SearchRunRequest();
        searchRunRequestAfter.setCreatedAfterFilter(timestamp);
        SearchRunResponse searchRunResponseAfter = testClients.withJobManagerToken().postForObject("/api/search/runs", searchRunRequestAfter,SearchRunResponse.class);

        assertEquals(8,searchRunResponseAfter.getItems().size());

        searchRunResponseAfter.getItems().forEach( i->    assertTrue(i.getDefinedTime().isAfter(timestamp) ));


        SearchRunRequest searchRunRequestBefore = new SearchRunRequest();
        searchRunRequestBefore.setCreatedBeforeFilter(timestamp);
        SearchRunResponse searchRunResponseBefore = testClients.withJobManagerToken().postForObject("/api/search/runs", searchRunRequestBefore,SearchRunResponse.class);

        assertEquals(7,searchRunResponseBefore.getItems().size());

        searchRunResponseBefore.getItems().forEach( i->    assertTrue(i.getDefinedTime().isBefore(timestamp) ));
    }

    @Test
    public void testSearchByStatus() throws InterruptedException {
        jobStore.clear();

        SubmitRequest submitRequestOk = new SubmitRequest();
        submitRequestOk.setJobSpec(JobSpec.builder("submitter1").withDescription("test").withLabel("status", BackendRunStatus.SUCCEEDED.name()).build());
        for (int cnt = 0; cnt < 7; cnt++) {
            testClients.withJobManagerToken().postForObject("/api/runs", submitRequestOk, Object.class);
        }

        SubmitRequest submitRequestAborted = new SubmitRequest();
        submitRequestAborted.setJobSpec(JobSpec.builder("submitter1").withDescription("test").withLabel("status", BackendRunStatus.ABORTED.name()).build());
        for (int cnt = 0; cnt < 8; cnt++) {
            testClients.withJobManagerToken().postForObject("/api/runs", submitRequestAborted, Object.class);
        }

        SubmitRequest submitRequestFailed = new SubmitRequest();
        submitRequestFailed.setJobSpec(JobSpec.builder("submitter1").withDescription("test").withLabel("status", BackendRunStatus.FAILED.name()).build());
        for (int cnt = 0; cnt < 9; cnt++) {
            testClients.withJobManagerToken().postForObject("/api/runs", submitRequestFailed, Object.class);
        }

        jobService.refreshActiveJobs();

        SearchRunRequest searchRunRequest = new SearchRunRequest();
        searchRunRequest.setStatusFilter(List.of(RunStatus.FAILED, RunStatus.ABORTED));
        SearchRunResponse searchRunResponse = testClients.withJobManagerToken().postForObject("/api/search/runs", searchRunRequest, SearchRunResponse.class);

        assertEquals(17, searchRunResponse.getItems().size());

        searchRunResponse.getItems().forEach(i -> assertTrue(i.getStatus().equals(RunStatus.FAILED) || i.getStatus().equals(RunStatus.ABORTED)));
    }
}
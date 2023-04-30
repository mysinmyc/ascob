package ascob.server.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobRefreshScheduler {

	@Autowired
	JobService jobService;
	
	@Async
	@Scheduled(initialDelayString="${jobrefresh.delay_ms:30000}",  fixedRateString = "${jobrefresh:30000}")
	public void refreshActiveJobs() {
		jobService.refreshActiveJobs();
	}
}

package ascob.server.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(matchIfMissing = true, name = "jobrefresh.enabled", havingValue = "true")
public class JobRefreshScheduler {

	@Autowired
	JobService jobService;
	
	@Async
	@Scheduled(initialDelayString="${jobrefresh.delay_ms:30000}",  fixedRateString = "${jobrefresh:30000}")
	public void refreshJobs() {
		jobService.refreshJobs();
	}
}

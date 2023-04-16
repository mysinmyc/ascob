package ascob.server.job;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import ascob.api.JobSpec;
import ascob.api.RunStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

@Component
public class JobStore {

	@PersistenceContext
	EntityManager entityManager;
	
	@Transactional(TxType.REQUIRES_NEW)
	public InternalRun newRun(JobSpec jobSpec) {
		InternalRun run = new InternalRun();
		run.setStatus(RunStatus.DEFINED);
		run.setJobSpec(jobSpec);
		run.setDefinedTime(LocalDateTime.now());
		run.setDescription(jobSpec.getDescription());
		run.setSubmitter(jobSpec.getSubmitter());		
		entityManager.persist(run);
		return run;
	}
	
	public InternalRun getRunById(Long runId) {
		return entityManager.find(InternalRun.class,runId);
	}
	
	@Transactional(TxType.REQUIRES_NEW)
	public void updateRun(InternalRun run) {
		entityManager.merge(run);
	}
}

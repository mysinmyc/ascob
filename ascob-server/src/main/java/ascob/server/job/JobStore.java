package ascob.server.job;

import ascob.job.JobSpec;
import ascob.job.RunSearchFilters;
import ascob.job.RunStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
		run.setWebhookId(UUID.randomUUID().toString());
		run.setRunnable(!jobSpec.isManualStart());
		entityManager.persist(run);
		return run;
	}

	public InternalRun getRunById(Long runId) {
		return entityManager.find(InternalRun.class,runId);
	}

	@Transactional(TxType.REQUIRES_NEW)
	public void updateRun(InternalRun run) {
		if (run.getStatus() != null && run.getStatus().isFinalState()) {
			run.setWebhookId(null);
		}
		entityManager.merge(run);
	}

	static final List<RunStatus>  ACTIVE_STATUSES = List.of(RunStatus.values()).stream().filter(s ->! s.isFinalState()).toList();

	public List<InternalRun> getActiveMonitoredJobs() {
		TypedQuery<InternalRun> runsQuery= entityManager.createQuery("from InternalRun where monitored=:monitored and status in (:statuses)", InternalRun.class);
		runsQuery.setParameter("monitored", Boolean.TRUE);
		runsQuery.setParameter("statuses", ACTIVE_STATUSES);
		return runsQuery.getResultList();
	}

	public InternalRun getRunByWebhookId(String webhookId) {
		TypedQuery<InternalRun> runsQuery= entityManager.createQuery("from InternalRun where webhookId=:webhookId", InternalRun.class);
		runsQuery.setParameter("webhookId", webhookId);
		return runsQuery.getSingleResult();
	}


	public List<InternalRun> searchRunByConditions(RunSearchFilters filters, int maxResults) {
		CriteriaBuilder criteriaBuilder=entityManager.getEntityManagerFactory().getCriteriaBuilder();
		CriteriaQuery<InternalRun> criteriaQuery = criteriaBuilder.createQuery(InternalRun.class);
		Root<InternalRun> root = criteriaQuery.from(InternalRun.class);
		criteriaQuery.select(root);
		List<Predicate> whereConditions = new ArrayList<>();
		if (filters.getSubmitterFilter() != null && !filters.getSubmitterFilter().isEmpty()) {
			whereConditions.add(criteriaBuilder.equal(root.get("submitter"), filters.getSubmitterFilter()));
		}
		if (filters.getStatusFilter() != null && filters.getStatusFilter().size()>0) {
			whereConditions.add(root.get("status").in(filters.getStatusFilter()));
		}
		if (filters.getCreatedAfterFilter() != null) {
			whereConditions.add(criteriaBuilder.greaterThan(root.get("definedTime"),filters.getCreatedAfterFilter()));
		}
		if (filters.getCreatedBeforeFilter() != null) {
			whereConditions.add(criteriaBuilder.lessThan(root.get("definedTime"),filters.getCreatedBeforeFilter()));
		}
		if (whereConditions.size()>0) {
			criteriaQuery.where(criteriaBuilder.and(whereConditions.toArray(new Predicate[]{})));
		}
		TypedQuery<InternalRun> query = entityManager.createQuery(criteriaQuery);
		criteriaQuery.orderBy(criteriaBuilder.desc(root.get("id")));
		if (maxResults>0) {
			query.setMaxResults(maxResults);
		}
		return query.getResultList();
	}

	@Transactional(TxType.REQUIRES_NEW)
	public void clear() {
		CriteriaBuilder criteriaBuilder=entityManager.getEntityManagerFactory().getCriteriaBuilder();
		CriteriaDelete<InternalRun> criteriaDelete = criteriaBuilder.createCriteriaDelete(InternalRun.class);
		Root<InternalRun> root = criteriaDelete.from(InternalRun.class);

		entityManager.createQuery(criteriaDelete).executeUpdate();
	}
}

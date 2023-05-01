package ascob.server.job;

import ascob.job.JobSpec;
import ascob.job.RunSearchFilters;
import ascob.job.RunStatus;
import ascob.server.util.SerializationUtil;
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

    static final List<RunStatus> ACTIVE_STATUSES = List.of(RunStatus.values()).stream().filter(s -> !s.isFinalState()).toList();

    static final List<RunStatus> PENDING_STATUSES = List.of(RunStatus.values()).stream().filter(s -> ! (s.isFinalState() || s.isRunning())).toList();
    @PersistenceContext
    EntityManager entityManager;

    private InternalRun buildRun(JobSpec jobSpec) throws InvalidJobSpecException {
        InternalRun run = new InternalRun();
        run.setStatus(RunStatus.DEFINED);
        run.setJobSpec(jobSpec);
        run.setDefinedTime(LocalDateTime.now());
        run.setDescription(jobSpec.getDescription());
        run.setSubmitter(jobSpec.getSubmitter());
        run.setWebhookId(UUID.randomUUID().toString());
        run.setRunnable(!jobSpec.isManualStart());
        return run;
    }

    @Transactional(TxType.REQUIRES_NEW)
    public InternalRun newRun(JobSpec jobSpec) throws InvalidJobSpecException {
        InternalRun run = buildRun(jobSpec);
        entityManager.persist(run);
        if (jobSpec.isRuntimeVariables()) {
            run.setRuntimeSpec(resolveVariables(run, jobSpec));
            entityManager.merge(run);
        }
        return run;
    }

    @Transactional(TxType.REQUIRES_NEW)
    public InternalRun duplicateRun(InternalRun sourceRun, String submmitter) throws InvalidJobSpecException {
        JobSpec jobSpec = SerializationUtil.clone(sourceRun.getJobSpec());
        jobSpec.setSubmitter(submmitter);
        jobSpec.setManualStart(false);
        InternalRun run = buildRun(jobSpec);
        run.setParentId(sourceRun.getParentId() == null ? sourceRun.getId() : sourceRun.getParentId());
        entityManager.persist(run);
        if (jobSpec.isRuntimeVariables()) {
            run.setRuntimeSpec(resolveVariables(run, jobSpec));
            entityManager.merge(run);
        }
        return run;
    }

    private JobSpec resolveVariables(InternalRun run, JobSpec jobSpec) throws InvalidJobSpecException {
        String jobSpecString = SerializationUtil.serialize(jobSpec);
        if (jobSpecString.indexOf("%%") > -1) {
            jobSpecString = jobSpecString.replaceAll("%%SUBMITTER%%", run.getSubmitter());
            jobSpecString = jobSpecString.replaceAll("%%WEBHOOKID%%", run.getWebhookId());
            jobSpecString = jobSpecString.replaceAll("%%RUNID%%", ""+run.getId());
            if (jobSpecString.indexOf("%%") > -1) {
                throw new InvalidJobSpecException("Invalid variables %% in spec");
            }
        }
        return SerializationUtil.deserialize(JobSpec.class, jobSpecString);
    }

    public InternalRun getRunById(Long runId) {
        return entityManager.find(InternalRun.class, runId);
    }

    @Transactional(TxType.REQUIRES_NEW)
    public void updateRun(InternalRun run) {
        if (run.getStatus() != null && run.getStatus().isFinalState()) {
            run.setWebhookId(null);
        }
        entityManager.merge(run);
    }

    public List<InternalRun> getActiveMonitoredJobs() {
        TypedQuery<InternalRun> runsQuery = entityManager.createQuery("from InternalRun where monitored=:monitored and status in (:statuses)", InternalRun.class);
        runsQuery.setParameter("monitored", Boolean.TRUE);
        runsQuery.setParameter("statuses", ACTIVE_STATUSES);
        return runsQuery.getResultList();
    }

    public List<InternalRun> getPendingJobs() {
        TypedQuery<InternalRun> runsQuery = entityManager.createQuery("from InternalRun where status in (:statuses)", InternalRun.class);
        runsQuery.setParameter("statuses", PENDING_STATUSES);
        return runsQuery.getResultList();
    }

    public InternalRun getRunByWebhookId(String webhookId) {
        TypedQuery<InternalRun> runsQuery = entityManager.createQuery("from InternalRun where webhookId=:webhookId", InternalRun.class);
        runsQuery.setParameter("webhookId", webhookId);
        return runsQuery.getSingleResult();
    }


    public List<InternalRun> searchRunByConditions(RunSearchFilters filters, int maxResults) {
        CriteriaBuilder criteriaBuilder = entityManager.getEntityManagerFactory().getCriteriaBuilder();
        CriteriaQuery<InternalRun> criteriaQuery = criteriaBuilder.createQuery(InternalRun.class);
        Root<InternalRun> root = criteriaQuery.from(InternalRun.class);
        criteriaQuery.select(root);
        List<Predicate> whereConditions = new ArrayList<>();
        if (filters.getSubmitterFilter() != null && !filters.getSubmitterFilter().isEmpty()) {
            whereConditions.add(criteriaBuilder.equal(root.get("submitter"), filters.getSubmitterFilter()));
        }
        if (filters.getStatusFilter() != null && filters.getStatusFilter().size() > 0) {
            whereConditions.add(root.get("status").in(filters.getStatusFilter()));
        }
        if (filters.getCreatedAfterFilter() != null) {
            whereConditions.add(criteriaBuilder.greaterThan(root.get("definedTime"), filters.getCreatedAfterFilter()));
        }
        if (filters.getCreatedBeforeFilter() != null) {
            whereConditions.add(criteriaBuilder.lessThan(root.get("definedTime"), filters.getCreatedBeforeFilter()));
        }
        if (whereConditions.size() > 0) {
            criteriaQuery.where(criteriaBuilder.and(whereConditions.toArray(new Predicate[]{})));
        }
        TypedQuery<InternalRun> query = entityManager.createQuery(criteriaQuery);
        criteriaQuery.orderBy(criteriaBuilder.desc(root.get("id")));
        if (maxResults > 0) {
            query.setMaxResults(maxResults);
        }
        return query.getResultList();
    }

    @Transactional(TxType.REQUIRES_NEW)
    public void addFileReference(InternalRun run, String fileId, String filePath) {
        InternalRunFileReference fileReference = new InternalRunFileReference();
        fileReference.setFilePath(filePath);
        fileReference.setFileId(fileId);
        fileReference.setParentRunId(run.getId());
        entityManager.persist(fileReference);
    }


    public InternalRunFileReference getFileReference(InternalRun internalRun, String fileId) {
        CriteriaBuilder criteriaBuilder = entityManager.getEntityManagerFactory().getCriteriaBuilder();
        CriteriaQuery<InternalRunFileReference> criteriaQuery = criteriaBuilder.createQuery(InternalRunFileReference.class);
        Root<InternalRunFileReference> root = criteriaQuery.from(InternalRunFileReference.class);
        criteriaQuery.select(root);
        criteriaQuery.where(criteriaBuilder.and(criteriaBuilder.equal(root.get("fileId"), fileId), criteriaBuilder.equal(root.get("parentRunId"), internalRun.getParentId() == null ? internalRun.getId() : internalRun.getParentId())));
        TypedQuery<InternalRunFileReference> query = entityManager.createQuery(criteriaQuery);
        return query.getSingleResult();
    }

    @Transactional(TxType.REQUIRES_NEW)
    public void clear() {
        CriteriaBuilder criteriaBuilder = entityManager.getEntityManagerFactory().getCriteriaBuilder();
        for (Class currentClass : List.of(InternalRunFileReference.class, InternalRun.class)) {
            CriteriaDelete<?> criteriaDelete = criteriaBuilder.createCriteriaDelete(currentClass);
            Root<?> root = criteriaDelete.from(currentClass);
            entityManager.createQuery(criteriaDelete).executeUpdate();
        }
    }


}

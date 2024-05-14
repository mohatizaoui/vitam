/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

package fr.gouv.vitam.scheduler.server.job.auditobject;

import com.google.common.annotations.VisibleForTesting;
import fr.gouv.vitam.common.VitamConfiguration;
import fr.gouv.vitam.common.guid.GUID;
import fr.gouv.vitam.common.guid.GUIDFactory;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.common.thread.ExecutorUtils;
import fr.gouv.vitam.common.thread.VitamThreadUtils;
import fr.gouv.vitam.functional.administration.client.AdminManagementClientFactory;
import fr.gouv.vitam.logbook.operations.client.LogbookOperationsClientFactory;
import fr.gouv.vitam.metadata.client.MetaDataClientFactory;
import fr.gouv.vitam.processing.management.client.ProcessingManagementClientFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Stream;

@DisallowConcurrentExecution
public class AuditObjectJob implements Job {

    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(AuditObjectJob.class);
    private static final int AUDIT_POOL_SIZE = 5;
    private static final Boolean EMPTY_RESULT_IS_SUCCESSFUL = Boolean.TRUE;
    private static final String AUDIT_TYPE_KEY = "auditType";
    private static final String OPERATIONS_DELAY_IN_MINUTES_KEY = "operationsDelayInMinutes";
    private static final String CHECK_INTEGRITY_ID = "AUDIT_FILE_INTEGRITY";
    private static final String CHECK_EXISTENCE_ID = "AUDIT_FILE_EXISTING";

    private final AdminManagementClientFactory adminManagementClientFactory;
    private final LogbookOperationsClientFactory logbookOperationsClientFactory;
    private final ProcessingManagementClientFactory processingManagementClientFactory;
    private final MetaDataClientFactory metaDataClientFactory;

    @VisibleForTesting
    AuditObjectJob(
        AdminManagementClientFactory adminManagementClientFactory,
        LogbookOperationsClientFactory logbookOperationsClientFactory,
        ProcessingManagementClientFactory processingManagementClientFactory,
        MetaDataClientFactory metaDataClientFactory
    ) {
        this.adminManagementClientFactory = adminManagementClientFactory;
        this.logbookOperationsClientFactory = logbookOperationsClientFactory;
        this.processingManagementClientFactory = processingManagementClientFactory;
        this.metaDataClientFactory = metaDataClientFactory;
    }

    public AuditObjectJob() {
        this(
            AdminManagementClientFactory.getInstance(),
            LogbookOperationsClientFactory.getInstance(),
            ProcessingManagementClientFactory.getInstance(),
            MetaDataClientFactory.getInstance()
        );
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info("Integrity audit job in progress...");
        final ThreadPoolExecutor executorService = ExecutorUtils.createScalableBatchExecutorService(AUDIT_POOL_SIZE);
        try {
            final CompletableFuture<Optional<Boolean>>[] completableFutures = VitamConfiguration.getTenants()
                .stream()
                .map(tenantId -> launchWorkflowAndPoll(context, executorService, tenantId))
                .toArray(CompletableFuture[]::new);
            CompletableFuture.allOf(completableFutures).join();
            if (!hasAllJobsFinishedSuccessfully(completableFutures)) {
                throw new JobExecutionException("At least one tenant has integrity audit failed");
            }
        } finally {
            executorService.shutdown();
            LOGGER.info("Integrity audit job is finished");
        }
    }

    private CompletableFuture<Optional<Boolean>> launchWorkflowAndPoll(
        JobExecutionContext context,
        ThreadPoolExecutor executorService,
        Integer tenantId
    ) {
        final AuditWorkflowLauncher auditWorkflowLauncher = new AuditWorkflowLauncher(
            adminManagementClientFactory,
            logbookOperationsClientFactory,
            processingManagementClientFactory,
            metaDataClientFactory
        );
        return CompletableFuture.supplyAsync(
            () -> {
                VitamThreadUtils.getVitamSession().setTenantId(tenantId);
                final GUID operationId = GUIDFactory.newRequestIdGUID(tenantId);
                VitamThreadUtils.getVitamSession().setRequestId(operationId);
                final JobDataMap jobDataMap = context.getMergedJobDataMap();
                final int operationsDelayInMinutes = jobDataMap.getIntValue(OPERATIONS_DELAY_IN_MINUTES_KEY);
                final String auditAction = getAuditAction(jobDataMap.getString(AUDIT_TYPE_KEY));
                return launchAndWait(
                    auditWorkflowLauncher,
                    tenantId,
                    operationId.getId(),
                    operationsDelayInMinutes,
                    auditAction
                );
            },
            executorService
        );
    }

    private Optional<Boolean> launchAndWait(
        AuditWorkflowLauncher auditWorkflowLauncher,
        Integer tenantId,
        String operationId,
        int operationsDelayInMinutes,
        String auditAction
    ) {
        return auditWorkflowLauncher
            .launch(tenantId, operationId, operationsDelayInMinutes, auditAction)
            .map(id -> new AuditPoller(processingManagementClientFactory, tenantId, id))
            .map(AuditPoller::waitForTermination);
    }

    private boolean hasAllJobsFinishedSuccessfully(CompletableFuture<Optional<Boolean>>[] completableFutures) {
        return Stream.of(completableFutures)
            .map(CompletableFuture::join)
            .allMatch(value -> value.orElse(EMPTY_RESULT_IS_SUCCESSFUL));
    }

    private String getAuditAction(String auditType) {
        if (auditType == null) {
            throw new IllegalStateException("Audit type cannot be null");
        }
        if (auditType.equalsIgnoreCase("Integrity")) {
            return CHECK_INTEGRITY_ID;
        } else if (auditType.equalsIgnoreCase("Existence")) {
            return CHECK_EXISTENCE_ID;
        }
        throw new IllegalStateException("Cannot find audit type = " + auditType);
    }
}

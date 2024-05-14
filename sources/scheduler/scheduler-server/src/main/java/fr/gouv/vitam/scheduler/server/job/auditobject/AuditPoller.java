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

import com.google.common.base.Stopwatch;
import fr.gouv.vitam.common.exception.BadRequestException;
import fr.gouv.vitam.common.exception.InternalServerException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.exception.WorkflowNotFoundException;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.common.model.ItemStatus;
import fr.gouv.vitam.common.model.ProcessState;
import fr.gouv.vitam.common.model.StatusCode;
import fr.gouv.vitam.processing.management.client.ProcessingManagementClient;
import fr.gouv.vitam.processing.management.client.ProcessingManagementClientFactory;

import java.util.concurrent.TimeUnit;

public class AuditPoller {

    public static final boolean UNSUCCESSFUL_TERMINATION = Boolean.FALSE;
    public static final boolean SUCCESSFUL_TERMINATION = Boolean.TRUE;
    public static final int AUDIT_EXECUTION_TIMEOUT = 30;
    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(AuditPoller.class);
    private static final int SLEEPING_TIME_MULTIPLIER = 2;
    private static final int POLL_LIMIT = 60000;
    private final ProcessingManagementClientFactory processingManagementClientFactory;
    private final Integer tenantId;
    private final String operationId;

    public AuditPoller(
        ProcessingManagementClientFactory processingManagementClientFactory,
        Integer tenantId,
        String operationId
    ) {
        this.processingManagementClientFactory = processingManagementClientFactory;
        this.tenantId = tenantId;
        this.operationId = operationId;
    }

    public boolean waitForTermination() {
        try (ProcessingManagementClient processingManagementClient = processingManagementClientFactory.getClient()) {
            final Stopwatch stopwatch = Stopwatch.createStarted();
            int timeSleep = 1000;
            ItemStatus operationProcessStatus;
            AuditProcessState auditProcessState;
            StatusCode globalStatus;
            do {
                operationProcessStatus = processingManagementClient.getOperationProcessStatus(operationId);
                final ProcessState globalState = operationProcessStatus.getGlobalState();
                globalStatus = operationProcessStatus.getGlobalStatus();
                auditProcessState = AuditProcessState.wrap(globalState);
                if (hasCompletedOrTimeout(auditProcessState, globalStatus, stopwatch)) {
                    break;
                }
                TimeUnit.MILLISECONDS.sleep(timeSleep);
                timeSleep = Math.min(timeSleep * SLEEPING_TIME_MULTIPLIER, POLL_LIMIT);
            } while (true);
            return auditProcessState.hasCurrentWorkflowFinishedSuccessfully(globalStatus, tenantId);
        } catch (VitamClientException | InternalServerException | BadRequestException | InterruptedException e) {
            LOGGER.error("An error has occurred while waiting for audit termination", e);
            return UNSUCCESSFUL_TERMINATION;
        } catch (WorkflowNotFoundException e) {
            LOGGER.error("Could not find workflow: job has finished a long time ago", e);
            return SUCCESSFUL_TERMINATION;
        }
    }

    private boolean hasCompletedOrTimeout(
        AuditProcessState auditProcessState,
        StatusCode globalStatus,
        Stopwatch stopwatch
    ) {
        return (
            !auditProcessState.isWaitingForResponse(globalStatus) ||
            stopwatch.elapsed(TimeUnit.MINUTES) >= AUDIT_EXECUTION_TIMEOUT
        );
    }
}

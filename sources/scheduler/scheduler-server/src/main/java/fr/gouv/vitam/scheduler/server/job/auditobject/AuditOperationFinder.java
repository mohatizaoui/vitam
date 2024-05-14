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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitam.common.database.builder.query.QueryHelper;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.BadRequestException;
import fr.gouv.vitam.common.exception.InternalServerException;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.exception.VitamRuntimeException;
import fr.gouv.vitam.common.exception.WorkflowNotFoundException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.common.model.ItemStatus;
import fr.gouv.vitam.common.model.ProcessState;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.logbook.LogbookEvent;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitam.logbook.common.exception.LogbookClientException;
import fr.gouv.vitam.logbook.operations.client.LogbookOperationsClient;
import fr.gouv.vitam.logbook.operations.client.LogbookOperationsClientFactory;
import fr.gouv.vitam.processing.management.client.ProcessingManagementClient;
import fr.gouv.vitam.processing.management.client.ProcessingManagementClientFactory;

import java.util.Objects;
import java.util.Optional;

import static fr.gouv.vitam.scheduler.server.job.auditobject.LastOperationExecution.NOT_FOUND_EXECUTION;

public class AuditOperationFinder {

    public static final String LAST_UPDATE_DATE_FIELD_NAME = "Last_Update_Date";
    public static final String JOB_EXECUTION_EVENT_TYPE = "LIST_OBJECTGROUP_ID";
    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(AuditOperationFinder.class);
    private final boolean ALLOW_EXECUTION = Boolean.TRUE;
    private final LogbookOperationsClientFactory logbookOperationsClientFactory;
    private final ProcessingManagementClientFactory processingManagementClientFactory;

    public AuditOperationFinder(
        LogbookOperationsClientFactory logbookOperationsClientFactory,
        ProcessingManagementClientFactory processingManagementClientFactory
    ) {
        this.logbookOperationsClientFactory = logbookOperationsClientFactory;
        this.processingManagementClientFactory = processingManagementClientFactory;
    }

    public LastOperationExecution findLastAuditData(String auditAction) {
        try (LogbookOperationsClient logbookOperationsClient = logbookOperationsClientFactory.getClient()) {
            final ObjectNode query = buildLogbookSelectQuery(auditAction);
            final JsonNode result = logbookOperationsClient.selectOperation(query);
            return toLogbookOperation(result)
                .map(firstResult -> {
                    boolean hasCompleted = hasOperationCompleted(firstResult);
                    String lastRunDate = getLastUpdateDateByIdOperation(firstResult);
                    Integer tenantId = firstResult.getTenant();
                    String operationId = firstResult.getId();
                    return new LastOperationExecution(operationId, tenantId, lastRunDate, hasCompleted);
                })
                .orElse(NOT_FOUND_EXECUTION);
        } catch (InvalidCreateOperationException | InvalidParseOperationException | LogbookClientException e) {
            throw new VitamRuntimeException(e);
        }
    }

    private Optional<LogbookOperation> toLogbookOperation(JsonNode result) {
        try {
            RequestResponseOK<LogbookOperation> response = RequestResponseOK.getFromJsonNode(
                result,
                LogbookOperation.class
            );
            return Optional.ofNullable(response.getFirstResult());
        } catch (InvalidParseOperationException e) {
            throw new IllegalStateException("An error has occurred while parsing the operation", e);
        }
    }

    private boolean hasOperationCompleted(LogbookOperation logbookOperation) {
        try (ProcessingManagementClient processingManagementClient = processingManagementClientFactory.getClient()) {
            final ItemStatus operationProcessStatus = processingManagementClient.getOperationProcessStatus(
                logbookOperation.getId()
            );
            final ProcessState state = operationProcessStatus.getGlobalState();
            final AuditProcessState auditProcessState = AuditProcessState.wrap(state);
            return auditProcessState.hasLastWorkflowCompleted();
        } catch (WorkflowNotFoundException e) {
            LOGGER.info("Workflow not found, it must have completed long ago", e);
            return ALLOW_EXECUTION;
        } catch (VitamClientException | InternalServerException | BadRequestException e) {
            throw new VitamRuntimeException(
                "An error has occurred while trying to fetch the operation status due to :",
                e
            );
        }
    }

    private String getLastUpdateDateByIdOperation(LogbookOperation logbookOperation) {
        return logbookOperation
            .getEvents()
            .stream()
            .filter(e -> Objects.equals(e.getEvType(), JOB_EXECUTION_EVENT_TYPE))
            .map(LogbookEvent::getEvDetData)
            .map(this::getLastUpdateDateFromEvDetData)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst()
            .orElse(null);
    }

    private Optional<String> getLastUpdateDateFromEvDetData(String evDetData) {
        return Optional.ofNullable(evDetData)
            .map(data -> {
                try {
                    return JsonHandler.getFromString(data);
                } catch (InvalidParseOperationException e) {
                    throw new IllegalStateException("evDetData format issue", e);
                }
            })
            .map(e -> e.get(LAST_UPDATE_DATE_FIELD_NAME))
            .map(JsonNode::asText);
    }

    private ObjectNode buildLogbookSelectQuery(String auditAction)
        throws InvalidCreateOperationException, InvalidParseOperationException {
        final Select select = new Select();
        select.setQuery(
            QueryHelper.and()
                .add(
                    QueryHelper.eq("events." + LogbookEvent.EV_TYPE, "AUDIT_CHECK_OBJECT." + auditAction),
                    // Hack to select only audit operation started internally (from scheduler).
                    // Audit started for external APIs do have a RIGHTS_STATEMENT_IDENTIFIER.
                    QueryHelper.isNull(LogbookEvent.RIGHTS_STATEMENT_IDENTIFIER)
                )
        );
        select.addOrderByDescFilter(LogbookEvent.EV_DATE_TIME);
        select.setLimitFilter(0, 1);
        return select.getFinalSelect();
    }
}

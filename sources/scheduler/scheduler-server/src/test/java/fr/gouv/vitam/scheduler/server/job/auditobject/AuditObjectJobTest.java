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
import fr.gouv.vitam.common.VitamConfiguration;
import fr.gouv.vitam.common.exception.VitamRuntimeException;
import fr.gouv.vitam.common.exception.WorkflowNotFoundException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.AuditOptions;
import fr.gouv.vitam.common.model.ItemStatus;
import fr.gouv.vitam.common.model.ProcessState;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.StatusCode;
import fr.gouv.vitam.common.model.logbook.LogbookEventOperation;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitam.common.thread.RunWithCustomExecutor;
import fr.gouv.vitam.common.thread.RunWithCustomExecutorRule;
import fr.gouv.vitam.common.thread.VitamThreadPoolExecutor;
import fr.gouv.vitam.functional.administration.client.AdminManagementClient;
import fr.gouv.vitam.functional.administration.client.AdminManagementClientFactory;
import fr.gouv.vitam.logbook.operations.client.LogbookOperationsClient;
import fr.gouv.vitam.logbook.operations.client.LogbookOperationsClientFactory;
import fr.gouv.vitam.metadata.client.MetaDataClient;
import fr.gouv.vitam.metadata.client.MetaDataClientFactory;
import fr.gouv.vitam.processing.management.client.ProcessingManagementClient;
import fr.gouv.vitam.processing.management.client.ProcessingManagementClientFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static fr.gouv.vitam.common.model.ProcessState.COMPLETED;
import static fr.gouv.vitam.common.model.ProcessState.RUNNING;
import static fr.gouv.vitam.common.model.StatusCode.KO;
import static fr.gouv.vitam.common.model.StatusCode.OK;
import static fr.gouv.vitam.scheduler.server.job.auditobject.AuditOperationFinder.JOB_EXECUTION_EVENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AuditObjectJobTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public RunWithCustomExecutorRule runInThread = new RunWithCustomExecutorRule(
        VitamThreadPoolExecutor.getDefaultExecutor()
    );

    @Mock
    private MetaDataClientFactory metaDataClientFactory;

    @Mock
    private LogbookOperationsClientFactory logbookOperationsClientFactory;

    @Mock
    private ProcessingManagementClientFactory processingManagementClientFactory;

    @Mock
    private AdminManagementClientFactory adminManagementClientFactory;

    @Mock
    private MetaDataClient metaDataClient;

    @Mock
    private LogbookOperationsClient logbookOperationsClient;

    @Mock
    private AdminManagementClient adminManagementClient;

    @Mock
    private ProcessingManagementClient processingManagementClient;

    @Mock
    private JobExecutionContext context;

    private AuditObjectJob auditObjectJob;

    private static boolean usingLastAuditDate(AuditOptions e) {
        String queryString = e.getQuery().toString();
        return queryString.contains("DATE") && queryString.contains("UNIT_TIME");
    }

    private static boolean notUsingLastAuditDate(AuditOptions e) {
        String queryString = e.getQuery().toString();
        return !queryString.contains("DATE") && queryString.contains("UNIT_TIME");
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.reset(metaDataClient);
        Mockito.reset(adminManagementClient);
        doReturn(metaDataClient).when(metaDataClientFactory).getClient();
        doReturn(adminManagementClient).when(adminManagementClientFactory).getClient();
        auditObjectJob = new AuditObjectJob(
            adminManagementClientFactory,
            logbookOperationsClientFactory,
            processingManagementClientFactory,
            metaDataClientFactory
        );
        VitamConfiguration.setAdminTenant(1);
        VitamConfiguration.setTenants(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
        when(context.getMergedJobDataMap()).thenReturn(
            new JobDataMap(Map.of("operationsDelayInMinutes", 5, "auditType", "Integrity"))
        );
    }

    @Before
    public void setUp() {
        when(metaDataClientFactory.getClient()).thenReturn(metaDataClient);
        when(logbookOperationsClientFactory.getClient()).thenReturn(logbookOperationsClient);
        when(adminManagementClientFactory.getClient()).thenReturn(adminManagementClient);
        when(processingManagementClientFactory.getClient()).thenReturn(processingManagementClient);
    }

    @Test
    @RunWithCustomExecutor
    public void shouldNotLaunchAuditWhenProcessIsNotAllowed() throws Exception {
        // GIVEN
        VitamConfiguration.setTenants(List.of(0));
        JsonNode unit = JsonHandler.getFromString(
            "{\"#id\": \"UNIT_ID\", \"#approximate_update_date\": \"UNIT_TIME\"}"
        );
        JsonNode nonEmptyRequestResponseOK = JsonHandler.toJsonNode(new RequestResponseOK<>().addResult(unit));
        when(metaDataClient.selectUnits(any())).thenReturn(nonEmptyRequestResponseOK);

        RequestResponseOK<LogbookOperation> logbookOperationRequestResponseOK = LogbookOperationFixture.withFirstResult(
            LogbookOperationFixture.jobExecutionEvtType()
        );

        given(logbookOperationsClient.selectOperation(any())).willAnswer(
            invocationOnMock -> JsonHandler.toJsonNode(logbookOperationRequestResponseOK)
        );
        given(processingManagementClient.getOperationProcessStatus(any())).willReturn(
            LogbookOperationFixture.itemStatus(RUNNING, OK)
        );

        // WHEN
        assertThatThrownBy(() -> auditObjectJob.execute(context))
            // THEN
            .hasCauseInstanceOf(VitamRuntimeException.class)
            .hasMessageContaining("Could not run audit since another");
    }

    @Test
    @RunWithCustomExecutor
    public void shouldThrowExceptionWhenProcessDidNotFinishSuccessfully() throws Exception {
        // GIVEN
        VitamConfiguration.setTenants(List.of(0));
        JsonNode unit = JsonHandler.getFromString(
            "{\"#id\": \"UNIT_ID\", \"#approximate_update_date\": \"UNIT_TIME\"}"
        );
        JsonNode nonEmptyRequestResponseOK = JsonHandler.toJsonNode(new RequestResponseOK<>().addResult(unit));
        when(metaDataClient.selectUnits(any())).thenReturn(nonEmptyRequestResponseOK);

        RequestResponseOK<LogbookOperation> logbookOperationRequestResponseOK = LogbookOperationFixture.withFirstResult(
            LogbookOperationFixture.jobExecutionEvtType()
        );

        given(logbookOperationsClient.selectOperation(any())).willAnswer(
            invocationOnMock -> JsonHandler.toJsonNode(logbookOperationRequestResponseOK)
        );
        given(processingManagementClient.getOperationProcessStatus(any())).willReturn(
            LogbookOperationFixture.itemStatus(COMPLETED, KO)
        );

        // WHEN
        assertThatThrownBy(() -> auditObjectJob.execute(context))
            // THEN
            .isInstanceOf(JobExecutionException.class)
            .hasMessageContaining("At least one tenant has integrity audit failed");
    }

    @Test
    @RunWithCustomExecutor
    public void shouldLaunchWorkflowWhenWorkflowIsNotFound() throws Exception {
        // GIVEN
        VitamConfiguration.setTenants(List.of(0));
        JsonNode unit = JsonHandler.getFromString(
            "{\"#id\": \"UNIT_ID\", \"#approximate_update_date\": \"UNIT_TIME\"}"
        );
        JsonNode nonEmptyRequestResponseOK = JsonHandler.toJsonNode(new RequestResponseOK<>().addResult(unit));
        when(metaDataClient.selectUnits(any())).thenReturn(nonEmptyRequestResponseOK);

        RequestResponseOK<LogbookOperation> logbookOperationRequestResponseOK = LogbookOperationFixture.withFirstResult(
            LogbookOperationFixture.jobExecutionEvtType()
        );

        given(logbookOperationsClient.selectOperation(any())).willAnswer(
            invocationOnMock -> JsonHandler.toJsonNode(logbookOperationRequestResponseOK)
        );
        given(processingManagementClient.getOperationProcessStatus(any())).willThrow(new WorkflowNotFoundException(""));

        // WHEN
        auditObjectJob.execute(context);

        // THEN
        verify(adminManagementClient).launchAuditWorkflow(
            ArgumentMatchers.argThat(AuditObjectJobTest::usingLastAuditDate),
            eq(false)
        );
    }

    @Test
    @RunWithCustomExecutor
    public void shouldLaunchWorkflowWithoutLastAuditDateWhenNull() throws Exception {
        // GIVEN
        VitamConfiguration.setTenants(List.of(0));
        JsonNode unit = JsonHandler.getFromString(
            "{\"#id\": \"UNIT_ID\", \"#approximate_update_date\": \"UNIT_TIME\"}"
        );
        JsonNode nonEmptyRequestResponseOK = JsonHandler.toJsonNode(new RequestResponseOK<>().addResult(unit));
        when(metaDataClient.selectUnits(any())).thenReturn(nonEmptyRequestResponseOK);

        RequestResponseOK<LogbookOperation> logbookOperationRequestResponseOK = LogbookOperationFixture.withFirstResult(
            LogbookOperationFixture.jobExecutionEvtTypeWithoutDate()
        );

        given(logbookOperationsClient.selectOperation(any())).willAnswer(
            invocationOnMock -> JsonHandler.toJsonNode(logbookOperationRequestResponseOK)
        );
        given(processingManagementClient.getOperationProcessStatus(any())).willReturn(
            LogbookOperationFixture.itemStatus(COMPLETED, OK)
        );

        // WHEN
        auditObjectJob.execute(context);

        // THEN
        verify(adminManagementClient).launchAuditWorkflow(
            ArgumentMatchers.argThat(AuditObjectJobTest::notUsingLastAuditDate),
            eq(false)
        );
    }

    @Test
    @RunWithCustomExecutor
    public void shouldUseSelectedTenantsWhenProvidedAndValid() throws Exception {
        // GIVEN
        VitamConfiguration.setTenants(List.of(0));
        JsonNode unit = JsonHandler.getFromString(
            "{\"#id\": \"UNIT_ID\", \"#approximate_update_date\": \"UNIT_TIME\"}"
        );
        JsonNode nonEmptyRequestResponseOK = JsonHandler.toJsonNode(new RequestResponseOK<>().addResult(unit));
        when(metaDataClient.selectUnits(any())).thenReturn(nonEmptyRequestResponseOK);

        RequestResponseOK<LogbookOperation> logbookOperationRequestResponseOK = LogbookOperationFixture.withFirstResult(
            LogbookOperationFixture.jobExecutionEvtTypeWithoutDate()
        );

        given(logbookOperationsClient.selectOperation(any())).willAnswer(
            invocationOnMock -> JsonHandler.toJsonNode(logbookOperationRequestResponseOK)
        );
        given(processingManagementClient.getOperationProcessStatus(any())).willReturn(
            LogbookOperationFixture.itemStatus(COMPLETED, OK)
        );

        // WHEN
        auditObjectJob.execute(context);

        // THEN
        verify(adminManagementClient).launchAuditWorkflow(
            ArgumentMatchers.argThat(AuditObjectJobTest::notUsingLastAuditDate),
            eq(false)
        );
    }

    @Test
    public void shouldReturnSelectedTenantsWhenProvidedValid() {
        VitamConfiguration.setTenants(Arrays.asList(1, 2, 3, 4, 5));
        when(context.getMergedJobDataMap()).thenReturn(
            new JobDataMap(Map.of("selectedTenants", "1,2,3", "operationsDelayInMinutes", 5, "auditType", "Integrity"))
        );

        List<Integer> result = auditObjectJob.getTenantsToAudit(context);

        assertThat(Arrays.asList(1, 2, 3)).isEqualTo(result);
    }

    @Test
    public void shouldReturnDefaultTenantsWhenSelectedTenantsAreInvalid() {
        VitamConfiguration.setTenants(Arrays.asList(1, 2, 3, 4, 5));
        when(context.getMergedJobDataMap()).thenReturn(
            new JobDataMap(Map.of("selectedTenants", "a,b,c", "operationsDelayInMinutes", 5, "auditType", "Integrity"))
        );

        List<Integer> result = auditObjectJob.getTenantsToAudit(context);

        assertThat(Arrays.asList(1, 2, 3, 4, 5)).isEqualTo(result);
    }

    @Test
    public void shouldReturnDefaultTenantsWhenNoSelectedTenantsProvided() {
        VitamConfiguration.setTenants(Arrays.asList(1, 2, 3, 4, 5));
        when(context.getMergedJobDataMap()).thenReturn(
            new JobDataMap(Map.of("selectedTenants", "", "operationsDelayInMinutes", 5, "auditType", "Integrity"))
        );

        List<Integer> result = auditObjectJob.getTenantsToAudit(context);

        assertThat(Arrays.asList(1, 2, 3, 4, 5)).isEqualTo(result);
    }

    @Test
    public void shouldReturnDefaultTenantsWhenSelectedTenantsKeyIsNull() {
        VitamConfiguration.setTenants(Arrays.asList(1, 2, 3, 4, 5));
        when(context.getMergedJobDataMap()).thenReturn(
            new JobDataMap(Map.of("operationsDelayInMinutes", 5, "auditType", "Integrity"))
        );

        List<Integer> result = auditObjectJob.getTenantsToAudit(context);

        assertThat(Arrays.asList(1, 2, 3, 4, 5)).isEqualTo(result);
    }

    private static class LogbookOperationFixture {

        public static final String DATE = "DATE";
        public static final String LOG_OP_ID = "ID";

        public static RequestResponseOK<LogbookOperation> withFirstResult(LogbookOperation logbookOperation) {
            return new RequestResponseOK<LogbookOperation>().addResult(logbookOperation);
        }

        public static LogbookOperation jobExecutionEvtType() {
            LogbookOperation logbookOperation = new LogbookOperation();
            logbookOperation.setId(LOG_OP_ID);
            List<LogbookEventOperation> events = new ArrayList<>();
            events.add(logbookEvent(JOB_EXECUTION_EVENT_TYPE, DATE));
            logbookOperation.setEvents(events);
            return logbookOperation;
        }

        public static LogbookOperation jobExecutionEvtTypeWithoutDate() {
            LogbookOperation logbookOperation = new LogbookOperation();
            logbookOperation.setId(LOG_OP_ID);
            List<LogbookEventOperation> events = new ArrayList<>();
            events.add(logbookEvent(JOB_EXECUTION_EVENT_TYPE, ""));
            logbookOperation.setEvents(events);
            return logbookOperation;
        }

        public static LogbookEventOperation logbookEvent(String type, String lastUpdateDate) {
            LogbookEventOperation logbookEvent = new LogbookEventOperation();
            logbookEvent.setEvType(type);
            logbookEvent.setEvDetData("{\"Last_Update_Date\": \"" + lastUpdateDate + "\"}");
            return logbookEvent;
        }

        public static ItemStatus itemStatus(ProcessState globalStatus, StatusCode statusCode) {
            ItemStatus itemStatus = new ItemStatus();
            itemStatus.setGlobalState(globalStatus);
            itemStatus.increment(statusCode);
            return itemStatus;
        }
    }
}

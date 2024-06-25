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

package fr.gouv.vitam.referential;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import fr.gouv.vitam.access.external.client.AccessExternalClient;
import fr.gouv.vitam.access.external.client.AccessExternalClientFactory;
import fr.gouv.vitam.access.external.rest.AccessExternalMain;
import fr.gouv.vitam.access.internal.client.AccessInternalClient;
import fr.gouv.vitam.access.internal.client.AccessInternalClientFactory;
import fr.gouv.vitam.access.internal.common.exception.AccessInternalClientNotFoundException;
import fr.gouv.vitam.access.internal.common.exception.AccessInternalClientServerException;
import fr.gouv.vitam.access.internal.rest.AccessInternalMain;
import fr.gouv.vitam.batch.report.rest.BatchReportMain;
import fr.gouv.vitam.common.DataLoader;
import fr.gouv.vitam.common.VitamRuleRunner;
import fr.gouv.vitam.common.VitamServerRunner;
import fr.gouv.vitam.common.VitamTestHelper;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.query.QueryHelper;
import fr.gouv.vitam.common.database.builder.query.VitamFieldsHelper;
import fr.gouv.vitam.common.database.builder.query.action.UpdateActionHelper;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.multiple.SelectMultiQuery;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.database.builder.request.single.Update;
import fr.gouv.vitam.common.elasticsearch.ElasticsearchRule;
import fr.gouv.vitam.common.exception.AccessUnauthorizedException;
import fr.gouv.vitam.common.exception.BadRequestException;
import fr.gouv.vitam.common.exception.InternalServerException;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.ProcessAction;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.StatusCode;
import fr.gouv.vitam.common.model.administration.IngestContractModel;
import fr.gouv.vitam.common.model.massupdate.MassUpdateUnitRuleRequest;
import fr.gouv.vitam.common.model.massupdate.RuleActions;
import fr.gouv.vitam.common.model.massupdate.RuleCategoryAction;
import fr.gouv.vitam.common.thread.RunWithCustomExecutor;
import fr.gouv.vitam.common.thread.VitamThreadUtils;
import fr.gouv.vitam.functional.administration.client.AdminManagementClient;
import fr.gouv.vitam.functional.administration.client.AdminManagementClientFactory;
import fr.gouv.vitam.functional.administration.rest.AdminManagementMain;
import fr.gouv.vitam.ingest.external.rest.IngestExternalMain;
import fr.gouv.vitam.ingest.internal.upload.rest.IngestInternalMain;
import fr.gouv.vitam.logbook.common.exception.LogbookClientAlreadyExistsException;
import fr.gouv.vitam.logbook.common.exception.LogbookClientBadRequestException;
import fr.gouv.vitam.logbook.common.exception.LogbookClientServerException;
import fr.gouv.vitam.logbook.rest.LogbookMain;
import fr.gouv.vitam.metadata.client.MetaDataClient;
import fr.gouv.vitam.metadata.client.MetaDataClientFactory;
import fr.gouv.vitam.metadata.rest.MetadataMain;
import fr.gouv.vitam.processing.management.rest.ProcessManagementMain;
import fr.gouv.vitam.worker.server.rest.WorkerMain;
import fr.gouv.vitam.workspace.api.exception.ContentAddressableStorageServerException;
import fr.gouv.vitam.workspace.rest.WorkspaceMain;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static fr.gouv.vitam.common.VitamTestHelper.doIngest;
import static fr.gouv.vitam.common.VitamTestHelper.prepareVitamSession;
import static fr.gouv.vitam.common.VitamTestHelper.verifyOperation;
import static fr.gouv.vitam.common.VitamTestHelper.waitOperation;
import static fr.gouv.vitam.common.guid.GUIDFactory.newOperationLogbookGUID;
import static fr.gouv.vitam.common.model.StatusCode.OK;
import static fr.gouv.vitam.logbook.common.parameters.Contexts.FILING_SCHEME;
import static fr.gouv.vitam.logbook.common.parameters.Contexts.HOLDING_SCHEME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

public class AccessContractRestrictionChecksIT extends VitamRuleRunner {

    private static final Integer tenantId = 0;
    private static final String SIP_HOLDING_SCHEME = "integration-ingest-internal/arbre_simple.zip";
    private static final String SIP_FILING_SCHEME = "integration-ingest-internal/plan_simple.zip";
    private static final String SIP_SIMPLE = "integration-ingest-internal/arbo_simple.zip";

    @ClassRule
    public static VitamServerRunner runner = new VitamServerRunner(
        AccessContractRestrictionChecksIT.class,
        mongoRule.getMongoDatabase().getName(),
        ElasticsearchRule.getClusterName(),
        Sets.newHashSet(
            MetadataMain.class,
            WorkerMain.class,
            BatchReportMain.class,
            AdminManagementMain.class,
            LogbookMain.class,
            WorkspaceMain.class,
            ProcessManagementMain.class,
            AccessInternalMain.class,
            IngestInternalMain.class,
            AccessExternalMain.class,
            IngestExternalMain.class
        )
    );

    private static AccessExternalClient accessExternalClient;
    private static AccessInternalClient accessInternalClient;
    private static MetaDataClient metadataClient;
    private static AdminManagementClient adminManagementClient;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        handleBeforeClass(Arrays.asList(0, 1), Collections.emptyMap());
        accessExternalClient = AccessExternalClientFactory.getInstance().getClient();
        accessInternalClient = AccessInternalClientFactory.getInstance().getClient();
        metadataClient = MetaDataClientFactory.getInstance().getClient();
        adminManagementClient = AdminManagementClientFactory.getInstance().getClient();

        new DataLoader("integration-ingest-internal").prepareData();
    }

    @AfterClass
    public static void tearDownAfterClass() {
        if (accessExternalClient != null) {
            accessExternalClient.close();
        }
        if (accessInternalClient != null) {
            accessInternalClient.close();
        }
        if (metadataClient != null) {
            metadataClient.close();
        }
        if (adminManagementClient != null) {
            adminManagementClient.close();
        }

        handleAfterClass();
        runAfter();
        fr.gouv.vitam.common.client.VitamClientFactory.resetConnections();
        fr.gouv.vitam.common.external.client.VitamClientFactory.resetConnections();
    }

    @RunWithCustomExecutor
    @Test
    public void testAccessContractRestrictions() throws Exception {
        // Setup
        /*
         *                 (HoldingUnit)
         *                  /         \
         *     (FilingScheme1)        (FilingScheme2)
         *           |
         *        (UnitA)
         *        /        \
         *   (UnitB)    (UnitC)
         *      |          |    \
         *    [GotB]     [GotC]  (UnitD)
         *
         * HoldingUnit:   No originating agency / No Rules
         * FilingScheme1: SP1 / No rules
         * FilingScheme2: SP1 / AppraisalRule with expired MaxEndDate
         * UnitA:         SP2 / No rules
         * UnitB:         SP2 / AppraisalRule with non-expired MaxEndDate
         * UnitC:         SP2 / AppraisalRule with expired MaxEndDate
         * UnitD:         SP2 / Invalid computed inherited rules
         */

        // Ingest holding schema
        String holdingSchemeIngestOperationId = doIngestOfHoldingScheme();

        Map<String, String> holdingSchemeUnitIds = getUnitIdsByTitle(holdingSchemeIngestOperationId);
        assertThat(holdingSchemeUnitIds).containsOnlyKeys("Arbre simple");
        String holdingSchemeUnitId = holdingSchemeUnitIds.get("Arbre simple");
        assertNotNull(holdingSchemeUnitId);

        // Update IngestContract LinkParentId & ingest filing scheme under holdingSchemeUnitId
        VitamThreadUtils.getVitamSession().setRequestId(newOperationLogbookGUID(tenantId));
        updateIngestContractLinkParentId("IngestContractWithHoldingScheme", holdingSchemeUnitId);

        String filingSchemeIngestOperationId = doIngestOfFilingSchemeReturningUnitIdsByTitle();

        Map<String, String> filingSchemeUnitIds = getUnitIdsByTitle(filingSchemeIngestOperationId);
        assertThat(filingSchemeUnitIds).containsOnlyKeys("FilingScheme1", "FilingScheme2");
        String filingScheme1 = filingSchemeUnitIds.get("FilingScheme1");
        String filingScheme2 = filingSchemeUnitIds.get("FilingScheme2");

        // Update IngestContract LinkParentId & ingest SIP under FilingScheme1 unit
        VitamThreadUtils.getVitamSession().setRequestId(newOperationLogbookGUID(tenantId));
        updateIngestContractLinkParentId("IngestContractWithFilingScheme", filingSchemeUnitIds.get("FilingScheme1"));

        String operationId = doIngest(tenantId, SIP_SIMPLE);
        verifyOperation(operationId, OK);

        Map<String, String> simpleSipUnitIds = getUnitIdsByTitle(operationId);
        assertThat(simpleSipUnitIds).containsOnlyKeys("UnitA", "UnitB", "UnitC", "UnitD");
        String unitA = simpleSipUnitIds.get("UnitA");
        String unitB = simpleSipUnitIds.get("UnitB");
        String unitC = simpleSipUnitIds.get("UnitC");
        String unitD = simpleSipUnitIds.get("UnitD");

        String[] operationIds = new String[] {
            holdingSchemeIngestOperationId,
            filingSchemeIngestOperationId,
            operationId,
        };

        // Compute inherited rules (except for UnitD)
        computeInheritedRules(filingScheme1, filingScheme2, unitA, unitB, unitC);

        // Invalidate computed inherited rules for UnitD
        updateUnitRules(unitD);

        // Case 1: OriginatingAgency filter + Rule filter
        List<String> foundUnitIds = search("ac_filter_originating_agencies_and_rules", operationIds);
        assertThat(foundUnitIds).containsExactlyInAnyOrder(holdingSchemeUnitId, unitC);

        // Case 2: OriginatingAgency filter
        foundUnitIds = search("ac_filter_originating_agencies", operationIds);
        assertThat(foundUnitIds).containsExactlyInAnyOrder(holdingSchemeUnitId, unitA, unitB, unitC, unitD);

        // Case 3: OriginatingAgency filter skip filing scheme
        foundUnitIds = search("ac_filter_originating_agencies_except_filing_scheme", operationIds);
        assertThat(foundUnitIds).containsExactlyInAnyOrder(
            holdingSchemeUnitId,
            filingScheme1,
            filingScheme2,
            unitA,
            unitB,
            unitC,
            unitD
        );

        // Case 4: Rule filter
        foundUnitIds = search("ac_filter_rules", operationIds);
        assertThat(foundUnitIds).containsExactlyInAnyOrder(holdingSchemeUnitId, filingScheme2, unitC);

        // Case 5: Rule filter skip filing scheme
        foundUnitIds = search("ac_filter_rules_except_filing_scheme", operationIds);
        assertThat(foundUnitIds).containsExactlyInAnyOrder(holdingSchemeUnitId, filingScheme1, filingScheme2, unitC);
    }

    private static List<String> search(String accessContractId, String... operationIds)
        throws InvalidCreateOperationException, InvalidParseOperationException, AccessInternalClientServerException, AccessInternalClientNotFoundException, AccessUnauthorizedException, BadRequestException {
        VitamThreadUtils.getVitamSession().setContractId(accessContractId);

        // When
        SelectMultiQuery query = new SelectMultiQuery();
        query.addQueries(QueryHelper.in(VitamFieldsHelper.initialOperation(), operationIds));
        query.addUsedProjection("#id");
        List<JsonNode> results =
            ((RequestResponseOK<JsonNode>) accessInternalClient.selectUnits(query.getFinalSelect())).getResults();

        return results.stream().map(n -> n.get("#id").asText()).collect(Collectors.toList());
    }

    private static void computeInheritedRules(String... unitIds)
        throws InvalidCreateOperationException, LogbookClientAlreadyExistsException, VitamClientException, InternalServerException, ContentAddressableStorageServerException, BadRequestException, InvalidParseOperationException, LogbookClientBadRequestException, LogbookClientServerException {
        SelectMultiQuery select = new SelectMultiQuery();
        select.addQueries(QueryHelper.in(VitamFieldsHelper.id(), unitIds));
        VitamTestHelper.computeInheritedRules(select);
    }

    private static String doIngestOfFilingSchemeReturningUnitIdsByTitle() throws Exception {
        prepareVitamSession(tenantId, "aName3", "Context_IT");
        final String operationGuid = doIngest(
            tenantId,
            SIP_FILING_SCHEME,
            FILING_SCHEME,
            ProcessAction.RESUME,
            StatusCode.STARTED
        );
        waitOperation(operationGuid);
        verifyOperation(operationGuid, OK);
        return operationGuid;
    }

    private static String doIngestOfHoldingScheme() throws Exception {
        prepareVitamSession(tenantId, "aName3", "Context_IT");
        final String operationGuid = doIngest(
            tenantId,
            SIP_HOLDING_SCHEME,
            HOLDING_SCHEME,
            ProcessAction.RESUME,
            StatusCode.STARTED
        );
        waitOperation(operationGuid);
        verifyOperation(operationGuid, OK);

        return operationGuid;
    }

    private static Map<String, String> getUnitIdsByTitle(String operationGuid) throws Exception {
        final JsonNode node = getArchiveUnitWithOpi(operationGuid);

        return RequestResponseOK.getFromJsonNode(node)
            .getResults()
            .stream()
            .collect(Collectors.toMap(n -> n.get("Title").asText(), n -> n.get("#id").asText()));
    }

    private static void updateIngestContractLinkParentId(String contractId, String linkParentId) throws Exception {
        final Update updateLinkParent = new Update();
        updateLinkParent.setQuery(QueryHelper.eq("Identifier", contractId));
        updateLinkParent.addActions(UpdateActionHelper.set(IngestContractModel.LINK_PARENT_ID, linkParentId));
        adminManagementClient.updateIngestContract(contractId, updateLinkParent.getFinalUpdate());
    }

    private static JsonNode getArchiveUnitWithOpi(String opi) throws Exception {
        Select selectQuery = new Select();
        selectQuery.setQuery(QueryHelper.eq("#opi", opi));
        return metadataClient.selectUnits(selectQuery.getFinalSelect());
    }

    private static void updateUnitRules(String unitId)
        throws InvalidCreateOperationException, VitamClientException, InvalidParseOperationException {
        MassUpdateUnitRuleRequest massUpdateUnitRuleRequest = new MassUpdateUnitRuleRequest();

        RuleActions ruleActions = new RuleActions();
        ruleActions.getAdd().add(Map.of("AccessRule", new RuleCategoryAction().setPreventInheritance(true)));
        massUpdateUnitRuleRequest.setRuleActions(ruleActions);

        SelectMultiQuery selectMultiQuery = new SelectMultiQuery();
        selectMultiQuery.addQueries(QueryHelper.eq(VitamFieldsHelper.id(), unitId));
        ObjectNode finalSelect = selectMultiQuery.getFinalSelect();
        finalSelect.remove("$projection");
        finalSelect.remove("$filter");
        finalSelect.remove("$facets");
        massUpdateUnitRuleRequest.setDslRequest(finalSelect);

        RequestResponse<JsonNode> requestResponse = accessExternalClient.massUpdateUnitsRules(
            new VitamContext(tenantId).setAccessContract("aName3"),
            JsonHandler.toJsonNode(massUpdateUnitRuleRequest)
        );
        assertThat(requestResponse.isOk()).isTrue();
    }
}

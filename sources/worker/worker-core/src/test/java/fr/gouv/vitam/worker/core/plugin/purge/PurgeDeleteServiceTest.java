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
package fr.gouv.vitam.worker.core.plugin.purge;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import fr.gouv.vitam.common.VitamConfiguration;
import fr.gouv.vitam.common.database.builder.query.VitamFieldsHelper;
import fr.gouv.vitam.common.guid.GUIDFactory;
import fr.gouv.vitam.common.model.objectgroup.ObjectGroupResponse;
import fr.gouv.vitam.common.model.objectgroup.QualifiersModel;
import fr.gouv.vitam.common.model.objectgroup.StorageJson;
import fr.gouv.vitam.common.model.objectgroup.StorageRacineModel;
import fr.gouv.vitam.common.model.objectgroup.VersionsModel;
import fr.gouv.vitam.common.thread.RunWithCustomExecutor;
import fr.gouv.vitam.common.thread.RunWithCustomExecutorRule;
import fr.gouv.vitam.common.thread.VitamThreadPoolExecutor;
import fr.gouv.vitam.common.thread.VitamThreadUtils;
import fr.gouv.vitam.logbook.lifecycles.client.LogbookLifeCyclesClient;
import fr.gouv.vitam.logbook.lifecycles.client.LogbookLifeCyclesClientFactory;
import fr.gouv.vitam.metadata.client.MetaDataClient;
import fr.gouv.vitam.metadata.client.MetaDataClientFactory;
import fr.gouv.vitam.storage.engine.client.StorageClient;
import fr.gouv.vitam.storage.engine.client.StorageClientFactory;
import fr.gouv.vitam.storage.engine.common.model.DataCategory;
import fr.gouv.vitam.worker.common.HandlerIO;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PurgeDeleteServiceTest {

    @Rule
    public RunWithCustomExecutorRule runInThread = new RunWithCustomExecutorRule(
        VitamThreadPoolExecutor.getDefaultExecutor()
    );

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private MetaDataClientFactory metaDataClientFactory;

    @Mock
    private MetaDataClient metaDataClient;

    @Mock
    private StorageClientFactory storageClientFactory;

    @Mock
    private StorageClient storageClient;

    @Mock
    private LogbookLifeCyclesClientFactory logbookLifeCyclesClientFactory;

    @Mock
    private LogbookLifeCyclesClient logbookLifeCyclesClient;

    @InjectMocks
    private PurgeDeleteService instance;

    @Mock
    HandlerIO handlerIO;

    private ObjectMapper mapper;

    @Before
    public void setUp() throws Exception {
        VitamThreadUtils.getVitamSession().setTenantId(0);
        VitamThreadUtils.getVitamSession().setRequestId(GUIDFactory.newRequestIdGUID(0));
        doReturn(storageClient).when(handlerIO).getStorageClient();
        doReturn(metaDataClient).when(handlerIO).getMetaDataClient();
        doReturn(metaDataClient).when(metaDataClientFactory).getClient();
        doReturn(storageClient).when(storageClientFactory).getClient();
        doReturn(logbookLifeCyclesClient).when(logbookLifeCyclesClientFactory).getClient();
        when(handlerIO.getLifeCyclesClient()).thenReturn(logbookLifeCyclesClient);
        when(handlerIO.getMetaDataClient()).thenReturn(metaDataClient);
        mapper = new ObjectMapper();
    }

    @Test
    @RunWithCustomExecutor
    public void deleteObjects() throws Exception {
        List<PurgeObjectGroupParams> purgeObjectGroupParams = createPurgeObjGroupParams();

        instance.deleteObjects(purgeObjectGroupParams, handlerIO);

        verify(storageClient).delete(VitamConfiguration.getDefaultStrategy(), DataCategory.OBJECT, "id11");
        verify(storageClient).delete(VitamConfiguration.getDefaultStrategy(), DataCategory.OBJECT, "id21");
        verify(storageClient).delete(VitamConfiguration.getDefaultStrategy(), DataCategory.OBJECT, "id31");

        verify(storageClient).delete(VitamConfiguration.getDefaultStrategy(), DataCategory.OBJECT, "id12");
        verify(storageClient).delete(VitamConfiguration.getDefaultStrategy(), DataCategory.OBJECT, "id22");
        verify(storageClient).delete(VitamConfiguration.getDefaultStrategy(), DataCategory.OBJECT, "id32");

        verify(storageClient).delete(VitamConfiguration.getDefaultStrategy(), DataCategory.OBJECT, "id13");
        verify(storageClient).delete(VitamConfiguration.getDefaultStrategy(), DataCategory.OBJECT, "id23");
        verify(storageClient).delete(VitamConfiguration.getDefaultStrategy(), DataCategory.OBJECT, "id33");
    }

    @Test
    @RunWithCustomExecutor
    public void deleteObjectGroups() throws Exception {
        List<PurgeObjectGroupParams> purgeObjectGroupParams = createPurgeObjGroupParams();

        instance.deleteObjectGroups(purgeObjectGroupParams, handlerIO);

        verify(logbookLifeCyclesClient).deleteLifecycleObjectGroupBulk(eq(List.of("id1", "id2", "id3")));
        verify(metaDataClient).deleteObjectGroupBulk(eq(List.of("id1", "id2", "id3")));

        verify(storageClient).delete(VitamConfiguration.getDefaultStrategy(), DataCategory.OBJECTGROUP, "id1.json");
        verify(storageClient).delete(VitamConfiguration.getDefaultStrategy(), DataCategory.OBJECTGROUP, "id2.json");
        verify(storageClient).delete(VitamConfiguration.getDefaultStrategy(), DataCategory.OBJECTGROUP, "id3.json");
    }

    private List<PurgeObjectGroupParams> createPurgeObjGroupParams() {
        List<PurgeObjectGroupParams> results = new ArrayList<>();
        for (int ogId = 1; ogId <= 3; ogId++) {
            ObjectGroupResponse objectGroupResponse = new ObjectGroupResponse();
            objectGroupResponse.setId("id" + ogId);
            List<QualifiersModel> qualifiers = new ArrayList<>();
            for (int i = 1; i <= 3; i++) {
                QualifiersModel qualifier = new QualifiersModel();
                qualifier.setQualifier("id" + i + ogId);
                VersionsModel versionsModel1 = new VersionsModel();
                versionsModel1.setId("id" + i + ogId);
                StorageJson storageJson = new StorageJson();
                storageJson.setStrategyId(VitamConfiguration.getDefaultStrategy());
                versionsModel1.setStorage(storageJson);
                qualifier.setVersions(List.of(versionsModel1));
                qualifiers.add(qualifier);
            }

            objectGroupResponse.setQualifiers(qualifiers);

            StorageRacineModel storageJsonOg = new StorageRacineModel();
            storageJsonOg.setStrategyId(VitamConfiguration.getDefaultStrategy());
            objectGroupResponse.setStorage(storageJsonOg);

            results.add(PurgeObjectGroupParams.fromObjectGroup(objectGroupResponse));
        }
        return results;
    }

    @Test
    @RunWithCustomExecutor
    public void deleteUnits() throws Exception {
        Map<String, String> unitIdsMap = ImmutableMap.of("unit1", "default", "unit2", "default", "unit3", "default");
        instance.deleteUnits(unitIdsMap, handlerIO);

        verify(logbookLifeCyclesClient).deleteLifecycleUnitsBulk(eq(unitIdsMap.keySet()));
        verify(metaDataClient).deleteUnitsBulk(eq(unitIdsMap.keySet()));

        verify(storageClient).delete(VitamConfiguration.getDefaultStrategy(), DataCategory.UNIT, "unit1.json");
        verify(storageClient).delete(VitamConfiguration.getDefaultStrategy(), DataCategory.UNIT, "unit2.json");
        verify(storageClient).delete(VitamConfiguration.getDefaultStrategy(), DataCategory.UNIT, "unit3.json");
    }

    @Test
    @RunWithCustomExecutor
    public void detachObjectGroupFromDeleteParentUnits() throws Exception {
        String gotId = GUIDFactory.newGUID().toString();
        instance.detachObjectGroupFromDeleteParentUnits(
            gotId,
            new HashSet<>(Arrays.asList("unit1", "unit2")),
            handlerIO
        );

        verify(metaDataClient).updateObjectGroupById(any(), eq(gotId));
    }

    private JsonNode buildUnitParams(Integer index) {
        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.put("_id", "aeaqaaaaaaecyk5aabsfwamuihjv6gyaaab" + index);
        rootNode.put("_opi", "aeeaaaaaacec226xado2aamuihjvawqaaaa" + index);

        // Create the nested _storage object
        ObjectNode storageNode = mapper.createObjectNode();
        storageNode.put("strategyId", "default");
        rootNode.set(VitamFieldsHelper.storage(), storageNode);

        rootNode.put("_sp", "agency" + index);

        return rootNode;
    }
}

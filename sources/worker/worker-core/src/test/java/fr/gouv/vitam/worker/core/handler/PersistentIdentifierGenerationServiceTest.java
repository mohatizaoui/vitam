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
package fr.gouv.vitam.worker.core.handler;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.administration.DataObjectVersionType;
import fr.gouv.vitam.common.model.administration.ManagementContractModel;
import fr.gouv.vitam.common.model.administration.PersistentIdentifierPolicy;
import fr.gouv.vitam.common.model.administration.PersistentIdentifierPolicyTypeEnum;
import fr.gouv.vitam.common.model.administration.PersistentIdentifierUsage;
import fr.gouv.vitam.common.model.administration.VersionUsageModel;
import fr.gouv.vitam.common.thread.RunWithCustomExecutor;
import org.apache.commons.collections4.IteratorUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PersistentIdentifierGenerationServiceTest {

    private static final String MULTI_USAGES_OBJECTS = "PersistentIdentifiers/objects-multi-usages.json";

    private PersistentIdentifierGenerationService persistentIdentifierGenerationService =
        new PersistentIdentifierGenerationService();

    private List<JsonNode> multiUsagesObjects;

    @Before
    public void setUp() throws Exception {
        final File multiUsagesObjectsFile = PropertiesUtils.getResourceFile(MULTI_USAGES_OBJECTS);
        multiUsagesObjects = IteratorUtils.toList(JsonHandler.getFromFile(multiUsagesObjectsFile).elements());
    }

    private List<JsonNode> filterObjectsNodesByQualifierPrefix(List<JsonNode> initialNodes, String qualifierPrefix) {
        return initialNodes
            .stream()
            .filter(
                node ->
                    node.has("DataObjectVersion") && node.get("DataObjectVersion").asText().startsWith(qualifierPrefix)
            )
            .collect(Collectors.toList());
    }

    @Test
    @RunWithCustomExecutor
    public void should_generate_only_version_1_when_management_contract_allow_initial_version_only()
        throws InvalidParseOperationException {
        ManagementContractModel managementContractModel = new ManagementContractModel();
        managementContractModel.setIdentifier("test mgt contract");
        managementContractModel.setId("test mgt contract");
        PersistentIdentifierPolicy initialPolicy = new PersistentIdentifierPolicy();
        initialPolicy.setPersistentIdentifierAuthority("NAAN-TEST");
        initialPolicy.setPersistentIdentifierPolicyType(PersistentIdentifierPolicyTypeEnum.ARK);
        PersistentIdentifierUsage initialVersionUsage = new PersistentIdentifierUsage();
        initialVersionUsage.setUsageName(DataObjectVersionType.BINARY_MASTER);
        initialVersionUsage.setInitialVersion(true);
        initialPolicy.setPersistentIdentifierUsages(List.of(initialVersionUsage));
        managementContractModel.setPersistentIdentifierPolicyList(List.of(initialPolicy));

        Map<String, List<JsonNode>> objectsByQualifierMap = convertListJsonNodeMapByUsage(multiUsagesObjects);

        persistentIdentifierGenerationService.handlePersistentIdentifierForGot(
            objectsByQualifierMap,
            managementContractModel,
            PersistentIdentifierPolicyTypeEnum.ARK
        );
        List<JsonNode> initialVersionsResults = filterObjectsNodesByQualifierPrefix(
            multiUsagesObjects,
            "BinaryMaster_1"
        );
        assertNotNull(initialVersionsResults);
        initialVersionsResults
            .stream()
            .forEach(qualifier -> {
                Assert.assertNotNull(qualifier.get("PersistentIdentifier"));
                JsonNode persistentNode = qualifier.get("PersistentIdentifier");
                assertEquals("NAAN-TEST", persistentNode.get(0).get("PersistentIdentifierReference").asText());
                assertEquals(
                    "ark:/NAAN-TEST/BinaryMaster_1",
                    persistentNode.get(0).get("PersistentIdentifierContent").asText()
                );
            });
    }

    @Test
    @RunWithCustomExecutor
    public void should_generate_version_1_and_last_when_management_contract_allow_initial_version_and_last()
        throws InvalidParseOperationException {
        ManagementContractModel managementContractModel = new ManagementContractModel();
        managementContractModel.setIdentifier("test mgt contract");
        managementContractModel.setId("test mgt contract");
        PersistentIdentifierPolicy initialPolicy = new PersistentIdentifierPolicy();
        initialPolicy.setPersistentIdentifierAuthority("NAAN-TEST");
        initialPolicy.setPersistentIdentifierPolicyType(PersistentIdentifierPolicyTypeEnum.ARK);
        PersistentIdentifierUsage binaryMasterPolicy = new PersistentIdentifierUsage();
        binaryMasterPolicy.setUsageName(DataObjectVersionType.BINARY_MASTER);
        binaryMasterPolicy.setInitialVersion(true);
        binaryMasterPolicy.setIntermediaryVersion(VersionUsageModel.IntermediaryVersionEnum.LAST);
        initialPolicy.setPersistentIdentifierUsages(List.of(binaryMasterPolicy));
        managementContractModel.setPersistentIdentifierPolicyList(List.of(initialPolicy));

        Map<String, List<JsonNode>> objectsByQualifierMap = convertListJsonNodeMapByUsage(multiUsagesObjects);

        persistentIdentifierGenerationService.handlePersistentIdentifierForGot(
            objectsByQualifierMap,
            managementContractModel,
            PersistentIdentifierPolicyTypeEnum.ARK
        );
        List<JsonNode> generatedResults = filterObjectsNodesByQualifierPrefix(multiUsagesObjects, "BinaryMaster_");
        assertNotNull(generatedResults);
        generatedResults
            .stream()
            .filter(
                element ->
                    element.has("DataObjectVersion") &&
                    !"BinaryMaster_2".equals(element.get("DataObjectVersion").asText())
            )
            .forEach(qualifier -> {
                Assert.assertNotNull(qualifier.get("PersistentIdentifier"));
                JsonNode persistentNode = qualifier.get("PersistentIdentifier");
                assertEquals("NAAN-TEST", persistentNode.get(0).get("PersistentIdentifierReference").asText());

                assertTrue(
                    "ark:/NAAN-TEST/BinaryMaster_1".equals(
                            persistentNode.get(0).get("PersistentIdentifierContent").asText()
                        ) ||
                    "ark:/NAAN-TEST/BinaryMaster_3".equals(
                            persistentNode.get(0).get("PersistentIdentifierContent").asText()
                        )
                );
            });
        JsonNode binaryMaster2Node = generatedResults
            .stream()
            .filter(
                element ->
                    element.has("DataObjectVersion") &&
                    "BinaryMaster_2".equals(element.get("DataObjectVersion").asText())
            )
            .findAny()
            .get();
        assertFalse(binaryMaster2Node.has("PersistentIdentifierContent"));
    }

    @Test
    @RunWithCustomExecutor
    public void should_generate_all_when_management_contract_allow_just_all() throws InvalidParseOperationException {
        ManagementContractModel managementContractModel = new ManagementContractModel();
        managementContractModel.setIdentifier("test mgt contract");
        managementContractModel.setId("test mgt contract");
        PersistentIdentifierPolicy initialPolicy = new PersistentIdentifierPolicy();
        initialPolicy.setPersistentIdentifierAuthority("NAAN-TEST");
        initialPolicy.setPersistentIdentifierPolicyType(PersistentIdentifierPolicyTypeEnum.ARK);
        PersistentIdentifierUsage binaryMasterPolicy = new PersistentIdentifierUsage();
        binaryMasterPolicy.setUsageName(DataObjectVersionType.BINARY_MASTER);
        binaryMasterPolicy.setIntermediaryVersion(VersionUsageModel.IntermediaryVersionEnum.ALL);
        initialPolicy.setPersistentIdentifierUsages(List.of(binaryMasterPolicy));
        managementContractModel.setPersistentIdentifierPolicyList(List.of(initialPolicy));

        Map<String, List<JsonNode>> objectsByQualifierMap = convertListJsonNodeMapByUsage(multiUsagesObjects);

        persistentIdentifierGenerationService.handlePersistentIdentifierForGot(
            objectsByQualifierMap,
            managementContractModel,
            PersistentIdentifierPolicyTypeEnum.ARK
        );
        List<JsonNode> initialVersionsResults = filterObjectsNodesByQualifierPrefix(multiUsagesObjects, "BinaryMaster");
        assertNotNull(initialVersionsResults);
        initialVersionsResults
            .stream()
            .forEach(qualifier -> {
                Assert.assertNotNull(qualifier.get("PersistentIdentifier"));
                JsonNode persistentNode = qualifier.get("PersistentIdentifier");
                assertEquals("NAAN-TEST", persistentNode.get(0).get("PersistentIdentifierReference").asText());

                assertTrue(
                    "ark:/NAAN-TEST/BinaryMaster_1".equals(
                            persistentNode.get(0).get("PersistentIdentifierContent").asText()
                        ) ||
                    "ark:/NAAN-TEST/BinaryMaster_2".equals(
                            persistentNode.get(0).get("PersistentIdentifierContent").asText()
                        ) ||
                    "ark:/NAAN-TEST/BinaryMaster_3".equals(
                            persistentNode.get(0).get("PersistentIdentifierContent").asText()
                        )
                );
            });
    }

    @Test
    @RunWithCustomExecutor
    public void should_generate_all_when_management_contract_allow_1_and_all() throws InvalidParseOperationException {
        ManagementContractModel managementContractModel = new ManagementContractModel();
        managementContractModel.setIdentifier("test mgt contract");
        managementContractModel.setId("test mgt contract");
        PersistentIdentifierPolicy initialPolicy = new PersistentIdentifierPolicy();
        initialPolicy.setPersistentIdentifierAuthority("NAAN-TEST");

        initialPolicy.setPersistentIdentifierPolicyType(PersistentIdentifierPolicyTypeEnum.ARK);
        PersistentIdentifierUsage binaryMasterPolicy = new PersistentIdentifierUsage();
        binaryMasterPolicy.setInitialVersion(true);
        binaryMasterPolicy.setUsageName(DataObjectVersionType.BINARY_MASTER);
        binaryMasterPolicy.setIntermediaryVersion(VersionUsageModel.IntermediaryVersionEnum.ALL);
        initialPolicy.setPersistentIdentifierUsages(List.of(binaryMasterPolicy));
        managementContractModel.setPersistentIdentifierPolicyList(List.of(initialPolicy));

        Map<String, List<JsonNode>> objectsByQualifierMap = convertListJsonNodeMapByUsage(multiUsagesObjects);

        persistentIdentifierGenerationService.handlePersistentIdentifierForGot(
            objectsByQualifierMap,
            managementContractModel,
            PersistentIdentifierPolicyTypeEnum.ARK
        );
        List<JsonNode> initialVersionsResults = filterObjectsNodesByQualifierPrefix(multiUsagesObjects, "BinaryMaster");
        assertNotNull(initialVersionsResults);
        initialVersionsResults
            .stream()
            .forEach(qualifier -> {
                Assert.assertNotNull(qualifier.get("PersistentIdentifier"));
                JsonNode persistentNode = qualifier.get("PersistentIdentifier");
                assertEquals("NAAN-TEST", persistentNode.get(0).get("PersistentIdentifierReference").asText());

                assertTrue(
                    "ark:/NAAN-TEST/BinaryMaster_1".equals(
                            persistentNode.get(0).get("PersistentIdentifierContent").asText()
                        ) ||
                    "ark:/NAAN-TEST/BinaryMaster_2".equals(
                            persistentNode.get(0).get("PersistentIdentifierContent").asText()
                        ) ||
                    "ark:/NAAN-TEST/BinaryMaster_3".equals(
                            persistentNode.get(0).get("PersistentIdentifierContent").asText()
                        )
                );
            });
    }

    @Test
    @RunWithCustomExecutor
    public void should_generate_multi_settings_when_management_contract_allow_multi_usages_policy()
        throws InvalidParseOperationException {
        ManagementContractModel managementContractModel = new ManagementContractModel();
        managementContractModel.setIdentifier("test mgt contract");
        managementContractModel.setId("test mgt contract");
        PersistentIdentifierPolicy initialPolicy = new PersistentIdentifierPolicy();
        initialPolicy.setPersistentIdentifierAuthority("NAAN-TEST");
        initialPolicy.setPersistentIdentifierPolicyType(PersistentIdentifierPolicyTypeEnum.ARK);

        PersistentIdentifierUsage binaryMasterPolicy = new PersistentIdentifierUsage();
        binaryMasterPolicy.setInitialVersion(true);
        binaryMasterPolicy.setUsageName(DataObjectVersionType.BINARY_MASTER);
        binaryMasterPolicy.setIntermediaryVersion(VersionUsageModel.IntermediaryVersionEnum.ALL);

        PersistentIdentifierUsage thumbnailPolicy = new PersistentIdentifierUsage();
        thumbnailPolicy.setInitialVersion(false);
        thumbnailPolicy.setUsageName(DataObjectVersionType.DISSEMINATION);
        thumbnailPolicy.setIntermediaryVersion(VersionUsageModel.IntermediaryVersionEnum.LAST);

        initialPolicy.setPersistentIdentifierUsages(List.of(binaryMasterPolicy, thumbnailPolicy));
        managementContractModel.setPersistentIdentifierPolicyList(List.of(initialPolicy));

        Map<String, List<JsonNode>> objectsByQualifierMap = convertListJsonNodeMapByUsage(multiUsagesObjects);

        persistentIdentifierGenerationService.handlePersistentIdentifierForGot(
            objectsByQualifierMap,
            managementContractModel,
            PersistentIdentifierPolicyTypeEnum.ARK
        );
        List<JsonNode> initialVersionsResults = filterObjectsNodesByQualifierPrefix(multiUsagesObjects, "BinaryMaster");
        initialVersionsResults.addAll(filterObjectsNodesByQualifierPrefix(multiUsagesObjects, "Dissemination_2"));
        assertNotNull(initialVersionsResults);
        initialVersionsResults
            .stream()
            .forEach(qualifier -> {
                Assert.assertNotNull(qualifier.get("PersistentIdentifier"));
                JsonNode persistentNode = qualifier.get("PersistentIdentifier");
                assertEquals("NAAN-TEST", persistentNode.get(0).get("PersistentIdentifierReference").asText());

                assertTrue(
                    "ark:/NAAN-TEST/BinaryMaster_1".equals(
                            persistentNode.get(0).get("PersistentIdentifierContent").asText()
                        ) ||
                    "ark:/NAAN-TEST/BinaryMaster_2".equals(
                            persistentNode.get(0).get("PersistentIdentifierContent").asText()
                        ) ||
                    "ark:/NAAN-TEST/BinaryMaster_3".equals(
                            persistentNode.get(0).get("PersistentIdentifierContent").asText()
                        ) ||
                    "ark:/NAAN-TEST/Dissemination_2".equals(
                            persistentNode.get(0).get("PersistentIdentifierContent").asText()
                        )
                );
            });
    }

    private Map<String, List<JsonNode>> convertListJsonNodeMapByUsage(List<JsonNode> multiUsagesObjects) {
        return multiUsagesObjects
            .stream()
            .filter(jsonNode -> jsonNode.has("DataObjectVersion"))
            .collect(
                Collectors.toMap(
                    jsonNode -> jsonNode.get("DataObjectVersion").asText(),
                    jsonNode -> Collections.singletonList(jsonNode)
                )
            );
    }
}

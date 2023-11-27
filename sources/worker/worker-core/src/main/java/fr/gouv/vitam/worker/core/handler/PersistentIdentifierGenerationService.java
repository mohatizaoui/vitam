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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitam.common.SedaConstants;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.administration.ActivationStatus;
import fr.gouv.vitam.common.model.administration.ManagementContractModel;
import fr.gouv.vitam.common.model.administration.PersistentIdentifierPolicy;
import fr.gouv.vitam.common.model.administration.PersistentIdentifierPolicyTypeEnum;
import fr.gouv.vitam.common.model.administration.PersistentIdentifierUsage;
import fr.gouv.vitam.common.model.administration.VersionUsageModel;
import fr.gouv.vitam.common.model.unit.ArchiveUnitRoot;
import fr.gouv.vitam.common.model.unit.PersistentIdentifierModel;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class PersistentIdentifierGenerationService {

    public static final String PERSISTENT_IDENTIFIER_FIELD = "PersistentIdentifier";

    private static final PersistentIdentifierGenerationService PERSISTENT_IDENTIFIER_GENERATION_SERVICE =
        new PersistentIdentifierGenerationService();

    public static PersistentIdentifierGenerationService getInstance() {
        return PERSISTENT_IDENTIFIER_GENERATION_SERVICE;
    }

    /**
     * Service to fill generated persistent identifiers and management contract id on objects
     *
     * @param objectsByQualifierMap map containing the qualifier -> list of objects
     * @param managementContractModel the management contract
     * @throws InvalidParseOperationException
     */
    public void handlePersistentIdentifierForGot(Map<String, List<JsonNode>> objectsByQualifierMap,
        ManagementContractModel managementContractModel, PersistentIdentifierPolicyTypeEnum persistentIdentifierType)
        throws InvalidParseOperationException {

        if (Objects.isNull(managementContractModel) ||
            ActivationStatus.INACTIVE.equals(managementContractModel.getStatus()) ||
            Objects.isNull(managementContractModel.getPersistentIdentifierPolicyList())) {
            return;
        }

        Optional<PersistentIdentifierPolicy> persistentIdentifierPolicyOpt =
            retrievePolicyByPersistentType(managementContractModel, persistentIdentifierType);
        if (persistentIdentifierPolicyOpt.isEmpty()) {
            return;
        }

        PersistentIdentifierPolicy persistentIdPolicy = persistentIdentifierPolicyOpt.get();
        if (PersistentIdentifierPolicyTypeEnum.ARK.equals(persistentIdPolicy.getPersistentIdentifierPolicyType())) {
            manageArkIdentifiersGeneration(objectsByQualifierMap, persistentIdPolicy);
        }
    }

    private void manageArkIdentifiersGeneration(Map<String, List<JsonNode>> objectsByQualifierMap,
        PersistentIdentifierPolicy persistentIdPolicy) throws InvalidParseOperationException {
        for (PersistentIdentifierUsage usageNode : persistentIdPolicy.getPersistentIdentifierUsages()) {
            final List<String> qualifierList = getSortedQualifierList(objectsByQualifierMap, usageNode);
            if (qualifierList.isEmpty()) {
                continue;
            }

            for (String qualifier : qualifierList) {
                List<JsonNode> qualifiersToUpdate = objectsByQualifierMap.get(qualifier);
                int version = Integer.parseInt(StringUtils.substringAfterLast(qualifier, "_"));

                boolean isFirstVersion = (version == 1);
                boolean concernLastVersionPolicy =
                    VersionUsageModel.IntermediaryVersionEnum.LAST.equals(usageNode.getIntermediaryVersion());
                boolean concernAllVersionsPolicy =
                    VersionUsageModel.IntermediaryVersionEnum.ALL.equals(usageNode.getIntermediaryVersion());
                boolean hasSingleQualifier = (qualifierList.size() == 1);

                if ((isFirstVersion && (usageNode.isInitialVersion() || concernAllVersionsPolicy ||
                    (concernLastVersionPolicy && hasSingleQualifier))) ||
                    (!isFirstVersion && (concernAllVersionsPolicy ||
                        (concernLastVersionPolicy && isLatestVersion(qualifierList, version))))) {
                    fillArkPersistentIdentifier(persistentIdPolicy, qualifiersToUpdate);
                }
            }
        }
    }

    private static List<String> getSortedQualifierList(Map<String, List<JsonNode>> objectsByQualifierMap,
        PersistentIdentifierUsage usageNode) {
        return objectsByQualifierMap.keySet().stream()
            .filter(key -> key.startsWith(usageNode.getUsageName().getName())).sorted(
                Comparator.comparingInt(key -> Integer.parseInt(StringUtils.substringAfterLast(key, "_"))))
            .collect(toList());

    }

    private void fillArkPersistentIdentifier(PersistentIdentifierPolicy persistentIdPolicy,
        List<JsonNode> qualifiersToUpdate)
        throws InvalidParseOperationException {
        for (JsonNode qualifierToUpdate : qualifiersToUpdate) {
            fillArkPersistentIdentifier(persistentIdPolicy, qualifierToUpdate);
        }
    }

    private boolean isLatestVersion(List<String> qualifierList, Integer version) {
        Integer lastVersion =
            qualifierList.stream()
                .map(qualifier -> Integer.parseInt(StringUtils.substringAfterLast(qualifier, "_")))
                .max(Integer::compare).orElseThrow();
        return version.equals(lastVersion);
    }

    /**
     * Fill generated persistent identifier on objects according to management contract settings
     *
     * @param policy
     * @param qualifierToUpdate
     * @throws InvalidParseOperationException
     */
    public void fillArkPersistentIdentifier(PersistentIdentifierPolicy policy, JsonNode qualifierToUpdate)
        throws InvalidParseOperationException {

        ObjectNode updatingQualifier = (ObjectNode) qualifierToUpdate;
        PersistentIdentifierModel vitamPersistentIdentifierModel = new PersistentIdentifierModel();
        vitamPersistentIdentifierModel.setPersistentIdentifierType(
            policy.getPersistentIdentifierPolicyType().name().toLowerCase());

        final String guid = updatingQualifier.get(SedaConstants.PREFIX_ID).asText();

        if (!qualifierToUpdate.has(PERSISTENT_IDENTIFIER_FIELD)) {
            updatingQualifier.set(PERSISTENT_IDENTIFIER_FIELD, JsonHandler.createArrayNode());
        }

        vitamPersistentIdentifierModel.setPersistentIdentifierReference(policy.getPersistentIdentifierAuthority());
        vitamPersistentIdentifierModel.setPersistentIdentifierContent(
            PersistentIdentifierPolicyTypeEnum.ARK.name().toLowerCase() + ":/" +
                policy.getPersistentIdentifierAuthority() + "/" + guid);

        ArrayNode persistentIdentifierNode = (ArrayNode) updatingQualifier.get(PERSISTENT_IDENTIFIER_FIELD);
        persistentIdentifierNode.add(JsonHandler.toJsonNode(vitamPersistentIdentifierModel));
    }

    public void handlePersistentIdentifierForUnit(final ArchiveUnitRoot archiveUnitRoot, final String unitGUID,
        ManagementContractModel managementContractModel, PersistentIdentifierPolicyTypeEnum persistentIdentifierType) {

        if (Objects.isNull(managementContractModel) ||
            ActivationStatus.INACTIVE.equals(managementContractModel.getStatus()) ||
            Objects.isNull(managementContractModel.getPersistentIdentifierPolicyList())) {
            return;
        }

        Optional<PersistentIdentifierPolicy> persistentIdentifierPolicy =
            retrievePolicyByPersistentTypeForUnit(managementContractModel, persistentIdentifierType);
        persistentIdentifierPolicy.ifPresent(persistentIdPolicy -> {
            String persistentIdAuthority = persistentIdPolicy.getPersistentIdentifierAuthority();
            PersistentIdentifierPolicyTypeEnum persistentIdPolicyType =
                persistentIdPolicy.getPersistentIdentifierPolicyType();

            PersistentIdentifierModel vitamPersistentIdentifierModel = new PersistentIdentifierModel();

            vitamPersistentIdentifierModel.setPersistentIdentifierReference(persistentIdAuthority);
            vitamPersistentIdentifierModel.setPersistentIdentifierType(
                persistentIdPolicyType.name().toLowerCase());
            vitamPersistentIdentifierModel.setPersistentIdentifierContent(
                persistentIdPolicyType.name().toLowerCase() + ":/" + persistentIdAuthority + "/" + unitGUID);
            if (Objects.isNull(archiveUnitRoot.getArchiveUnit().getDescriptiveMetadataModel()
                .getPersistentIdentifier())) {
                archiveUnitRoot.getArchiveUnit().getDescriptiveMetadataModel()
                    .setPersistentIdentifier(new ArrayList<>());
            }
            archiveUnitRoot.getArchiveUnit().getDescriptiveMetadataModel()
                .getPersistentIdentifier().add(vitamPersistentIdentifierModel);
        });
    }



    private Optional<PersistentIdentifierPolicy> retrievePolicyByPersistentTypeForUnit(
        ManagementContractModel managementContractModel,
        PersistentIdentifierPolicyTypeEnum persistentIdentifierPolicyType) {
        return managementContractModel.getPersistentIdentifierPolicyList().stream()
            .filter(policy -> policy.isPersistentIdentifierUnit() &&
                persistentIdentifierPolicyType.equals(policy.getPersistentIdentifierPolicyType()))
            .findFirst();
    }

    private Optional<PersistentIdentifierPolicy> retrievePolicyByPersistentType(
        ManagementContractModel managementContractModel,
        PersistentIdentifierPolicyTypeEnum persistentIdentifierPolicyType) {
        return managementContractModel.getPersistentIdentifierPolicyList().stream()
            .filter(policy -> persistentIdentifierPolicyType.equals(policy.getPersistentIdentifierPolicyType()))
            .findFirst();
    }
}

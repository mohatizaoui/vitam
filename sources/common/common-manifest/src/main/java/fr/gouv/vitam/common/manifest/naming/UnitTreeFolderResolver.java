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

package fr.gouv.vitam.common.manifest.naming;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitam.common.database.builder.query.VitamFieldsHelper;
import fr.gouv.vitam.common.exception.ExportException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class UnitTreeFolderResolver implements FolderResolver {

    private static final String PATH_SEPARATOR = "/";
    private static final String DEFAULT_TITLE_LANGUAGE = "fr";
    private static final int MAX_DIRECTORY_NAME_SIZE_LIMIT = 100;

    private final Map<String, JsonNode> unitsByIds;
    private final Map<String, String> unitIdsByObjectGroupId = new HashMap<>();
    private final Map<String, String> resolvedFolderByUnitId = new HashMap<>();
    private final Set<String> resolvedBasePaths = new HashSet<>();

    public UnitTreeFolderResolver(List<JsonNode> units) throws ExportException {
        checkDuplicatePathsForUnits(units);
        checkDuplicatePathsForObjectGroups(units);

        this.unitsByIds = units.stream().collect(Collectors.toMap(UnitTreeFolderResolver::getId, unit -> unit));

        for (JsonNode unit : units) {
            String unitId = getId(unit);
            Optional<String> objectGroupId = getObjectGroupId(unit);
            objectGroupId.ifPresent(ogId -> this.unitIdsByObjectGroupId.put(ogId, unitId));
        }
    }

    private static void checkDuplicatePathsForUnits(List<JsonNode> units) throws ExportException {
        Set<String> unitIds = units.stream().map(UnitTreeFolderResolver::getId).collect(Collectors.toSet());
        for (JsonNode unit : units) {
            String unitId = getId(unit);
            List<String> declaredParentUnitIds = getUnitParents(unit);
            List<String> exportedParentUnitIds = declaredParentUnitIds.stream().filter(unitIds::contains).toList();
            if (exportedParentUnitIds.size() > 1) {
                throw new ExportException("Multiple paths for unit with ID " + unitId);
            }
        }
    }

    private static void checkDuplicatePathsForObjectGroups(List<JsonNode> units) throws ExportException {
        // Ensure no multiple units reference the same object group id
        Set<String> objectGroupIds = new HashSet<>();
        for (JsonNode unit : units) {
            Optional<String> objectGroupId = getObjectGroupId(unit);
            if (objectGroupId.isPresent() && !objectGroupIds.add(objectGroupId.get())) {
                throw new ExportException("Multiple paths for object group with ID " + objectGroupId.get());
            }
        }
    }

    @Override
    public String resolve(String objectGroupId) {
        if (!this.unitIdsByObjectGroupId.containsKey(objectGroupId)) {
            throw new IllegalStateException("Unknown unit for object group with ID " + objectGroupId);
        }

        String unitId = this.unitIdsByObjectGroupId.get(objectGroupId);

        return resolveUnitPath(unitId);
    }

    private String resolveUnitPath(String unitId) {
        if (resolvedFolderByUnitId.containsKey(unitId)) {
            return resolvedFolderByUnitId.get(unitId);
        }

        JsonNode unit = unitsByIds.get(unitId);
        if (unit == null) {
            throw new IllegalStateException("Unknown unit with ID " + unitId);
        }

        List<String> declaredParentUnitIds = getUnitParents(unit);
        List<String> exportParentUnitIds = declaredParentUnitIds.stream().filter(unitsByIds::containsKey).toList();

        if (exportParentUnitIds.size() > 2) {
            throw new IllegalStateException("Multiple paths not expected for unit with ID " + unitId);
        }

        String baseParentPath;
        if (exportParentUnitIds.isEmpty()) {
            baseParentPath = "";
        } else {
            String parentUnitId = exportParentUnitIds.get(0);
            baseParentPath = resolveUnitPath(parentUnitId) + PATH_SEPARATOR;
        }

        String title = getTitle(unit);
        String normalizedTitle = FileNameCleaner.cleanFileName(title);

        if (
            this.resolvedBasePaths.contains(baseParentPath + normalizedTitle) ||
            normalizedTitle.length() > MAX_DIRECTORY_NAME_SIZE_LIMIT
        ) {
            // If duplicate path OR path is too long ==> truncate unit title & suffix with guid
            normalizedTitle = truncateAndAddSuffix(normalizedTitle, unitId);

            if (this.resolvedBasePaths.contains(baseParentPath + normalizedTitle)) {
                throw new IllegalStateException(
                    "Deduplicated title still exists for unit with ID " + unitId + ". Title: '" + normalizedTitle + "'"
                );
            }
        }

        this.resolvedBasePaths.add(baseParentPath + normalizedTitle);
        this.resolvedFolderByUnitId.put(unitId, baseParentPath + normalizedTitle);
        return baseParentPath + normalizedTitle;
    }

    private static String truncateAndAddSuffix(String sanitizedTitle, String unitId) {
        int maxLength = Math.min(MAX_DIRECTORY_NAME_SIZE_LIMIT, sanitizedTitle.length() + 1 + unitId.length());
        return sanitizedTitle.substring(0, maxLength - unitId.length() - 1) + "_" + unitId;
    }

    private static String getId(JsonNode unit) {
        return unit.get(VitamFieldsHelper.id()).asText();
    }

    private static String getTitle(JsonNode unit) {
        if (unit.has("Title")) {
            return unit.get("Title").asText();
        }
        ObjectNode title_ = (ObjectNode) unit.get("Title_");
        if (title_ == null || title_.isEmpty()) {
            throw new IllegalStateException("Unit with ID " + getId(unit) + " does not have a Title or Title_ field");
        }
        // If "Title" is missing, and multiple "Title_.*" fields are found, use "fr" as default
        if (title_.has(DEFAULT_TITLE_LANGUAGE)) {
            return title_.get(DEFAULT_TITLE_LANGUAGE).asText();
        }
        return title_.elements().next().asText();
    }

    private static Optional<String> getObjectGroupId(JsonNode unit) {
        if (!unit.has(VitamFieldsHelper.object())) {
            return Optional.empty();
        }
        return Optional.of(unit.get(VitamFieldsHelper.object()).asText());
    }

    private static List<String> getUnitParents(JsonNode unit) {
        List<String> parentUnitIds = new ArrayList<>();
        for (JsonNode parent : unit.get(VitamFieldsHelper.unitups())) {
            parentUnitIds.add(parent.asText());
        }
        return parentUnitIds;
    }
}

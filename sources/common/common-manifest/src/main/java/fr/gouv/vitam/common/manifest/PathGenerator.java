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
package fr.gouv.vitam.common.manifest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ListMultimap;
import fr.gouv.vitam.common.database.builder.query.VitamFieldsHelper;
import fr.gouv.vitam.common.exception.ExportException;

import java.io.File;
import java.util.List;
import java.util.Set;

public class PathGenerator {

    public static final int FULL_DIRECTORY_NAME_SIZE_LIMIT = 183;
    public static final String UNIT_TITLE = "Title";

    public static String generatePath(
        List<JsonNode> units,
        ListMultimap<String, String> multimap,
        List<String> linkedUnits,
        Set<String> existingDirectoryNames
    ) throws ExportException {
        if (linkedUnits.size() != 1) {
            throw new ExportException("The unit must have exactly one parent");
        }
        checkForMultipleParents(units);

        String childId = linkedUnits.get(0);
        String rootId = findRoot(multimap, childId);
        return buildPath(units, multimap, rootId, childId, existingDirectoryNames);
    }

    private static void checkForMultipleParents(List<JsonNode> units) throws ExportException {
        for (JsonNode unit : units) {
            if (unit.has(VitamFieldsHelper.unitups())) {
                ArrayNode unitupsNode = (ArrayNode) unit.get(VitamFieldsHelper.unitups());

                if (unitupsNode.size() > 1) {
                    throw new ExportException(
                        "The unit with ID " + unit.get(VitamFieldsHelper.id()).asText() + " has multiple parents!"
                    );
                }
            }
        }
    }

    private static String buildPath(
        List<JsonNode> units,
        ListMultimap<String, String> multimap,
        String currentId,
        String targetId,
        Set<String> existingDirectoryNames
    ) throws ExportException {
        JsonNode unit = findUnitById(units, currentId);
        if (unit == null) {
            throw new ExportException("Unit not found for ID: " + currentId);
        }

        String title = unit.get(UNIT_TITLE).asText();
        String directoryName = buildDirectoryName(title, unit, existingDirectoryNames);

        if (currentId.equals(targetId)) {
            return directoryName;
        }

        List<String> children = multimap.get(currentId);
        for (String childId : children) {
            String path = buildPath(units, multimap, childId, targetId, existingDirectoryNames);
            if (path != null) {
                return directoryName + File.separator + path;
            }
        }

        return null;
    }

    private static String findRoot(ListMultimap<String, String> multimap, String childId) {
        for (String key : multimap.keySet()) {
            List<String> children = multimap.get(key);
            if (children.contains(childId)) {
                return findRoot(multimap, key);
            }
        }
        return childId;
    }

    private static JsonNode findUnitById(List<JsonNode> units, String unitId) {
        for (JsonNode unit : units) {
            if (unit.get(VitamFieldsHelper.id()).asText().equals(unitId)) {
                return unit;
            }
        }
        return null;
    }

    private static String buildDirectoryName(String title, JsonNode unit, Set<String> existingDirectoryNames) {
        String directoryName = FileNameCleaner.cleanFileName(title);

        if (existingDirectoryNames.contains(directoryName)) {
            String idPrefix = unit.get(VitamFieldsHelper.id()).textValue() != null
                ? unit.get(VitamFieldsHelper.id()).textValue()
                : "";
            directoryName = directoryName + idPrefix;
        }

        if (directoryName.length() > FULL_DIRECTORY_NAME_SIZE_LIMIT) {
            directoryName = directoryName.substring(0, FULL_DIRECTORY_NAME_SIZE_LIMIT);
        }

        return directoryName;
    }
}

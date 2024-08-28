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

package fr.gouv.vitam.functional.administration.core.archiveunitprofilesSchema;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.model.administration.CombinedSchemaModel;
import fr.gouv.vitam.common.model.administration.schema.SchemaCardinality;
import fr.gouv.vitam.common.model.administration.schema.SchemaResponse;

public class EffectiveCardinalityProcessor implements CombinedSchemaProcessor {

    private SchemaResponse schemaResponse;

    private JsonNode controlSchemaNode;

    public EffectiveCardinalityProcessor() {}

    public EffectiveCardinalityProcessor(SchemaResponse schemaResponse, JsonNode controlSchemaNode) {
        this.schemaResponse = schemaResponse;
        this.controlSchemaNode = controlSchemaNode;
    }

    @Override
    public void process(CombinedSchemaModel model) {
        SchemaCardinality cardinality = this.schemaResponse.getCardinality();
        SchemaCardinality effectiveCardinality;

        boolean isForbidden = isFieldForbiddenDueToAdditionalProperties(
            controlSchemaNode,
            this.schemaResponse.getPath()
        );

        boolean isRequired = isFieldRequired(controlSchemaNode, this.schemaResponse.getPath());

        if (isForbidden) {
            effectiveCardinality = SchemaCardinality.ZERO;
        } else {
            effectiveCardinality = determineEffectiveCardinality(
                cardinality,
                this.schemaResponse,
                isRequired,
                this.controlSchemaNode
            );
        }

        model.setEffectiveCardinality(effectiveCardinality.toString());
    }

    private boolean isFieldRequired(JsonNode controlSchemaNode, String fieldPath) {
        // Split the path into segments
        String[] pathSegments = fieldPath.split("\\.");

        // Traverse to the parent node
        JsonNode currentNode = controlSchemaNode;
        for (int i = 0; i < pathSegments.length - 1; i++) {
            currentNode = currentNode.path("properties").path(pathSegments[i]);
            if (currentNode.isMissingNode()) {
                return false; // If we can't find the path, return false
            }

            // Check if the current node is an array with items
            if (currentNode.path("type").asText().equals("array")) {
                currentNode = currentNode.path("items");
            }
        }

        // Get the field name (last segment of the path)
        String fieldName = pathSegments[pathSegments.length - 1];

        // Check if the parent node has a "required" list and if it contains the field name
        JsonNode requiredNode = currentNode.path("required");
        if (requiredNode.isArray()) {
            for (JsonNode requiredField : requiredNode) {
                if (requiredField.asText().equals(fieldName)) {
                    return true;
                }
            }
        }

        return false;
    }

    private SchemaCardinality determineEffectiveCardinality(
        SchemaCardinality cardinality,
        SchemaResponse schema,
        Boolean isRequired,
        JsonNode controlSchemaNode
    ) {
        boolean isArray = controlSchemaNode
            .path("properties")
            .path(schema.getFieldName())
            .path("type")
            .asText()
            .equals("array");

        JsonNode fieldNode = controlSchemaNode.path("properties").path(schema.getFieldName());

        if (isArray) {
            int minItems = fieldNode.path("minItems").asInt(0);
            int maxItems = fieldNode.path("maxItems").asInt(Integer.MAX_VALUE);

            if (minItems == 0 && maxItems == 1) {
                return SchemaCardinality.ONE;
            } else if (minItems == 1 && maxItems == 1) {
                return SchemaCardinality.ONE_REQUIRED;
            }
        }

        switch (cardinality) {
            case MANY_REQUIRED:
                return isArray ? SchemaCardinality.MANY_REQUIRED : SchemaCardinality.ONE_REQUIRED;
            case MANY:
                return isArray
                    ? (isRequired ? SchemaCardinality.MANY_REQUIRED : SchemaCardinality.MANY)
                    : (isRequired ? SchemaCardinality.ONE_REQUIRED : SchemaCardinality.ONE);
            case ONE_REQUIRED:
                return SchemaCardinality.ONE_REQUIRED;
            case ONE:
                return isRequired ? SchemaCardinality.ONE_REQUIRED : SchemaCardinality.ONE;
            default:
                return cardinality;
        }
    }

    private boolean isFieldForbiddenDueToAdditionalProperties(JsonNode controlSchemaNode, String fieldPath) {
        String[] pathSegments = fieldPath.split("\\.");
        JsonNode currentNode = controlSchemaNode;

        for (String segment : pathSegments) {
            if (currentNode.has("additionalProperties") && !currentNode.get("additionalProperties").asBoolean()) {
                if (!currentNode.path("properties").has(segment)) {
                    return true;
                }
            }
            currentNode = currentNode.path("properties").path(segment);
            if (currentNode.isMissingNode()) {
                break;
            }
        }

        return false;
    }
}

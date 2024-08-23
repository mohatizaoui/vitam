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
import fr.gouv.vitam.common.model.administration.SchemaControl;
import fr.gouv.vitam.common.model.administration.schema.SchemaResponse;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ControlProcessor implements CombinedSchemaProcessor {

    private JsonNode controlSchemaNode;

    private SchemaResponse schemaResponse;

    public ControlProcessor() {}

    public ControlProcessor(JsonNode controlSchemaNode, SchemaResponse schemaResponse) {
        this.controlSchemaNode = controlSchemaNode;
        this.schemaResponse = schemaResponse;
    }

    @Override
    public void process(CombinedSchemaModel model) {
        Map<String, SchemaControl> controls = this.parseControlSchema(this.controlSchemaNode);

        SchemaControl control = controls.get(this.schemaResponse.getFieldName());
        if (control != null) {
            model.setControl(control);
        }
    }

    public Map<String, SchemaControl> parseControlSchema(JsonNode controlSchemaNode) {
        Map<String, SchemaControl> controls = new HashMap<>();

        JsonNode propertiesNode = controlSchemaNode.path("properties");

        Iterator<Map.Entry<String, JsonNode>> fields = propertiesNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String fieldName = field.getKey();
            JsonNode fieldNode = field.getValue();

            SchemaControl control = new SchemaControl();
            processNode(fieldNode, control);

            controls.put(fieldName, control);
        }

        return controls;
    }

    private static void processNode(JsonNode node, SchemaControl control) {
        JsonNode typeNode = node.path("type");
        if (typeNode.isTextual()) {
            String type = typeNode.asText().toUpperCase();
            // control.setType(type);

            if ("ARRAY".equals(type)) {
                JsonNode itemsNode = node.path("items");
                processNode(itemsNode, control); // Process items node recursively
            }
        }

        JsonNode formatNode = node.path("format");
        if (formatNode.isTextual()) {
            String format = formatNode.asText();
            control.setType(format.toUpperCase().replace("-", ""));
        }

        JsonNode enumNode = node.path("enum");
        if (enumNode.isArray()) {
            for (JsonNode enumValue : enumNode) {
                control.addValue(enumValue.asText());
            }
            control.setType("SELECT");
        }

        JsonNode patternNode = node.path("pattern");
        if (patternNode.isTextual()) {
            control.setType("REGEX");
            control.setValue(patternNode.asText());
        }

        JsonNode descriptionNode = node.path("description");
        if (descriptionNode.isTextual()) {
            control.setComment(descriptionNode.asText());
        }
    }
}

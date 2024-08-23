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

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.administration.SchemaControl;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class JsonSchemaParserTest {

    @Test
    public void testParseControlSchemaWithRegex() throws InvalidParseOperationException {
        String controlSchema =
            "{ \"$schema\": \"http://vitam-json-schema.org/draft-04/schema#\", \"id\": \"http://example.com/root.json\", \"type\": \"object\", \"properties\": { \"Title\": { \"type\": \"string\", \"pattern\": \"^FACT_[0-9]{10}$\"} } }";

        ControlProcessor controlProcessor = new ControlProcessor();

        Map<String, SchemaControl> controls = controlProcessor.parseControlSchema(
            JsonHandler.getFromString(controlSchema)
        );

        Assert.assertNotNull(controls);
        Assert.assertTrue(controls.containsKey("Title"));

        SchemaControl titleControl = controls.get("Title");
        Assert.assertEquals("REGEX", titleControl.getType());
        Assert.assertEquals("^FACT_[0-9]{10}$", titleControl.getValue());
    }

    @Test
    public void testParseControlSchemaWithoutEnum() throws InvalidParseOperationException {
        String controlSchema =
            "{ \"$schema\": \"http://vitam-json-schema.org/draft-04/schema#\", \"id\": \"http://example.com/root.json\", \"type\": \"object\", \"properties\": { \"Title\": { \"type\": \"string\" } } }";

        ControlProcessor controlProcessor = new ControlProcessor();
        Map<String, SchemaControl> controls = controlProcessor.parseControlSchema(
            JsonHandler.getFromString(controlSchema)
        );

        Assert.assertNotNull(controls);
        Assert.assertTrue(controls.containsKey("Title"));

        SchemaControl titleControl = controls.get("Title");
        Assert.assertNull(titleControl.getType());
        Assert.assertTrue(titleControl.getValues().isEmpty());
        Assert.assertNull(titleControl.getValue());
    }

    @Test
    public void testParseControlSchemaWithSelect() throws InvalidParseOperationException {
        String controlSchema =
            "{ \"$schema\": \"http://vitam-json-schema.org/draft-04/schema#\", \"id\": \"http://example.com/root.json\", \"type\": \"object\", \"properties\": { \"Status\": { \"type\": \"string\", \"enum\": [\"ACTIVE\", \"INACTIVE\"] } } }";

        ControlProcessor controlProcessor = new ControlProcessor();
        Map<String, SchemaControl> controls = controlProcessor.parseControlSchema(
            JsonHandler.getFromString(controlSchema)
        );

        Assert.assertNotNull(controls);
        Assert.assertTrue(controls.containsKey("Status"));

        SchemaControl statusControl = controls.get("Status");
        Assert.assertEquals("SELECT", statusControl.getType());
        Assert.assertTrue(statusControl.getValues().contains("ACTIVE"));
        Assert.assertTrue(statusControl.getValues().contains("INACTIVE"));
    }

    @Test
    public void testParseControlSchemaWithDateTime() throws InvalidParseOperationException {
        String controlSchema =
            "{ \"$schema\": \"http://vitam-json-schema.org/draft-04/schema#\", \"id\": \"http://example.com/root.json\", \"type\": \"object\", \"properties\": { \"EventDateTime\": { \"type\": \"string\", \"format\": \"date-time\" } } }";

        ControlProcessor controlProcessor = new ControlProcessor();
        Map<String, SchemaControl> controls = controlProcessor.parseControlSchema(
            JsonHandler.getFromString(controlSchema)
        );

        Assert.assertNotNull(controls);
        Assert.assertTrue(controls.containsKey("EventDateTime"));

        SchemaControl control = controls.get("EventDateTime");
        Assert.assertEquals("DATETIME", control.getType());
    }

    @Test
    public void testParseControlSchemaWithDate() throws InvalidParseOperationException {
        String controlSchema =
            "{ \"$schema\": \"http://vitam-json-schema.org/draft-04/schema#\", \"id\": \"http://example.com/root.json\", \"type\": \"object\", \"properties\": { \"EventDate\": { \"type\": \"string\", \"format\": \"date\" } } }";

        ControlProcessor controlProcessor = new ControlProcessor();
        Map<String, SchemaControl> controls = controlProcessor.parseControlSchema(
            JsonHandler.getFromString(controlSchema)
        );

        Assert.assertNotNull(controls);
        Assert.assertTrue(controls.containsKey("EventDate"));

        SchemaControl control = controls.get("EventDate");
        Assert.assertEquals("DATE", control.getType());
    }

    @Test
    public void testParseControlSchemaWithDateYearMonth() throws InvalidParseOperationException {
        String controlSchema =
            "{ \"$schema\": \"http://vitam-json-schema.org/draft-04/schema#\", \"id\": \"http://example.com/root.json\", \"type\": \"object\", \"properties\": { \"EventYearMonth\": { \"type\": \"string\", \"format\": \"date-yearmonth\" } } }";

        ControlProcessor controlProcessor = new ControlProcessor();
        Map<String, SchemaControl> controls = controlProcessor.parseControlSchema(
            JsonHandler.getFromString(controlSchema)
        );

        Assert.assertNotNull(controls);
        Assert.assertTrue(controls.containsKey("EventYearMonth"));

        SchemaControl control = controls.get("EventYearMonth");
        Assert.assertEquals("DATEYEARMONTH", control.getType());
    }

    @Test
    public void testParseControlSchemaWithDateYear() throws InvalidParseOperationException {
        String controlSchema =
            "{ \"$schema\": \"http://vitam-json-schema.org/draft-04/schema#\", \"id\": \"http://example.com/root.json\", \"type\": \"object\", \"properties\": { \"EventYear\": { \"type\": \"string\", \"format\": \"date-year\" } } }";

        ControlProcessor controlProcessor = new ControlProcessor();
        Map<String, SchemaControl> controls = controlProcessor.parseControlSchema(
            JsonHandler.getFromString(controlSchema)
        );

        Assert.assertNotNull(controls);
        Assert.assertTrue(controls.containsKey("EventYear"));

        SchemaControl control = controls.get("EventYear");
        Assert.assertEquals("DATEYEAR", control.getType());
    }
}

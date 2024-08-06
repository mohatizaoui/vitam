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
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.ArchiveUnitProfileModel;
import fr.gouv.vitam.common.model.administration.CombinedSchemaModel;
import fr.gouv.vitam.common.model.administration.schema.SchemaCardinality;
import fr.gouv.vitam.common.model.administration.schema.SchemaOrigin;
import fr.gouv.vitam.common.model.administration.schema.SchemaResponse;
import fr.gouv.vitam.common.model.administration.schema.SchemaType;
import fr.gouv.vitam.functional.administration.common.exception.ReferentialException;
import fr.gouv.vitam.functional.administration.core.archiveunitprofiles.ArchiveUnitProfileService;
import fr.gouv.vitam.functional.administration.core.schema.SchemaService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ArchiveUnitProfileSchemaServiceImplTest {

    private ArchiveUnitProfileService archiveUnitProfileService;
    private SchemaService schemaService;
    private ArchiveUnitProfileSchemaServiceImpl archiveUnitProfileSchemaServiceImpl;

    @Before
    public void setUp() {
        archiveUnitProfileService = Mockito.mock(ArchiveUnitProfileService.class);
        schemaService = Mockito.mock(SchemaService.class);
        archiveUnitProfileSchemaServiceImpl = new ArchiveUnitProfileSchemaServiceImpl(
            archiveUnitProfileService,
            schemaService
        );
    }

    private ArchiveUnitProfileModel createProfile(String controlSchema, List<String> fields) {
        ArchiveUnitProfileModel profile = new ArchiveUnitProfileModel();
        profile.setControlSchema(controlSchema);
        profile.setFields(fields);
        return profile;
    }

    private SchemaResponse createSchemaResponse(String fieldName, SchemaType type, SchemaCardinality cardinality) {
        SchemaResponse schema = new SchemaResponse();
        schema.setFieldName(fieldName);
        schema.setSedaField(fieldName);
        schema.setCollection("Unit");
        schema.setDescription(fieldName + " of the unit");
        schema.setType(type);
        schema.setOrigin(SchemaOrigin.INTERNAL);
        schema.setCardinality(cardinality);
        schema.setShortName(fieldName);
        schema.setPath(fieldName);
        schema.setSedaVersions(Arrays.asList("2.1"));
        return schema;
    }

    @Test
    public void testGetCombinedSchemaModelsById()
        throws ReferentialException, InvalidParseOperationException, IOException, InvalidCreateOperationException {
        String identifier = "AUP-000001";

        JsonNode schemaTemplate = JsonSchemaUtil.loadSchemaTemplate();
        String controlSchema = JsonSchemaUtil.modifySchema(schemaTemplate, Arrays.asList("Title", "EventDate"));
        ArchiveUnitProfileModel profile = createProfile(controlSchema, Arrays.asList("Title", "EventDate"));

        SchemaResponse schema1 = createSchemaResponse("Title", SchemaType.TEXT, SchemaCardinality.ONE);
        SchemaResponse schema2 = createSchemaResponse("EventDate", SchemaType.DATE, SchemaCardinality.ONE);

        List<SchemaResponse> schemas = Arrays.asList(schema1, schema2);

        RequestResponseOK<ArchiveUnitProfileModel> profileResponse = new RequestResponseOK<>(
            null,
            Arrays.asList(profile),
            2
        );

        Mockito.when(archiveUnitProfileService.findArchiveUnitProfiles(Mockito.any())).thenReturn(profileResponse);
        Mockito.when(schemaService.findUnitSchema()).thenReturn(schemas);

        List<CombinedSchemaModel> combinedSchemaModels =
            archiveUnitProfileSchemaServiceImpl.getCombinedSchemaModelsById(identifier);

        Assert.assertNotNull(combinedSchemaModels);
        Assert.assertEquals(2, combinedSchemaModels.size());

        CombinedSchemaModel combinedSchema1 = combinedSchemaModels.get(0);
        Assert.assertEquals("Title", combinedSchema1.getFieldName());
        Assert.assertEquals("REGEX", combinedSchema1.getControl().getType());

        CombinedSchemaModel combinedSchema2 = combinedSchemaModels.get(1);
        Assert.assertEquals("EventDate", combinedSchema2.getFieldName());
        Assert.assertEquals("DATE", combinedSchema2.getControl().getType());
    }

    @Test
    public void testEffectiveCardinalityOne()
        throws ReferentialException, InvalidParseOperationException, IOException, InvalidCreateOperationException {
        String identifier = "AUP-000002";

        JsonNode schemaTemplate = JsonSchemaUtil.loadSchemaTemplate();
        String controlSchema = JsonSchemaUtil.modifySchema(schemaTemplate, Arrays.asList("EffectiveDate"));
        ArchiveUnitProfileModel profile = createProfile(controlSchema, Arrays.asList("EffectiveDate"));

        SchemaResponse schema = createSchemaResponse("EffectiveDate", SchemaType.DATE, SchemaCardinality.ONE);

        List<SchemaResponse> schemas = Arrays.asList(schema);

        RequestResponseOK<ArchiveUnitProfileModel> profileResponse = new RequestResponseOK<>(
            null,
            Arrays.asList(profile),
            2
        );

        Mockito.when(archiveUnitProfileService.findArchiveUnitProfiles(Mockito.any())).thenReturn(profileResponse);
        Mockito.when(schemaService.findUnitSchema()).thenReturn(schemas);

        List<CombinedSchemaModel> combinedSchemaModels =
            archiveUnitProfileSchemaServiceImpl.getCombinedSchemaModelsById(identifier);

        Assert.assertNotNull(combinedSchemaModels);
        Assert.assertEquals(1, combinedSchemaModels.size());

        CombinedSchemaModel combinedSchema = combinedSchemaModels.get(0);
        Assert.assertEquals("EffectiveDate", combinedSchema.getFieldName());
        Assert.assertEquals("DATE", combinedSchema.getControl().getType());
        Assert.assertEquals("ONE_REQUIRED", combinedSchema.getEffectiveCardinality());
    }

    @Test
    public void testEffectiveCardinalityZeroDueToAdditionalPropertiesForBirthDate()
        throws ReferentialException, InvalidParseOperationException, IOException, InvalidCreateOperationException {
        String identifier = "AUP-000003";

        JsonNode schemaTemplate = JsonSchemaUtil.loadSchemaTemplate();
        String controlSchema = JsonSchemaUtil.modifySchema(schemaTemplate, Arrays.asList("Addressee"));
        ArchiveUnitProfileModel profile = createProfile(controlSchema, Arrays.asList("Addressee"));

        SchemaResponse schemaAddressee = createSchemaResponse("Addressee", SchemaType.OBJECT, SchemaCardinality.ONE);
        SchemaResponse schemaBirthDate = createSchemaResponse(
            "Addressee.BirthDate",
            SchemaType.DATE,
            SchemaCardinality.ONE
        );

        List<SchemaResponse> schemas = Arrays.asList(schemaAddressee, schemaBirthDate);
        RequestResponseOK<ArchiveUnitProfileModel> profileResponse = new RequestResponseOK<>(
            null,
            Arrays.asList(profile),
            2
        );

        Mockito.when(archiveUnitProfileService.findArchiveUnitProfiles(Mockito.any())).thenReturn(profileResponse);
        Mockito.when(schemaService.findUnitSchema()).thenReturn(schemas);

        List<CombinedSchemaModel> combinedSchemaModels =
            archiveUnitProfileSchemaServiceImpl.getCombinedSchemaModelsById(identifier);

        Assert.assertNotNull(combinedSchemaModels);
        Assert.assertEquals(2, combinedSchemaModels.size());

        CombinedSchemaModel combinedSchemaAddressee = combinedSchemaModels.get(0);
        Assert.assertEquals("Addressee", combinedSchemaAddressee.getFieldName());
        Assert.assertNotEquals("ZERO", combinedSchemaAddressee.getEffectiveCardinality());

        CombinedSchemaModel combinedSchemaBirthDate = combinedSchemaModels.get(1);
        Assert.assertEquals("Addressee.BirthDate", combinedSchemaBirthDate.getFieldName());
        Assert.assertEquals("ZERO", combinedSchemaBirthDate.getEffectiveCardinality());
    }

    @Test
    public void testEffectiveCardinalityZero()
        throws ReferentialException, InvalidParseOperationException, IOException, InvalidCreateOperationException {
        String identifier = "AUP-000003";

        JsonNode schemaTemplate = JsonSchemaUtil.loadSchemaTemplate();
        String controlSchema = JsonSchemaUtil.modifySchema(schemaTemplate, Arrays.asList("ForbiddenDate"));
        ArchiveUnitProfileModel profile = createProfile(controlSchema, Arrays.asList("ForbiddenDate"));

        SchemaResponse schema = createSchemaResponse("ForbiddenDate", SchemaType.TEXT, SchemaCardinality.ONE);

        List<SchemaResponse> schemas = Arrays.asList(schema);

        RequestResponseOK<ArchiveUnitProfileModel> profileResponse = new RequestResponseOK<>(
            null,
            Arrays.asList(profile),
            2
        );

        Mockito.when(archiveUnitProfileService.findArchiveUnitProfiles(Mockito.any())).thenReturn(profileResponse);
        Mockito.when(schemaService.findUnitSchema()).thenReturn(schemas);

        List<CombinedSchemaModel> combinedSchemaModels =
            archiveUnitProfileSchemaServiceImpl.getCombinedSchemaModelsById(identifier);

        Assert.assertNotNull(combinedSchemaModels);
        Assert.assertEquals(1, combinedSchemaModels.size());

        CombinedSchemaModel combinedSchema = combinedSchemaModels.get(0);
        Assert.assertEquals("ForbiddenDate", combinedSchema.getFieldName());
        Assert.assertEquals("ZERO", combinedSchema.getEffectiveCardinality());
    }

    @Test
    public void testEffectiveCardinalityMany()
        throws ReferentialException, InvalidParseOperationException, IOException, InvalidCreateOperationException {
        String identifier = "AUP-000004";

        JsonNode schemaTemplate = JsonSchemaUtil.loadSchemaTemplate();
        String controlSchema = JsonSchemaUtil.modifySchema(schemaTemplate, Arrays.asList("MultipleDates"));
        ArchiveUnitProfileModel profile = createProfile(controlSchema, Arrays.asList("MultipleDates"));

        SchemaResponse schema = createSchemaResponse("MultipleDates", SchemaType.DATE, SchemaCardinality.MANY);

        List<SchemaResponse> schemas = Arrays.asList(schema);

        RequestResponseOK<ArchiveUnitProfileModel> profileResponse = new RequestResponseOK<>(
            null,
            Arrays.asList(profile),
            2
        );

        Mockito.when(archiveUnitProfileService.findArchiveUnitProfiles(Mockito.any())).thenReturn(profileResponse);
        Mockito.when(schemaService.findUnitSchema()).thenReturn(schemas);

        List<CombinedSchemaModel> combinedSchemaModels =
            archiveUnitProfileSchemaServiceImpl.getCombinedSchemaModelsById(identifier);

        Assert.assertNotNull(combinedSchemaModels);
        Assert.assertEquals(1, combinedSchemaModels.size());

        CombinedSchemaModel combinedSchema = combinedSchemaModels.get(0);
        Assert.assertEquals("MultipleDates", combinedSchema.getFieldName());
        Assert.assertEquals("DATE", combinedSchema.getControl().getType());
        Assert.assertEquals("MANY_REQUIRED", combinedSchema.getEffectiveCardinality());
    }

    @Test
    public void testEffectiveCardinalityArray()
        throws ReferentialException, InvalidParseOperationException, IOException, InvalidCreateOperationException {
        String identifier = "AUP-000005";

        JsonNode schemaTemplate = JsonSchemaUtil.loadSchemaTemplate();
        String controlSchema = JsonSchemaUtil.modifySchema(schemaTemplate, Arrays.asList("Tags"));
        ArchiveUnitProfileModel profile = createProfile(controlSchema, Arrays.asList("Tags"));

        SchemaResponse schema = createSchemaResponse("Tags", SchemaType.KEYWORD, SchemaCardinality.MANY);

        List<SchemaResponse> schemas = Arrays.asList(schema);

        RequestResponseOK<ArchiveUnitProfileModel> profileResponse = new RequestResponseOK<>(
            null,
            Arrays.asList(profile),
            2
        );

        Mockito.when(archiveUnitProfileService.findArchiveUnitProfiles(Mockito.any())).thenReturn(profileResponse);
        Mockito.when(schemaService.findUnitSchema()).thenReturn(schemas);

        List<CombinedSchemaModel> combinedSchemaModels =
            archiveUnitProfileSchemaServiceImpl.getCombinedSchemaModelsById(identifier);

        Assert.assertNotNull(combinedSchemaModels);
        Assert.assertEquals(1, combinedSchemaModels.size());

        CombinedSchemaModel combinedSchema = combinedSchemaModels.get(0);
        Assert.assertEquals("Tags", combinedSchema.getFieldName());
        Assert.assertNull(combinedSchema.getControl().getType());
        Assert.assertEquals("MANY_REQUIRED", combinedSchema.getEffectiveCardinality());
    }
}

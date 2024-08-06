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
import fr.gouv.vitam.common.database.builder.query.QueryHelper;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.database.parser.request.adapter.SingleVarNameAdapter;
import fr.gouv.vitam.common.database.parser.request.single.SelectParserSingle;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.ArchiveUnitProfileModel;
import fr.gouv.vitam.common.model.administration.CombinedSchemaModel;
import fr.gouv.vitam.common.model.administration.schema.SchemaResponse;
import fr.gouv.vitam.functional.administration.common.exception.ReferentialException;
import fr.gouv.vitam.functional.administration.core.archiveunitprofiles.ArchiveUnitProfileService;
import fr.gouv.vitam.functional.administration.core.schema.SchemaService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ArchiveUnitProfileSchemaServiceImpl implements ArchiveUnitProfileSchemaService {

    private final ArchiveUnitProfileService archiveUnitProfileService;
    private final SchemaService schemaService;

    public ArchiveUnitProfileSchemaServiceImpl(
        ArchiveUnitProfileService archiveUnitProfileService,
        SchemaService schemaService
    ) {
        this.archiveUnitProfileService = archiveUnitProfileService;
        this.schemaService = schemaService;
    }

    public List<CombinedSchemaModel> getCombinedSchemaModels(
        ArchiveUnitProfileModel profile,
        List<SchemaResponse> schemas
    ) throws InvalidParseOperationException, IOException {
        List<CombinedSchemaModel> combinedSchemaModels = new ArrayList<>();

        JsonNode controlSchemaNode = JsonHandler.getFromString(profile.getControlSchema());

        // Retrieve the list of required fields
        JsonNode requiredNode = controlSchemaNode.path("required");
        List<String> requiredFields = requiredNode.isMissingNode()
            ? new ArrayList<>()
            : JsonHandler.getFromJsonNode(requiredNode, List.class, String.class);

        for (SchemaResponse schema : schemas) {
            CombinedSchemaModel model = this.build(schema, controlSchemaNode, requiredFields);
            combinedSchemaModels.add(model);
        }

        return combinedSchemaModels;
    }

    public CombinedSchemaModel build(SchemaResponse schema, JsonNode controlSchemaNode, List<String> requiredFields) {
        List<CombinedSchemaProcessor> processors = new ArrayList<>();
        processors.add(new SchemaFillerProcessor(schema));
        processors.add(new ControlProcessor(controlSchemaNode, schema));
        processors.add(new EffectiveCardinalityProcessor(schema, controlSchemaNode, requiredFields));
        CombinedSchemaModel model = new CombinedSchemaModel();
        for (CombinedSchemaProcessor processor : processors) {
            processor.process(model);
        }
        return model;
    }

    @Override
    public List<CombinedSchemaModel> getCombinedSchemaModelsById(String identifier)
        throws ReferentialException, InvalidParseOperationException, IOException, InvalidCreateOperationException {
        // Récupérer le profil d'unité d'archive par identifiant
        RequestResponseOK<ArchiveUnitProfileModel> profileResponse = archiveUnitProfileService.findArchiveUnitProfiles(
            buildQueryByIdentifier(identifier)
        );

        if (!profileResponse.isOk() || profileResponse.getResults().isEmpty()) {
            throw new ReferentialException("Archive unit profile not found for identifier: " + identifier);
        }

        ArchiveUnitProfileModel profile = profileResponse.getResults().get(0);

        // Récupérer les schémas
        List<SchemaResponse> schemas = schemaService.findUnitSchema();

        // Appeler getCombinedSchemaModels pour obtenir le schéma combiné
        return getCombinedSchemaModels(profile, schemas);
    }

    private JsonNode buildQueryByIdentifier(String identifier)
        throws InvalidParseOperationException, InvalidCreateOperationException {
        SelectParserSingle parser = new SelectParserSingle(new SingleVarNameAdapter());
        parser.parse(new Select().getFinalSelect());
        parser.addCondition(QueryHelper.eq("Identifier", identifier));
        return parser.getRequest().getFinalSelect();
    }
}

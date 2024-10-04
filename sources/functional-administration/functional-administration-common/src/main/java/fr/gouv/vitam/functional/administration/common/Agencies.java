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
package fr.gouv.vitam.functional.administration.common;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.database.server.mongodb.VitamDocument;
import fr.gouv.vitam.common.model.administration.AgenciesModel;
import org.bson.Document;

import java.util.List;

/**
 * Defines a Agency collection. </BR>
 */
public class Agencies extends VitamDocument<Agencies> {

    /**
     *
     */
    private static final long serialVersionUID = 4196706995314270224L;

    /**
     * the Agency id
     */
    public static final String IDENTIFIER = "Identifier";

    /**
     * the Agency name
     */
    public static final String NAME = "Name";

    /**
     * the Agency description
     */
    public static final String DESCRIPTION = "Description";
    private static final String TENANT = "_tenant";

    public static final String ENTITY_TYPE = "EntityType";
    public static final String NAME_ENTRY_PARALLEL = "NameEntryParallel";
    public static final String AUTHORIZED_FORM = "AuthorizedForm";
    public static final String ALTERNATIVE_FORM = "AlternativeForm";
    public static final String ENTITY_ID = "EntityId";
    public static final String FROM_DATE = "FromDate";
    public static final String TO_DATE = "ToDate";
    public static final String FUNCTIONS = "Functions";
    public static final String BIOG_HIST = "BiogHist";
    public static final String PLACES = "Places";
    public static final String LEGAL_STATUSES = "LegalStatuses";
    public static final String MANDATES = "Mandates";
    public static final String STRUCTURE_OR_GENEALOGY = "StructureOrGenealogy";
    public static final String GENERAL_CONTEXT = "GeneralContext";
    public static final String CREATION_DATE = "CreationDate";
    public static final String UPDATE_DATE = "UpdateDate";
    public static final String MAINTENANCE_STATUS = "MaintenanceStatus";
    public static final String LOCAL_STATUS = "LocalStatus";
    public static final String SOURCES = "Sources";
    public static final String EVENT_DESCRIPTION = "EventDescription";

    /**
     * /**
     * Empty Constructor
     */
    public Agencies() {}

    /**
     * @param tenantId the working tenant
     */
    public Agencies(Integer tenantId) {
        // Empty
        append(TENANT, tenantId);
    }

    /**
     * Constructor
     *
     * @param document data in format Document to create agency
     */
    public Agencies(Document document) {
        super(document);
    }

    /**
     * @param content in format JsonNode to create agency
     */
    public Agencies(JsonNode content) {
        super(content);
    }

    /**
     * @param content in format String to create agency
     */
    public Agencies(String content) {
        super(content);
    }

    @Override
    public VitamDocument<Agencies> newInstance(JsonNode content) {
        return new Agencies(content);
    }

    /**
     * Get the Agency description
     *
     * @return this
     */
    public String getDescription() {
        return getString(DESCRIPTION);
    }

    /**
     * Set or change the Agency description
     *
     * @param description to set to Agency
     * @return this
     */
    public Agencies setDescription(String description) {
        append(DESCRIPTION, description);
        return this;
    }

    /**
     * Get the Agency Identifier
     *
     * @return this
     */
    public String getIdentifier() {
        return getString(IDENTIFIER);
    }

    /**
     * Set or change the Agency identifier
     *
     * @param identifier to set to Agency
     * @return this
     */
    public Agencies setIdentifier(String identifier) {
        append(IDENTIFIER, identifier);
        return this;
    }

    /**
     * Get the Agency name
     *
     * @return this
     */
    public String getName() {
        return getString(NAME);
    }

    /**
     * Set or change the Agency name
     *
     * @param name to set to Agency
     * @return this
     */
    public Agencies setName(String name) {
        append(NAME, name);
        return this;
    }

    public String getEntityType() {
        return getString(ENTITY_TYPE);
    }

    public List<String> getNameEntryParallel() {
        return getList(NAME_ENTRY_PARALLEL, String.class);
    }

    public List<String> getAuthorizedForm() {
        return getList(AUTHORIZED_FORM, String.class);
    }

    public List<String> getAlternativeForm() {
        return getList(ALTERNATIVE_FORM, String.class);
    }

    public String getEntityId() {
        return getString(ENTITY_ID);
    }

    public String getFromDate() {
        return getString(FROM_DATE);
    }

    public String getToDate() {
        return getString(TO_DATE);
    }

    public List<String> getFunctions() {
        return getList(FUNCTIONS, String.class);
    }

    public String getBiogHist() {
        return getString(BIOG_HIST);
    }

    public List<String> getPlaces() {
        return getList(PLACES, String.class);
    }

    public List<String> getLegalStatuses() {
        return getList(LEGAL_STATUSES, String.class);
    }

    public List<String> getMandates() {
        return getList(MANDATES, String.class);
    }

    public String getStructureOrGenealogy() {
        return getString(STRUCTURE_OR_GENEALOGY);
    }

    public String getGeneralContext() {
        return getString(GENERAL_CONTEXT);
    }

    public String getCreationDate() {
        return getString(CREATION_DATE);
    }

    public String getUpdateDate() {
        return getString(UPDATE_DATE);
    }

    public String getMaintenanceStatus() {
        return getString(MAINTENANCE_STATUS);
    }

    public String getLocalStatus() {
        return getString(LOCAL_STATUS);
    }

    public List<String> getSources() {
        return getList(SOURCES, String.class);
    }

    public String getEventDescription() {
        return getString(EVENT_DESCRIPTION);
    }

    public Agencies setEntityType(String entityType) {
        append(ENTITY_TYPE, entityType);
        return this;
    }

    public Agencies setNameEntryParallel(List<String> nameEntryParallel) {
        append(NAME_ENTRY_PARALLEL, nameEntryParallel);
        return this;
    }

    public Agencies setAuthorizedForm(List<String> authorizedForm) {
        append(AUTHORIZED_FORM, authorizedForm);
        return this;
    }

    public Agencies setAlternativeForm(List<String> alternativeForm) {
        append(ALTERNATIVE_FORM, alternativeForm);
        return this;
    }

    public Agencies setEntityId(String entityId) {
        append(ENTITY_ID, entityId);
        return this;
    }

    public Agencies setFromDate(String fromDate) {
        append(FROM_DATE, fromDate);
        return this;
    }

    public Agencies setToDate(String toDate) {
        append(TO_DATE, toDate);
        return this;
    }

    public Agencies setFunctions(List<String> functions) {
        append(FUNCTIONS, functions);
        return this;
    }

    public Agencies setBiogHist(String biogHist) {
        append(BIOG_HIST, biogHist);
        return this;
    }

    public Agencies setPlaces(List<String> places) {
        append(PLACES, places);
        return this;
    }

    public Agencies setLegalStatuses(List<String> legalStatuses) {
        append(LEGAL_STATUSES, legalStatuses);
        return this;
    }

    public Agencies setMandates(List<String> mandates) {
        append(MANDATES, mandates);
        return this;
    }

    public Agencies setStructureOrGenealogy(String structureOrGenealogy) {
        append(STRUCTURE_OR_GENEALOGY, structureOrGenealogy);
        return this;
    }

    public Agencies setGeneralContext(String generalContext) {
        append(GENERAL_CONTEXT, generalContext);
        return this;
    }

    public Agencies setCreationDate(String creationDate) {
        append(CREATION_DATE, creationDate);
        return this;
    }

    public Agencies setUpdateDate(String updateDate) {
        append(UPDATE_DATE, updateDate);
        return this;
    }

    public Agencies setMaintenanceStatus(String maintenanceStatus) {
        append(MAINTENANCE_STATUS, maintenanceStatus);
        return this;
    }

    public Agencies setLocalStatus(String localStatus) {
        append(LOCAL_STATUS, localStatus);
        return this;
    }

    public Agencies setSources(List<String> sources) {
        append(SOURCES, sources);
        return this;
    }

    public Agencies setEventDescription(String eventDescription) {
        append(EVENT_DESCRIPTION, eventDescription);
        return this;
    }

    public AgenciesModel wrap() {
        return new AgenciesModel(
            this.getIdentifier(),
            this.getName(),
            this.getDescription(),
            this.get("#tenant", Integer.class)
        )
            .setToDate(this.getToDate())
            .setFromDate(this.getFromDate())
            .setUpdateDate(this.getUpdateDate())
            .setEntityType(this.getEntityType())
            .setEntityId(this.getEntityId())
            .setNameEntryParallel(this.getNameEntryParallel())
            .setAuthorizedForm(this.getAuthorizedForm())
            .setAlternativeForm(this.getAlternativeForm())
            .setFunctions(this.getFunctions())
            .setBiogHist(this.getBiogHist())
            .setPlaces(this.getPlaces())
            .setLegalStatuses(this.getLegalStatuses())
            .setMandates(this.getMandates())
            .setStructureOrGenealogy(this.getStructureOrGenealogy())
            .setGeneralContext(this.getGeneralContext())
            .setCreationDate(this.getCreationDate())
            .setMaintenanceStatus(this.getMaintenanceStatus())
            .setLocalStatus(this.getLocalStatus())
            .setEventDescription(this.getEventDescription())
            .setSources(this.getSources());
    }
}

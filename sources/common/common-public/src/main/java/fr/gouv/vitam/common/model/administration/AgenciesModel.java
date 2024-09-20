/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL-C license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL-C license as
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
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL-C license and that you
 * accept its terms.
 */
package fr.gouv.vitam.common.model.administration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.gouv.vitam.common.model.ModelConstants;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Data Transfer Object Model of Agency
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AgenciesModel {

    public static final String TAG_NAME = "Name";
    public static final String TAG_IDENTIFIER = "Identifier";
    public static final String TAG_DESCRIPTION = "Description";
    public static final String TAG_ENTITY_TYPE = "EntityType";
    public static final String TAG_NAME_ENTRY_PARALLEL = "NameEntryParallel";
    public static final String TAG_AUTHORIZED_FORM = "AuthorizedForm";
    public static final String TAG_ALTERNATIVE_FORM = "AlternativeForm";
    public static final String TAG_ENTITY_ID = "EntityId";
    public static final String TAG_FROM_DATE = "FromDate";
    public static final String TAG_TO_DATE = "ToDate";
    public static final String TAG_FUNCTIONS = "Functions";
    public static final String TAG_BIOG_HIST = "BiogHist";
    public static final String TAG_PLACES = "Places";
    public static final String TAG_LEGAL_STATUSES = "LegalStatuses";
    public static final String TAG_MANDATES = "Mandates";
    public static final String TAG_STRUCTURE_OR_GENEALOGY = "StructureOrGenealogy";
    public static final String TAG_GENERAL_CONTEXT = "GeneralContext";
    public static final String TAG_CREATION_DATE = "CreationDate";
    public static final String TAG_UPDATE_DATE = "UpdateDate";
    public static final String TAG_MAINTENANCE_STATUS = "MaintenanceStatus";
    public static final String TAG_LOCAL_STATUS = "LocalStatus";
    public static final String TAG_SOURCES = "Sources";
    public static final String TAG_EVENT_DESCRIPTION = "EventDescription";

    /**
     * unique id
     */
    @JsonProperty(ModelConstants.HASH + ModelConstants.TAG_ID)
    private String id;

    /**
     * tenant id
     */
    @JsonProperty(ModelConstants.UNDERSCORE + ModelConstants.TAG_TENANT)
    private Integer tenant;

    /**
     * document version
     */
    @JsonProperty(ModelConstants.HASH + ModelConstants.TAG_VERSION)
    private Integer version;

    @JsonProperty(TAG_NAME)
    private String name;

    @JsonProperty(TAG_IDENTIFIER)
    private String identifier;

    @JsonProperty(TAG_DESCRIPTION)
    private String description;

    @JsonProperty(TAG_ENTITY_TYPE)
    private String entityType;

    @JsonProperty(TAG_NAME_ENTRY_PARALLEL)
    private List<String> nameEntryParallel;

    @JsonProperty(TAG_AUTHORIZED_FORM)
    private List<String> authorizedForm;

    @JsonProperty(TAG_ALTERNATIVE_FORM)
    private List<String> alternativeForm;

    @JsonProperty(TAG_ENTITY_ID)
    private String entityId;

    @JsonProperty(TAG_FROM_DATE)
    private String fromDate;

    @JsonProperty(TAG_TO_DATE)
    private String toDate;

    @JsonProperty(TAG_FUNCTIONS)
    private List<String> functions;

    @JsonProperty(TAG_BIOG_HIST)
    private String biogHist;

    @JsonProperty(TAG_PLACES)
    private List<String> places;

    @JsonProperty(TAG_LEGAL_STATUSES)
    private List<String> legalStatuses;

    @JsonProperty(TAG_MANDATES)
    private List<String> mandates;

    @JsonProperty(TAG_STRUCTURE_OR_GENEALOGY)
    private String structureOrGenealogy;

    @JsonProperty(TAG_GENERAL_CONTEXT)
    private String generalContext;

    @JsonProperty(TAG_CREATION_DATE)
    private String creationDate;

    @JsonProperty(TAG_UPDATE_DATE)
    private String updateDate;

    @JsonProperty(TAG_MAINTENANCE_STATUS)
    private String maintenanceStatus;

    @JsonProperty(TAG_LOCAL_STATUS)
    private String localStatus;

    @JsonProperty(TAG_SOURCES)
    private List<String> sources;

    //EventDescription or MaintenanceEvent
    @JsonProperty(TAG_EVENT_DESCRIPTION)
    private String eventDescription;

    /**
     * Constructor of AgencyModel
     *
     * @param identifier
     * @param name
     * @param description
     */
    public AgenciesModel(
        @JsonProperty(TAG_IDENTIFIER) String identifier,
        @JsonProperty(TAG_NAME) String name,
        @JsonProperty(TAG_DESCRIPTION) String description,
        @JsonProperty(ModelConstants.HASH + ModelConstants.TAG_TENANT) int tenant
    ) {
        this.tenant = tenant;
        this.identifier = identifier;
        this.name = name;
        this.description = description;
    }

    /**
     * empty constructor
     */
    public AgenciesModel() {}

    /**
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     * @return AgencyModel
     */
    public AgenciesModel setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * @return tenant
     */
    public Integer getTenant() {
        return tenant;
    }

    /**
     * @param tenant value to set working tenant
     * @return this
     */
    public AgenciesModel setTenant(Integer tenant) {
        this.tenant = tenant;
        return this;
    }

    /**
     * @return version
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * @param version
     */
    public AgenciesModel setVersion(Integer version) {
        this.version = version;
        return this;
    }

    /**
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     */
    public AgenciesModel setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * @return identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier
     * @return AgencyModel
     */
    public AgenciesModel setIdentifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    /**
     * @return last update of Agency
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @param description to set
     * @return this
     */
    public AgenciesModel setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getEntityType() {
        return entityType;
    }

    public List<String> getNameEntryParallel() {
        return nameEntryParallel;
    }

    public List<String> getAuthorizedForm() {
        return authorizedForm;
    }

    public List<String> getAlternativeForm() {
        return alternativeForm;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getFromDate() {
        return fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public List<String> getFunctions() {
        return functions;
    }

    public String getBiogHist() {
        return biogHist;
    }

    public List<String> getPlaces() {
        return places;
    }

    public List<String> getLegalStatuses() {
        return legalStatuses;
    }

    public List<String> getMandates() {
        return mandates;
    }

    public String getStructureOrGenealogy() {
        return structureOrGenealogy;
    }

    public String getGeneralContext() {
        return generalContext;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public String getMaintenanceStatus() {
        return maintenanceStatus;
    }

    public String getLocalStatus() {
        return localStatus;
    }

    public List<String> getSources() {
        return sources;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public AgenciesModel setEntityType(String entityType) {
        this.entityType = entityType;
        return this;
    }

    public AgenciesModel setNameEntryParallel(List<String> nameEntryParallel) {
        this.nameEntryParallel = nameEntryParallel;
        return this;
    }

    public AgenciesModel setAuthorizedForm(List<String> authorizedForm) {
        this.authorizedForm = authorizedForm;
        return this;
    }

    public AgenciesModel setAlternativeForm(List<String> alternativeForm) {
        this.alternativeForm = alternativeForm;
        return this;
    }

    public AgenciesModel setEntityId(String entityId) {
        this.entityId = entityId;
        return this;
    }

    public AgenciesModel setFromDate(String fromDate) {
        this.fromDate = fromDate;
        return this;
    }

    public AgenciesModel setToDate(String toDate) {
        this.toDate = toDate;
        return this;
    }

    public AgenciesModel setFunctions(List<String> functions) {
        this.functions = functions;
        return this;
    }

    public AgenciesModel setBiogHist(String biogHist) {
        this.biogHist = biogHist;
        return this;
    }

    public AgenciesModel setPlaces(List<String> places) {
        this.places = places;
        return this;
    }

    public AgenciesModel setLegalStatuses(List<String> legalStatuses) {
        this.legalStatuses = legalStatuses;
        return this;
    }

    public AgenciesModel setMandates(List<String> mandates) {
        this.mandates = mandates;
        return this;
    }

    public AgenciesModel setStructureOrGenealogy(String structureOrGenealogy) {
        this.structureOrGenealogy = structureOrGenealogy;
        return this;
    }

    public AgenciesModel setGeneralContext(String generalContext) {
        this.generalContext = generalContext;
        return this;
    }

    public AgenciesModel setCreationDate(String creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    public AgenciesModel setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
        return this;
    }

    public AgenciesModel setMaintenanceStatus(String maintenanceStatus) {
        this.maintenanceStatus = maintenanceStatus;
        return this;
    }

    public AgenciesModel setLocalStatus(String localStatus) {
        this.localStatus = localStatus;
        return this;
    }

    public AgenciesModel setSources(List<String> sources) {
        this.sources = sources;
        return this;
    }

    public AgenciesModel setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
        return this;
    }

    public static Set<String> getAllFieldNames() {
        return Set.of(
            TAG_NAME,
            TAG_IDENTIFIER,
            TAG_DESCRIPTION,
            TAG_ENTITY_TYPE,
            TAG_NAME_ENTRY_PARALLEL,
            TAG_AUTHORIZED_FORM,
            TAG_ALTERNATIVE_FORM,
            TAG_ENTITY_ID,
            TAG_FROM_DATE,
            TAG_TO_DATE,
            TAG_FUNCTIONS,
            TAG_BIOG_HIST,
            TAG_PLACES,
            TAG_LEGAL_STATUSES,
            TAG_MANDATES,
            TAG_STRUCTURE_OR_GENEALOGY,
            TAG_GENERAL_CONTEXT,
            TAG_CREATION_DATE,
            TAG_UPDATE_DATE,
            TAG_MAINTENANCE_STATUS,
            TAG_LOCAL_STATUS,
            TAG_SOURCES,
            TAG_EVENT_DESCRIPTION
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgenciesModel that = (AgenciesModel) o;

        return (
            Objects.equals(this.tenant, that.tenant) &&
            Objects.equals(identifier, that.identifier) &&
            Objects.equals(name, that.name) &&
            Objects.equals(description, that.description) &&
            Objects.equals(alternativeForm, that.alternativeForm) &&
            Objects.equals(authorizedForm, that.authorizedForm) &&
            Objects.equals(biogHist, that.biogHist) &&
            Objects.equals(creationDate, that.creationDate) &&
            Objects.equals(entityId, that.entityId) &&
            Objects.equals(entityType, that.entityType) &&
            Objects.equals(fromDate, that.fromDate) &&
            Objects.equals(eventDescription, that.eventDescription) &&
            Objects.equals(functions, that.functions) &&
            Objects.equals(generalContext, that.generalContext) &&
            Objects.equals(mandates, that.mandates) &&
            Objects.equals(legalStatuses, that.legalStatuses) &&
            Objects.equals(localStatus, that.localStatus) &&
            Objects.equals(maintenanceStatus, that.maintenanceStatus) &&
            Objects.equals(nameEntryParallel, that.nameEntryParallel) &&
            Objects.equals(places, that.places) &&
            Objects.equals(sources, that.sources)
        );
    }

    //FIXME : We keep the definition of methods equals/hashCode as written before without including new fields,
    // clean solution should be handled in the bug 13497
    @Override
    public int hashCode() {
        return Objects.hash(
            tenant,
            identifier,
            name,
            description,
            alternativeForm,
            authorizedForm,
            biogHist,
            creationDate,
            entityId,
            entityType,
            fromDate,
            eventDescription,
            functions,
            generalContext,
            mandates,
            legalStatuses,
            localStatus,
            maintenanceStatus,
            nameEntryParallel,
            places,
            sources
        );
    }
}

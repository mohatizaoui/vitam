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
package fr.gouv.vitam.common.model.administration.profile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.gouv.vitam.common.model.ModelConstants;
import fr.gouv.vitam.common.model.administration.ProfileFormat;
import fr.gouv.vitam.common.model.administration.ProfileSedaVersion;
import fr.gouv.vitam.common.model.administration.ProfileStatus;

/**
 * Data Transfer Object Model of Profile (DTO).
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ProfileModel {

    public static final String TAG_IDENTIFIER = "Identifier";
    public static final String TAG_NAME = "Name";
    public static final String TAG_DESCRIPTION = "Description";
    public static final String TAG_STATUS = "Status";
    public static final String TAG_FORMAT = "Format";
    public static final String TAG_SEDA_VERSION = "SedaVersion";
    public static final String TAG_PATH = "Path";
    public static final String CREATION_DATE = "CreationDate";
    public static final String LAST_UPDATE = "LastUpdate";
    public static final String ACTIVATION_DATE = "ActivationDate";
    public static final String DEACTIVATION_DATE = "DeactivationDate";

    /**
     * unique id
     */
    @JsonProperty(ModelConstants.HASH + ModelConstants.TAG_ID)
    private String id;

    /**
     * tenant id
     */
    @JsonProperty(ModelConstants.HASH + ModelConstants.TAG_TENANT)
    private Integer tenant;

    /**
     * document version
     */
    @JsonProperty(ModelConstants.HASH + ModelConstants.TAG_VERSION)
    private Integer version;

    @JsonProperty(TAG_IDENTIFIER)
    protected String identifier;

    @JsonProperty(TAG_NAME)
    protected String name;

    @JsonProperty(TAG_DESCRIPTION)
    protected String description;

    @JsonProperty(TAG_STATUS)
    protected ProfileStatus status;

    @JsonProperty(TAG_FORMAT)
    protected ProfileFormat format;

    @JsonProperty(TAG_SEDA_VERSION)
    protected ProfileSedaVersion sedaVersion;

    @JsonProperty(TAG_PATH)
    private String path;

    @JsonProperty(CREATION_DATE)
    private String creationDate;

    @JsonProperty(LAST_UPDATE)
    private String lastUpdate;

    @JsonProperty(ACTIVATION_DATE)
    private String activationDate;

    @JsonProperty(DEACTIVATION_DATE)
    private String deactivationDate;

    /**
     * Constructor without fields use for jackson
     */
    public ProfileModel() {
        super();
    }

    public ProfileModel(CreateProfileModel createProfileModel) {
        identifier = createProfileModel.getIdentifier();
        name = createProfileModel.getName();
        description = createProfileModel.getDescription();
        status = createProfileModel.getStatus();
        format = createProfileModel.getFormat();
        sedaVersion = createProfileModel.getSedaVersion();
    }

    /**
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id value to set field
     * @return this
     */
    public ProfileModel setId(String id) {
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
    public ProfileModel setTenant(Integer tenant) {
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
     * @param version to set
     */
    public void setVersion(Integer version) {
        this.version = version;
    }

    /**
     * Get the identifier of the profile
     *
     * @return identifier as String
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Set the identifier of the profile This value must be unique by tenant
     *
     * @param identifier as String
     * @return this
     */
    public ProfileModel setIdentifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    /**
     * Get name of the profile
     *
     * @return name as String
     */
    public String getName() {
        return name;
    }

    /**
     * Set or change the profile name
     *
     * @param name as String to set
     * @return this
     */
    public ProfileModel setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the profile description
     *
     * @return description of profile
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set or change the profile description
     *
     * @param description to set
     * @return this
     */
    public ProfileModel setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get the profile status
     *
     * @return status of profile
     */
    public ProfileStatus getStatus() {
        return status;
    }

    /**
     * Set or change the profile status
     *
     * @param status toi set
     * @return this
     */
    public ProfileModel setStatus(ProfileStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Get the format of the profile file (xsd, rng, ...)
     *
     * @return the file format as string
     */
    public ProfileFormat getFormat() {
        return format;
    }

    /**
     * Set the profile file format (xsd, rng, ...)
     *
     * @param format to set
     * @return this
     */
    public ProfileModel setFormat(ProfileFormat format) {
        this.format = format;
        return this;
    }

    /**
     * Get the profile seda version
     *
     * @return the profile seda version
     */
    public ProfileSedaVersion getSedaVersion() {
        return sedaVersion;
    }

    /**
     * Set the profile seda version (2.1, 2.2, 2.3, ...)
     *
     * @param sedaVersion to set
     * @return this
     */
    public ProfileModel setSedaVersion(ProfileSedaVersion sedaVersion) {
        this.sedaVersion = sedaVersion;
        return this;
    }

    /**
     * @return path as String
     */
    public String getPath() {
        return path;
    }

    /**
     * Profile path in storage
     *
     * @param path to set
     * @return this
     */
    public ProfileModel setPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * @return the creation date of profile
     */
    public String getCreationDate() {
        return creationDate;
    }

    /**
     * @param creationDate to set
     * @return this
     */
    public ProfileModel setCreationDate(String creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    /**
     * @return last update of profile
     */
    public String getLastUpdate() {
        return lastUpdate;
    }

    /**
     * @param lastUpdate to set
     * @return this
     */
    public ProfileModel setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
        return this;
    }

    /**
     * @return the activation date of profile
     */
    public String getActivationDate() {
        return activationDate;
    }

    /**
     * @param activationDate to set
     * @return this
     */
    public ProfileModel setActivationDate(String activationDate) {
        this.activationDate = activationDate;
        return this;
    }

    /**
     * @return the desactivation date of profile
     */
    public String getDeactivationDate() {
        return deactivationDate;
    }

    /**
     * @param deactivationDate to set
     * @return this
     */
    public ProfileModel setDeactivationDate(String deactivationDate) {
        this.deactivationDate = deactivationDate;
        return this;
    }
}

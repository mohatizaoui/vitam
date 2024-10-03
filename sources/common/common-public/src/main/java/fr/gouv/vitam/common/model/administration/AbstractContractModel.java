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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.gouv.vitam.common.model.ModelConstants;

/**
 * Data Transfer Object Model of access contract (DTO).
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AbstractContractModel {

    public static final String TAG_DESCRIPTION = "Description";

    public static final String TAG_NAME = "Name";

    public static final String TAG_IDENTIFIER = "Identifier";

    public static final String TAG_STATUS = "Status";

    public static final String TAG_CREATION_DATE = "CreationDate";

    public static final String TAG_LAST_UPDATE = "LastUpdate";

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

    @JsonProperty(TAG_NAME)
    private String name;

    @JsonProperty(TAG_IDENTIFIER)
    private String identifier;

    @JsonProperty(TAG_DESCRIPTION)
    private String description;

    @JsonProperty(TAG_STATUS)
    private ActivationStatus status;

    @JsonProperty(TAG_CREATION_DATE)
    private String creationDate;

    @JsonProperty(TAG_LAST_UPDATE)
    private String lastUpdate;

    @JsonProperty("ActivationDate")
    private String activationDate;

    @JsonProperty("DeactivationDate")
    private String deactivationDate;

    /**
     * Constructor without fields use for jackson
     */
    public AbstractContractModel() {
        super();
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
    public AbstractContractModel setId(String id) {
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
    public AbstractContractModel setTenant(Integer tenant) {
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
    public AbstractContractModel setVersion(Integer version) {
        this.version = version;
        return this;
    }

    /**
     * Get the identifier of the contract
     *
     * @return String
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Set the identifier of the contract This value must be unique by tenant
     *
     * @param identifier as String
     * @return this
     */
    public AbstractContractModel setIdentifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    /**
     * Get name of the contract
     *
     * @return name as String
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set or change the contract name
     *
     * @param name as String to set
     * @return this
     */
    public AbstractContractModel setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the contract description
     *
     * @return description of contract
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Set or change the contract description
     *
     * @param description to set
     * @return this
     */
    public AbstractContractModel setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get the contract status
     *
     * @return status of contract
     */
    public ActivationStatus getStatus() {
        return this.status;
    }

    /**
     * Set or change the contract status
     *
     * @param status toi set
     * @return this
     */
    public AbstractContractModel setStatus(ActivationStatus status) {
        this.status = status;
        return this;
    }

    /**
     * @return the creation date of contract
     */
    public String getCreationDate() {
        return this.creationDate;
    }

    /**
     * @deprecated since 8.0, replaced with {@link #getCreationDate()}
     */
    @Deprecated(forRemoval = true, since = "8.0")
    @JsonIgnore
    public String getCreationdate() {
        return this.creationDate;
    }

    /**
     * @param creationDate to set
     * @return this
     */
    public AbstractContractModel setCreationDate(String creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    /**
     * @deprecated since 8.0, replaced with {@link #setCreationDate(String creationDate)}
     */
    @Deprecated(forRemoval = true, since = "8.0")
    @JsonIgnore
    public AbstractContractModel setCreationdate(String creationdate) {
        this.creationDate = creationdate;
        return this;
    }

    /**
     * @return last update of contract
     */
    public String getLastUpdate() {
        return this.lastUpdate;
    }

    /**
     * @deprecated since 8.0, replaced with {@link #getLastUpdate()}
     */
    @Deprecated(forRemoval = true, since = "8.0")
    @JsonIgnore
    public String getLastupdate() {
        return this.lastUpdate;
    }

    /**
     * @param lastUpdate to set
     * @return this
     */
    public AbstractContractModel setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
        return this;
    }

    /**
     * @deprecated since 8.0, replaced with {@link #setLastUpdate(String lastUpdate)}
     */
    @Deprecated(forRemoval = true, since = "8.0")
    @JsonIgnore
    public AbstractContractModel setLastupdate(String lastupdate) {
        this.lastUpdate = lastupdate;
        return this;
    }

    /**
     * @return the activation date of contracr
     */
    public String getActivationDate() {
        return this.activationDate;
    }

    /**
     * @deprecated since 8.0, replaced with {@link #getActivationDate()}
     */
    @Deprecated(forRemoval = true, since = "8.0")
    @JsonIgnore
    public String getActivationdate() {
        return this.activationDate;
    }

    /**
     * @param activationDate to set
     * @return this
     */
    public AbstractContractModel setActivationDate(String activationDate) {
        this.activationDate = activationDate;
        return this;
    }

    /**
     * @deprecated since 8.0, replaced with {@link #setActivationDate(String activationDate)}
     */
    @Deprecated(forRemoval = true, since = "8.0")
    @JsonIgnore
    public AbstractContractModel setActivationdate(String activationdate) {
        this.activationDate = activationdate;
        return this;
    }

    /**
     * @return the desactivation date of contract
     */
    public String getDeactivationDate() {
        return this.deactivationDate;
    }

    /**
     * @deprecated since 8.0, replaced with {@link #getDeactivationDate()}
     */
    @Deprecated(forRemoval = true, since = "8.0")
    @JsonIgnore
    public String getDeactivationdate() {
        return this.deactivationDate;
    }

    /**
     * @param deactivationDate to set
     * @return this
     */
    public AbstractContractModel setDeactivationDate(String deactivationDate) {
        this.deactivationDate = deactivationDate;
        return this;
    }

    /**
     * @deprecated since 8.0, replaced with {@link #setDeactivationDate(String deactivationDate)}
     */
    @Deprecated(forRemoval = true, since = "8.0")
    @JsonIgnore
    public AbstractContractModel setDeactivationdate(String deactivationdate) {
        this.deactivationDate = deactivationdate;
        return this;
    }
}

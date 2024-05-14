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
package fr.gouv.vitam.common.model.identifier;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PurgedPersistentIdentifier {

    @JsonProperty("id")
    private String id;

    @JsonProperty("tenant")
    private Integer tenant;

    @JsonProperty("version")
    private Integer version;

    @JsonProperty("persistentIdentifier")
    private List<PersistentIdentifier> persistentIdentifiers;

    @JsonProperty("type")
    private PurgedCollectionType type;

    @JsonProperty("operationId")
    private String operationId;

    @JsonProperty("operationType")
    private String operationType;

    @JsonProperty("operationLastPersistentDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "UTC")
    private String operationLastPersistentDate;

    @JsonProperty("objectGroupId")
    private String objectGroupId;

    public PurgedPersistentIdentifier() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getTenant() {
        return tenant;
    }

    public void setTenant(Integer tenant) {
        this.tenant = tenant;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public List<PersistentIdentifier> getPersistentIdentifiers() {
        return persistentIdentifiers;
    }

    public void setPersistentIdentifiers(List<PersistentIdentifier> persistentIdentifiers) {
        this.persistentIdentifiers = persistentIdentifiers;
    }

    public PurgedCollectionType getType() {
        return type;
    }

    public void setType(PurgedCollectionType type) {
        this.type = type;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getOperationLastPersistentDate() {
        return operationLastPersistentDate;
    }

    public void setOperationLastPersistentDate(String operationLastPersistentDate) {
        this.operationLastPersistentDate = operationLastPersistentDate;
    }

    public String getObjectGroupId() {
        return objectGroupId;
    }

    public void setObjectGroupId(String objectGroupId) {
        this.objectGroupId = objectGroupId;
    }
}

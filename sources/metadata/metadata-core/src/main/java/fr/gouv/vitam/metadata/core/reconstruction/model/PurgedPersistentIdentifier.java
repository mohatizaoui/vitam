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
package fr.gouv.vitam.metadata.core.reconstruction.model;

import com.mongodb.BasicDBList;
import fr.gouv.vitam.common.database.server.mongodb.BsonHelper;
import fr.gouv.vitam.common.database.server.mongodb.VitamDocument;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.unit.PersistentIdentifierModel;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class PurgedPersistentIdentifier {

    private final String id;
    private final Integer tenant;
    private final List<PersistentIdentifierModel> persistentIdentifiers;
    private final Integer version;
    private final String type;
    private final String operationId;
    private final String operationType;
    private final String operationLastPersistentDate;
    private final String objectGroupId;
    private final String archivalAgencyIdentifier;
    private final String lastPersistentDate;

    private PurgedPersistentIdentifier(Builder builder) {
        this.id = builder.id;
        this.tenant = builder.tenant;
        this.persistentIdentifiers = builder.persistentIdentifiers;
        this.version = builder.version;
        this.type = builder.type;
        this.objectGroupId = builder.objectGroupId;
        this.archivalAgencyIdentifier = builder.archivalAgencyIdentifier;
        this.operationId = builder.operationId;
        this.operationType = builder.operationType;
        this.operationLastPersistentDate = builder.operationLastPersistentDate;
        this.lastPersistentDate = builder.lastPersistentDate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Document toDocument(PurgedPersistentIdentifier purgedIdentifier) {
        Document document = new Document();

        document.append(VitamDocument.ID, purgedIdentifier.getId());
        document.append("persistentIdentifier",
            convertPersistentIdentifierModelListToDocuments(purgedIdentifier.getPersistentIdentifier()));
        document.append(VitamDocument.TENANT_ID, purgedIdentifier.getTenant());
        document.append(VitamDocument.VERSION, 0);
        document.append("type", purgedIdentifier.getType());
        document.append("idObjectGroup", purgedIdentifier.getObjectGroupId());
        document.append("archivalAgencyIdentifier", purgedIdentifier.getArchivalAgencyIdentifier());
        document.append("opId", purgedIdentifier.getOperationId());
        document.append("opType", purgedIdentifier.getOperationType());
        document.append("opEndDate", purgedIdentifier.getOperationLastPersistentDate());

        return document;
    }

    public static PurgedPersistentIdentifier fromDocument(Document document) throws InvalidParseOperationException {

        List<PersistentIdentifierModel> persistentIdentifierModels = new ArrayList<>();

        final List<Document> persistentIdentifiersAsDocuments =
            document.getList("persistentIdentifier", Document.class);
        for (Document persistentIdentifier : persistentIdentifiersAsDocuments) {
            persistentIdentifierModels
                .add(BsonHelper.fromDocumentToObject(persistentIdentifier, PersistentIdentifierModel.class));
        }

        return PurgedPersistentIdentifier.builder()
            .setId(document.getString(VitamDocument.ID))
            .setTenant(document.getInteger(VitamDocument.TENANT_ID))
            .setVersion(document.getInteger(VitamDocument.VERSION))
            .setType(document.getString("type"))
            .setObjectGroupId(document.getString("idObjectGroup"))
            .setArchivalAgencyIdentifier(document.getString("archivalAgencyIdentifier"))
            .setOperationId(document.getString("opId"))
            .setOperationType(document.getString("opType"))
            .setOperationLastPersistentDate(document.getString("opEndDate"))
            .setLastPersistentDate(document.getString("lastPersistentDate"))
            .setPersistentIdentifier(persistentIdentifierModels)
            .build();
    }

    public static List<Document> convertListToDocumentList(
        List<PurgedPersistentIdentifier> purgedIdentifierList) {
        List<Document> documentList = new ArrayList<>();
        for (PurgedPersistentIdentifier purgedIdentifier : purgedIdentifierList) {
            Document document = toDocument(purgedIdentifier);
            documentList.add(document);
        }
        return documentList;
    }

    private static BasicDBList convertPersistentIdentifierModelListToDocuments(
        List<PersistentIdentifierModel> persistentIdentifierModels) {
        BasicDBList bsonArray = new BasicDBList();
        for (PersistentIdentifierModel persistentIdentifierModel : persistentIdentifierModels) {
            bsonArray.add(Document.parse(JsonHandler.unprettyPrint(persistentIdentifierModel)));
        }
        return bsonArray;
    }

    public String getId() {
        return id;
    }

    public Integer getTenant() {
        return tenant;
    }

    public List<PersistentIdentifierModel> getPersistentIdentifier() {
        return persistentIdentifiers;
    }

    public Integer getVersion() {
        return version;
    }

    public String getType() {
        return type;
    }

    public String getObjectGroupId() {
        return objectGroupId;
    }

    public String getArchivalAgencyIdentifier() {
        return archivalAgencyIdentifier;
    }

    public String getOperationId() {
        return operationId;
    }

    public String getOperationType() {
        return operationType;
    }

    public String getOperationLastPersistentDate() {
        return operationLastPersistentDate;
    }

    public String getLastPersistentDate(){
        return lastPersistentDate;
    }

    public static class Builder {

        private String id;
        private Integer tenant;
        private List<PersistentIdentifierModel> persistentIdentifiers;
        private Integer version;
        private String type;
        private String objectGroupId;
        private String operationId;
        private String operationType;
        private String operationLastPersistentDate;
        private String archivalAgencyIdentifier;
        public String lastPersistentDate;

        public Builder() {
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setTenant(Integer tenant) {
            this.tenant = tenant;
            return this;
        }

        public Builder setPersistentIdentifier(List<PersistentIdentifierModel> persistentIdentifiers) {
            this.persistentIdentifiers = persistentIdentifiers;
            return this;
        }

        public Builder setVersion(Integer version) {
            this.version = version;
            return this;
        }

        public Builder setObjectGroupId(String objectGroupId) {
            this.objectGroupId = objectGroupId;
            return this;
        }

        public Builder setArchivalAgencyIdentifier(String archivalAgencyIdentifier) {
            this.archivalAgencyIdentifier = archivalAgencyIdentifier;
            return this;
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder setLastPersistentDate(String lastPersistentDate) {
            this.lastPersistentDate = lastPersistentDate;
            return this;
        }

        public Builder setOperationId(String operationId) {
            this.operationId = operationId;
            return this;
        }

        public Builder setOperationType(String operationType) {
            this.operationType = operationType;
            return this;
        }

        public Builder setOperationLastPersistentDate(String operationLastPersistentDate) {
            this.operationLastPersistentDate = operationLastPersistentDate;
            return this;
        }

        public PurgedPersistentIdentifier build() {
            return new PurgedPersistentIdentifier(this);
        }

    }

}

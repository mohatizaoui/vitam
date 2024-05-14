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
package fr.gouv.vitam.functional.administration.common.schema;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.database.server.mongodb.VitamDocument;
import org.bson.Document;

/**
 * Defines a schema collection. </BR>
 */
public class Schema extends VitamDocument<Schema> {

    /**
     * Collections Tag
     */
    public static final String TAG_COLLECTION = "Collection";

    /**
     * Description Tag
     */
    public static final String TAG_DESCRIPTION = "Description";

    /**
     * Origin Tag
     */
    public static final String TAG_ORIGIN = "Origin";

    /**
     * ShortName Tag
     */
    public static final String TAG_SHORT_NAME = "ShortName";

    /**
     * isObject Tag
     */
    public static final String TAG_IS_OBJECT_NAME = "IsObject";

    /**
     * The path tag
     */
    public static final String TAG_PATH = "Path";

    /**
     * pathCardinality tag
     */
    public static final String TAG_PATH_CARDINALITY = "Cardinality";

    /**
     * the creation date of the schema
     */
    public static final String CREATIONDATE = "CreationDate";
    /**
     * the last update of schema
     */
    public static final String LAST_UPDATE = "LastUpdate";

    /**
     * Empty Constructor
     */
    public Schema() {}

    /**
     * Constructor
     *
     * @param document data in format Document to create Schema
     */
    public Schema(Document document) {
        super(document);
    }

    /**
     * @param content in format JsonNode to create Schema
     */
    public Schema(JsonNode content) {
        super(content);
    }

    /**
     * @param content in format String to create Schema
     */
    public Schema(String content) {
        super(content);
    }

    /**
     * @param tenantId the working tenant
     */
    public Schema(Integer tenantId) {
        append(TENANT_ID, tenantId);
    }

    @Override
    public VitamDocument<Schema> newInstance(JsonNode content) {
        return new Schema(content);
    }

    /**
     * @param id the id of schema
     * @return
     */
    public Schema setId(String id) {
        append(VitamDocument.ID, id);
        return this;
    }

    /**
     * The schema description
     *
     * @return the description of the schema
     */
    public String getDescription() {
        return getString(TAG_DESCRIPTION);
    }

    /**
     * Set or change the schema description
     *
     * @param description
     * @return this
     */
    public Schema setDescription(String description) {
        append(TAG_DESCRIPTION, description);
        return this;
    }

    /**
     * The schema collection of the element
     *
     * @return the collection of the schema element
     */
    public String getCollection() {
        return getString(TAG_COLLECTION);
    }

    /**
     * Set or change the schema collection
     *
     * @param collection
     * @return this
     */
    public Schema setCollection(String collection) {
        append(TAG_COLLECTION, collection);
        return this;
    }

    /**
     * The schema origin
     *
     * @return the origin of the schema
     */
    public String getOrigin() {
        return getString(TAG_ORIGIN);
    }

    /**
     * Set or change the schema origin
     *
     * @param origin
     * @return this
     */
    public Schema setOrigin(String origin) {
        append(TAG_ORIGIN, origin);
        return this;
    }

    /**
     * The schema shortName
     *
     * @return the shortName of the schema
     */
    public String getShortName() {
        return getString(TAG_SHORT_NAME);
    }

    /**
     * Set or change the schema shortName
     *
     * @param shortName
     * @return this
     */
    public Schema setShortName(String shortName) {
        append(TAG_SHORT_NAME, shortName);
        return this;
    }

    /**
     * The schema IsObject
     *
     * @return the IsObject of the schema
     */
    public boolean getIsObject() {
        return getBoolean(TAG_IS_OBJECT_NAME, false);
    }

    /**
     * Set or change the schema IsObject
     *
     * @param isObject
     * @return this
     */
    public Schema setIsObject(boolean isObject) {
        append(TAG_IS_OBJECT_NAME, isObject);
        return this;
    }

    /**
     * The schema path
     *
     * @return the path of the schema
     */
    public String getPath() {
        return getString(TAG_PATH);
    }

    /**
     * Set or change the schema path
     *
     * @param path
     * @return this
     */
    public Schema setPath(String path) {
        append(TAG_PATH, path);
        return this;
    }

    /**
     * The schema cardinality
     *
     * @return the cardinality of the schema
     */
    public String getCardinality() {
        return getString(TAG_PATH_CARDINALITY);
    }

    /**
     * Set or change the schema cardinality
     *
     * @param cardinality
     * @return this
     */
    public Schema setCardinality(String cardinality) {
        append(TAG_PATH_CARDINALITY, cardinality);
        return this;
    }

    /**
     * @return creation date of schema elt
     */
    public String getCreationdate() {
        return getString(CREATIONDATE);
    }

    /**
     * @param creationdate to set
     * @return this
     */
    public Schema setCreationdate(String creationdate) {
        append(CREATIONDATE, creationdate);
        return this;
    }

    /**
     * @return last update of profile
     */
    public String getLastupdate() {
        return getString(LAST_UPDATE);
    }

    /**
     * @param lastupdate to set
     * @return this
     */
    public Schema setLastupdate(String lastupdate) {
        append(LAST_UPDATE, lastupdate);
        return this;
    }
}

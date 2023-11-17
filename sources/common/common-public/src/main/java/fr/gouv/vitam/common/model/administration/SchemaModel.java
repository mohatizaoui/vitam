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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


/**
 * POJO java for ontology api response
 */
public class SchemaModel {

    /**
     * Collections Tag
     */
    public static final String TAG_COLLECTIONS = "Collections";

    /**
     * Identifier Tag
     */
    public static final String TAG_FIELD_NAME = "FieldName";

    /**
     * Seda field Tag
     */
    public static final String TAG_SEDAFIELD = "SedaField";

    /**
     * Seda field Tag
     */
    public static final String TAG_APIFIELD = "ApiField";
    public static final String TAG_CATEGORY = "Category";
    public static final String TAG_API_PATH = "ApiPath";
    /**
     * Description Tag
     */
    public static final String TAG_DESCRIPTION = "Description";
    /**
     * Type Tag
     */
    public static final String TAG_TYPE = "Type";

    /**
     * Origin Tag
     */
    public static final String TAG_ORIGIN = "Origin";

    /**
     * ShortName Tag
     */
    public static final String TAG_SHORT_NAME = "ShortName";

    /**
     * SedaVersion tag
     */
    public static final String TAG_SEDA_VERSIONS = "SedaVersions";
    public static final String TAG_PATH = "Path";


    /**
     * StringSize tag for string fields: short, medium, and large
     */
    public static final String TAG_STRING_TYPE_SIZE = "StringSize";

    /**
     * pathCardinality tag
     */
    public static final String TAG_PATH_CARDINALITY = "Cardinality";

    /**
     * The ontology fieldName
     */
    @JsonProperty(TAG_FIELD_NAME)
    private String fieldName;
    /**
     * The ontology seda field
     */
    @JsonProperty(TAG_SEDAFIELD)
    private String sedaField;
    /**
     * The ontology api field
     */

    @JsonProperty(TAG_COLLECTIONS)
    private List<String> collections;

    @JsonProperty(TAG_API_PATH)
    private String apiPath;

    @JsonProperty(TAG_APIFIELD)
    private String apiField;

    @JsonProperty(TAG_CATEGORY)
    private SchemaCategory category;

    /**
     * The ontology description
     */
    @JsonProperty(TAG_DESCRIPTION)
    private String description;

    @JsonProperty(TAG_PATH_CARDINALITY)
    private OntologyCardinality cardinality;
    /**
     * The ontology type
     */
    @JsonProperty(TAG_TYPE)
    private SchemaType type;
    /**
     * The ontology origin
     */
    @JsonProperty(TAG_ORIGIN)
    private OntologyOrigin origin;

    @JsonProperty(TAG_SHORT_NAME)
    private String shortName;

    @JsonProperty(TAG_PATH)
    private String path;

    @JsonProperty(TAG_SEDA_VERSIONS)
    private List<String> sedaVersions;

    @JsonProperty(TAG_STRING_TYPE_SIZE)
    private OntologyStringTypeSize stringSize;

    /**
     * Constructor without fields use for jackson
     */
    public SchemaModel() {
        super();
    }


    public String getFieldName() {
        return fieldName;
    }

    public SchemaModel setFieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
    }

    public String getSedaField() {
        return sedaField;
    }

    public SchemaModel setSedaField(String sedaField) {
        this.sedaField = sedaField;
        return this;
    }

    public String getApiField() {
        return apiField;
    }

    public SchemaModel setApiField(String apiField) {
        this.apiField = apiField;
        return this;
    }

    public SchemaCategory getCategory() {
        return category;
    }

    public SchemaModel setCategory(SchemaCategory category) {
        this.category = category;
        return this;
    }

    public String getApiPath() {
        return apiPath;
    }

    public SchemaModel setApiPath(String apiPath) {
        this.apiPath = apiPath;
        return this;
    }


    public String getDescription() {
        return description;
    }

    public SchemaModel setDescription(String description) {
        this.description = description;
        return this;
    }

    public OntologyOrigin getOrigin() {
        return origin;
    }

    public SchemaModel setOrigin(OntologyOrigin origin) {
        this.origin = origin;
        return this;
    }

    public SchemaType getType() {
        return type;
    }

    public SchemaModel setType(SchemaType type) {
        this.type = type;
        return this;
    }

    public String getShortName() {
        return shortName;
    }

    public SchemaModel setShortName(String shortName) {
        this.shortName = shortName;
        return this;
    }

    public List<String> getCollections() {
        return collections;
    }

    public SchemaModel setCollections(List<String> collections) {
        this.collections = collections;
        return this;
    }

    public OntologyCardinality getCardinality() {
        return cardinality;
    }

    public void setCardinality(OntologyCardinality cardinality) {
        this.cardinality = cardinality;
    }

    public OntologyStringTypeSize getStringSize() {
        return stringSize;
    }

    public void setStringSize(OntologyStringTypeSize stringSize) {
        this.stringSize = stringSize;
    }


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getSedaVersions() {
        return sedaVersions;
    }

    public void setSedaVersions(List<String> sedaVersions) {
        this.sedaVersions = sedaVersions;
    }
}



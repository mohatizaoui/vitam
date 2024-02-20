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
package fr.gouv.vitam.common.model.unit;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;


public class GpsType {

    @JsonProperty("GpsVersionID")
    protected String gpsVersionID;
    @JsonProperty("GpsAltitude")
    protected BigInteger gpsAltitude;
    @JsonProperty("GpsAltitudeRef")
    protected String gpsAltitudeRef;
    @JsonProperty("GpsLatitude")
    protected String gpsLatitude;
    @JsonProperty("GpsLatitudeRef")
    protected String gpsLatitudeRef;
    @JsonProperty("GpsLongitude")
    protected String gpsLongitude;
    @JsonProperty("GpsLongitudeRef")
    protected String gpsLongitudeRef;
    @JsonProperty("GpsDateStamp")
    protected String gpsDateStamp;

    public GpsType() {
        // Empty constructor for deserialization
    }

    public String getGpsVersionID() {
        return gpsVersionID;
    }

    public GpsType setGpsVersionID(String gpsVersionID) {
        this.gpsVersionID = gpsVersionID;
        return this;
    }

    public BigInteger getGpsAltitude() {
        return gpsAltitude;
    }

    public GpsType setGpsAltitude(BigInteger gpsAltitude) {
        this.gpsAltitude = gpsAltitude;
        return this;
    }

    public String getGpsAltitudeRef() {
        return gpsAltitudeRef;
    }

    public GpsType setGpsAltitudeRef(String gpsAltitudeRef) {
        this.gpsAltitudeRef = gpsAltitudeRef;
        return this;
    }

    public String getGpsLatitude() {
        return gpsLatitude;
    }

    public GpsType setGpsLatitude(String gpsLatitude) {
        this.gpsLatitude = gpsLatitude;
        return this;
    }

    public String getGpsLatitudeRef() {
        return gpsLatitudeRef;
    }

    public GpsType setGpsLatitudeRef(String gpsLatitudeRef) {
        this.gpsLatitudeRef = gpsLatitudeRef;
        return this;
    }

    public String getGpsLongitude() {
        return gpsLongitude;
    }

    public GpsType setGpsLongitude(String gpsLongitude) {
        this.gpsLongitude = gpsLongitude;
        return this;
    }

    public String getGpsLongitudeRef() {
        return gpsLongitudeRef;
    }

    public GpsType setGpsLongitudeRef(String gpsLongitudeRef) {
        this.gpsLongitudeRef = gpsLongitudeRef;
        return this;
    }

    public String getGpsDateStamp() {
        return gpsDateStamp;
    }

    public GpsType setGpsDateStamp(String gpsDateStamp) {
        this.gpsDateStamp = gpsDateStamp;
        return this;
    }
}

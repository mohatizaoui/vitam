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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ArchiveUnitProfileSedaVersion {
    VERSION_2_1("v2.1"),
    VERSION_2_2("v2.2"),
    VERSION_2_3("v2.3");

    private static final Map<String, ArchiveUnitProfileSedaVersion> VERSION_MAP = Stream.of(
        ArchiveUnitProfileSedaVersion.values()
    ).collect(Collectors.toMap(ArchiveUnitProfileSedaVersion::getVersion, Function.identity()));

    private final String version;

    ArchiveUnitProfileSedaVersion(String version) {
        this.version = version;
    }

    @JsonValue
    public String getVersion() {
        return version;
    }

    /**
     * @param version
     * @return the associated ProfileSedaVersion according to parameter
     * @throws IllegalStateException when version not found
     */
    @JsonCreator
    public static ArchiveUnitProfileSedaVersion forVersion(String version) throws IllegalArgumentException {
        if (version == null) {
            return ArchiveUnitProfileSedaVersion.VERSION_2_1;
        }
        return Optional.ofNullable(VERSION_MAP.get(version)).orElseThrow(
            () -> new IllegalArgumentException("Cannot find ArchiveUnitProfileSedaVersion " + version)
        );
    }
}

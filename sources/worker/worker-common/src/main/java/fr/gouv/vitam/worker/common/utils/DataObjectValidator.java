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
package fr.gouv.vitam.worker.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataObjectValidator {

    public static final Pattern DATA_OBJECT_VERSION_PATTERN = Pattern.compile("^[a-zA-Z]+(_[1-9]\\d{0,6})?$");
    public static final String DATA_OBJECT_VERSION_NOT_DEFINED_MESSAGE = "Data object version is not defined";
    public static final String DATA_OBJECT_VERSION_NOT_ALLOWED_MESSAGE = "Data object version pattern '%s' is not allowed";

    public static void validateVersionDataObject(final String versionDataObject) throws InvalidDataObjectException {
        checkDataObjectVersionPresence(versionDataObject);
        validateDataObjectVersionFormat(versionDataObject);
    }

    private static void checkDataObjectVersionPresence(final String dataObjectVersion)
        throws InvalidDataObjectException {
        if (StringUtils.isBlank(dataObjectVersion)) {
            throw new InvalidDataObjectException(DATA_OBJECT_VERSION_NOT_DEFINED_MESSAGE);
        }
    }

    private static void validateDataObjectVersionFormat(final String dataObjectVersion)
        throws InvalidDataObjectException {
        final Matcher matcher = DATA_OBJECT_VERSION_PATTERN.matcher(dataObjectVersion);
        if (!matcher.matches()) {
            final String message = String.format(DATA_OBJECT_VERSION_NOT_ALLOWED_MESSAGE, dataObjectVersion);
            throw new InvalidDataObjectException(message);
        }
    }
}
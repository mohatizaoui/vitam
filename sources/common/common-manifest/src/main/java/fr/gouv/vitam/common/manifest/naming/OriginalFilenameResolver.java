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

package fr.gouv.vitam.common.manifest.naming;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

import static fr.gouv.vitam.common.manifest.naming.ExtensionHelper.getExtension;

public class OriginalFilenameResolver implements FilenameResolver {

    private static final String CONTENT_PREFIX = "Content/";
    private static final String PATH_SEPARATOR = "/";
    private static final int MAX_FILE_NAME_SIZE_LIMIT = 150;
    private final Set<String> resolvedPaths = new HashSet<>();

    @Override
    public String resolve(String basePath, String objectId, String uri, String filename) {
        String fullPath = resolveFullPath(basePath, objectId, uri, filename);

        if (!resolvedPaths.add(fullPath)) {
            throw new IllegalStateException("Duplicate path '" + fullPath + "' for object with ID: " + objectId);
        }
        return fullPath;
    }

    private String resolveFullPath(String basePath, String objectId, String uri, String filename) {
        if (StringUtils.isEmpty(filename)) {
            // Fallback to guid
            return GuidFilenameResolver.INSTANCE.resolve(basePath, objectId, uri, filename);
        }

        String normalizedFilename = FileNameCleaner.cleanFileName(filename);

        String fullPath = getFullPath(basePath, normalizedFilename);
        if (!resolvedPaths.contains(fullPath) && normalizedFilename.length() < MAX_FILE_NAME_SIZE_LIMIT) {
            return fullPath;
        }

        // If duplicate path OR path is too long ==> truncate filename & suffix with guid
        return getFullPath(basePath, truncateAndAddSuffix(normalizedFilename, objectId));
    }

    private static String truncateAndAddSuffix(String filename, String objectId) {
        int maxLength = Math.min(MAX_FILE_NAME_SIZE_LIMIT, filename.length() + 1 + objectId.length());

        String extension = getExtension(filename);
        if (StringUtils.isNotEmpty(extension)) {
            return (
                filename.substring(0, maxLength - extension.length() - 1 - objectId.length() - 1) +
                "_" +
                objectId +
                "." +
                extension
            );
        } else {
            return filename.substring(0, maxLength - objectId.length() - 1) + "_" + objectId;
        }
    }

    private static String getFullPath(String basePath, String objectPath) {
        if (basePath.isEmpty()) {
            return CONTENT_PREFIX + objectPath;
        }
        return CONTENT_PREFIX + basePath + PATH_SEPARATOR + objectPath;
    }
}

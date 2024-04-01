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
package fr.gouv.vitam.collect.internal.core.helpers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitam.collect.common.exception.CollectInternalException;
import fr.gouv.vitam.collect.common.exception.CollectInternalInvalidRequestException;
import fr.gouv.vitam.collect.common.exception.CollectInternalServerSideException;
import fr.gouv.vitam.collect.internal.core.common.CollectJsonMetadataLine;
import fr.gouv.vitam.collect.internal.core.common.CollectJsonMetadataSelector;
import fr.gouv.vitam.common.collection.CloseableIterator;
import fr.gouv.vitam.common.database.builder.query.VitamFieldsHelper;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.unit.ArchiveUnitModel;
import fr.gouv.vitam.common.model.unit.RuleCategoryModel;
import fr.gouv.vitam.common.model.unit.RuleModel;
import fr.gouv.vitam.common.security.SanityChecker;
import fr.gouv.vitam.worker.core.distribution.JsonLineGenericIterator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;

import static fr.gouv.vitam.common.model.unit.RuleModel.END_DATE;

public class JsonlMetadataFileValidator {

    private static final Set<String> ALLOWED_RESERVED_FIELD_NAMES =
        Set.of(VitamFieldsHelper.management(), VitamFieldsHelper.history());

    private static final TypeReference<ArchiveUnitModel>
        ARCHIVE_UNIT_MODEL_TYPE_REFERENCE = new TypeReference<>() {
    };

    public void validate(File jsonlMetadataFile)
        throws CollectInternalException {

        doSanityChecks(jsonlMetadataFile);

        try (
            InputStream inputStream = new FileInputStream(jsonlMetadataFile);
            CloseableIterator<CollectJsonMetadataLine> iterator =
                new JsonLineGenericIterator<>(inputStream, CollectJsonMetadataLine.TYPE_REFERENCE)) {

            for (int lineIndex = 0; iterator.hasNext(); lineIndex++) {
                CollectJsonMetadataLine entry = iterator.next();

                // Validate line
                validateMetadataIdentificationInformation(entry, lineIndex);
                validateUnitContent(entry.getUnitContent(), lineIndex);

                // FIXME : Add support for key/value selectors
            }
        } catch (IOException e) {
            throw new CollectInternalServerSideException(
                "An internal error occurred during jsonl metadata file processing", e);
        }

    }

    private static void doSanityChecks(File jsonlMetadataFile)
        throws CollectInternalInvalidRequestException, CollectInternalServerSideException {
        if (jsonlMetadataFile.length() == 0) {
            throw new CollectInternalInvalidRequestException("Empty jsonl file");
        }
        try {
            SanityChecker.checkJsonLines(jsonlMetadataFile);
        } catch (IOException e) {
            throw new CollectInternalServerSideException(
                "An internal error occurred during jsonl metadata file processing", e);
        } catch (IllegalArgumentException | InvalidParseOperationException e) {
            throw new CollectInternalInvalidRequestException(
                "Cannot validate json-lines request: " + e.getLocalizedMessage(), e);
        }
    }

    private void validateMetadataIdentificationInformation(CollectJsonMetadataLine entry, int lineIndex)
        throws CollectInternalInvalidRequestException {

        if (entry.getFile() == null && entry.getSelector() == null) {
            throw new CollectInternalInvalidRequestException("Invalid entry at index: " + lineIndex +
                ". Missing metadata identification information.");
        }

        if (entry.getFile() != null && entry.getSelector() != null) {
            throw new CollectInternalInvalidRequestException("Invalid entry at index: " + lineIndex +
                ". Fields '" + CollectJsonMetadataLine.FILE_FIELD + "' and '" + CollectJsonMetadataLine.SELECTOR_FIELD +
                "' are mutually exclusive.");
        }

        if (entry.getFile() != null) {
            validateFileIdentifier(entry.getFile(), lineIndex);
        }

        if (entry.getSelector() != null) {
            validateSelector(entry.getSelector(), lineIndex);
        }
    }

    private void validateFileIdentifier(String fileValue, int lineIndex) throws CollectInternalInvalidRequestException {
        if (StringUtils.isBlank(fileValue)) {
            throw new CollectInternalInvalidRequestException("Invalid entry at index: " + lineIndex +
                ". Empty unit file path '" + fileValue + "'");
        }
        String path = FilenameUtils.normalize(fileValue);
        if (!FilenameUtils.equals(fileValue, path)) {
            throw new CollectInternalInvalidRequestException("Invalid entry at index: " + lineIndex +
                ". Illegal unit file path '" + fileValue + "'");
        }
    }


    private void validateSelector(CollectJsonMetadataSelector selectorValue, int lineIndex)
        throws CollectInternalInvalidRequestException {
        if (selectorValue.getEntries().isEmpty()) {
            throw new CollectInternalInvalidRequestException("Invalid entry at index: " + lineIndex +
                ". Empty selectors");
        }
        // FIXME : Implement validation criteria :
        //  - fields name (No internal fields "_", "$", spacing, unprintable...)
        //  - check ontology type :
        //    - Exact match types are OK (keyword, long, double, bool, date).
        //    - Analyzed texts should not queried (otherwise, we won't be able to manage direct inserts, we'll be stuck forever using insert + update)
        throw new CollectInternalInvalidRequestException("Invalid entry at index: " + lineIndex +
            ". Custom selector key are not supported yet");
    }

    private void validateUnitContent(ObjectNode unitContent, int lineIndex)
        throws CollectInternalInvalidRequestException {
        checkNonEmptyUnit(unitContent, lineIndex);
        validateReservedUnitFieldNames(unitContent, lineIndex);
        validateUnitFormat(unitContent, lineIndex);
    }

    private void checkNonEmptyUnit(ObjectNode unitContent, int lineIndex)
        throws CollectInternalInvalidRequestException {
        if (unitContent == null || unitContent.isEmpty()) {
            throw new CollectInternalInvalidRequestException("Invalid unit metadata at index: " + lineIndex +
                ". Empty metadata content");
        }
    }

    private static void validateReservedUnitFieldNames(ObjectNode unitContent, int lineIndex)
        throws CollectInternalInvalidRequestException {
        Iterator<String> it = unitContent.fieldNames();
        while (it.hasNext()) {
            String fieldName = it.next();
            if (fieldName.startsWith("$") || fieldName.startsWith("_")) {
                throw new CollectInternalInvalidRequestException("Invalid unit metadata at index: " + lineIndex +
                    ". Illegal field name '" + fieldName + "'");
            }
            if (fieldName.startsWith("#") && !ALLOWED_RESERVED_FIELD_NAMES.contains(fieldName)) {
                throw new CollectInternalInvalidRequestException("Invalid unit metadata at index: " + lineIndex +
                    ". Forbidden field name '" + fieldName + "'");
            }
            if (fieldName.contains(".")) {
                throw new CollectInternalInvalidRequestException("Invalid unit metadata at index: " + lineIndex +
                    ". Field name must be root-level field: '" + fieldName + "'");
            }
        }
    }

    private static void validateUnitFormat(ObjectNode unitContent, int lineIndex)
        throws CollectInternalInvalidRequestException {

        ArchiveUnitModel archiveUnitModel;
        try {
            // Use strict deserializer to validate unit content structure & format
            archiveUnitModel = JsonHandler.getFromStrictJsonNode(unitContent, ARCHIVE_UNIT_MODEL_TYPE_REFERENCE);
        } catch (InvalidParseOperationException e) {
            throw new CollectInternalInvalidRequestException("Invalid unit metadata at index: " + lineIndex +
                ". Unit format validation failed: " + e.getLocalizedMessage(), e);
        }

        validateUnitRulesEndDates(archiveUnitModel, lineIndex);
    }

    private static void validateUnitRulesEndDates(ArchiveUnitModel archiveUnitModel, int lineIndex)
        throws CollectInternalInvalidRequestException {
        if (archiveUnitModel.getManagement() != null) {
            validateRulesEndDates(archiveUnitModel.getManagement().getDissemination(), "Dissemination", lineIndex);
            validateRulesEndDates(archiveUnitModel.getManagement().getStorage(), "Storage", lineIndex);
            validateRulesEndDates(archiveUnitModel.getManagement().getAppraisal(), "Appraisal", lineIndex);
            validateRulesEndDates(archiveUnitModel.getManagement().getAccess(), "Access", lineIndex);
            validateRulesEndDates(archiveUnitModel.getManagement().getReuse(), "Reuse", lineIndex);
            validateRulesEndDates(archiveUnitModel.getManagement().getClassification(), "Classification", lineIndex);
            validateRulesEndDates(archiveUnitModel.getManagement().getHold(), "Hold", lineIndex);
        }
    }

    private static void validateRulesEndDates(RuleCategoryModel ruleCategoryModel, String ruleCategory, int lineIndex)
        throws CollectInternalInvalidRequestException {
        if (ruleCategoryModel == null || CollectionUtils.isEmpty(ruleCategoryModel.getRules())) {
            return;
        }
        for (RuleModel rule : ruleCategoryModel.getRules()) {
            if (rule.getEndDate() != null) {
                throw new CollectInternalInvalidRequestException("Invalid unit metadata at index: " + lineIndex +
                    ". Unit " + ruleCategory + " Rules cannot contain '" + END_DATE + "' field.");
            }
        }
    }
}

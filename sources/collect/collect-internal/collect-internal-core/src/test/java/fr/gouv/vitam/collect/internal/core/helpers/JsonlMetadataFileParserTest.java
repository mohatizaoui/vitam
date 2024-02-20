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

import fr.gouv.vitam.collect.common.exception.CollectInternalInvalidRequestException;
import fr.gouv.vitam.common.PropertiesUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JsonlMetadataFileParserTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testParseValidJsonlMetadataFile() throws Exception {

        // Given
        JsonlMetadataFileParser parser = new JsonlMetadataFileParser();
        File jsonlMetadataFile = PropertiesUtils.getResourceFile("update/metadata.jsonl");

        // When
        File transformedFile = temporaryFolder.newFile("transformed_metadata.jsonl");
        parser.process(jsonlMetadataFile, transformedFile);

        // Then
        File expectedTransformedMetadataFile =
            PropertiesUtils.getResourceFile("update/expected_transformed_metadata.jsonl");
        assertThat(transformedFile).hasSameContentAs(expectedTransformedMetadataFile);
    }

    @Test
    public void testParseInvalidJsonlMetadataFile_InvalidFieldNames() throws Exception {

        assertInvalid("update/metadata_invalid_field_name_dot.jsonl",
            "Invalid unit metadata at index: 0. Field name must be root-level field: 'Description.Level'");

        assertInvalid("update/metadata_invalid_field_name_dash.jsonl",
            "Invalid unit metadata at index: 0. Forbidden field name '#id'");

        assertInvalid("update/metadata_invalid_field_name_underscore.jsonl",
            "Invalid unit metadata at index: 0. Illegal field name '_id'");

        assertInvalid("update/metadata_invalid_field_name_dollar.jsonl",
            "Invalid unit metadata at index: 0. Illegal field name '$myField'");
    }

    @Test
    public void testParseInvalidJsonlMetadataFile_EmptyRequest() throws Exception {

        assertInvalid("update/metadata_invalid_empty.jsonl",
            "Empty jsonl file");

        assertInvalid("update/metadata_invalid_empty_line.jsonl",
            "Cannot validate json-lines request: Json is not valid from Sanitize check");
    }

    @Test
    public void testParseInvalidJsonlMetadataFile_EmptyUnitContent() throws Exception {

        assertInvalid("update/metadata_null_unit_content.jsonl",
            "Invalid unit metadata at index: 1. Empty metadata content");

        assertInvalid("update/metadata_empty_unit_content.jsonl",
            "Invalid unit metadata at index: 1. Empty metadata content");

        assertInvalid("update/metadata_missing_unit_content.jsonl",
            "Invalid unit metadata at index: 1. Empty metadata content");
    }

    @Test
    public void testParseInvalidJsonlMetadataFile_SanityCheckKo() throws Exception {

        assertInvalid("update/metadata_invalid_sanity_check_ko.jsonl",
            "Cannot validate json-lines request: Json is not valid from Sanitize check");
    }

    @Test
    public void testParseInvalidJsonlMetadataFile_UnitFormatKo() throws Exception {

        assertInvalid("update/metadata_invalid_unit_format.jsonl",
            "Cannot deserialize value of type `fr.gouv.vitam.common.model.unit.LevelType`");
    }

    @Test
    public void testParseInvalidJsonlMetadataFile_UnitWithManagementRuleEndDate() throws Exception {

        assertInvalid("update/metadata_invalid_unit_management_rule_end_date.jsonl",
            "Invalid unit metadata at index: 0. Unit Appraisal Rules cannot contain 'EndDate' field.");
    }

    @Test
    public void testParseInvalidJsonlMetadataFile_MissingUnitIdentificationInformation() throws Exception {

        assertInvalid("update/metadata_missing_unit_identification_information.jsonl",
            "Invalid entry at index: 1. Missing metadata identification information.");
    }

    @Test
    public void testParseInvalidJsonlMetadataFile_InvalidFile() throws Exception {

        assertInvalid("update/metadata_empty_file.jsonl",
            "Invalid entry at index: 0. Empty unit file path '    '");

        assertInvalid("update/metadata_illegal_file.jsonl",
            "Invalid entry at index: 0. Illegal unit file path 'toto/../tata'");
    }

    @Test
    public void testParseInvalidJsonlMetadataFile_UnsupportedUnitSelector() throws Exception {

        // FIXME : Remove test when custom selectors are implemented
        assertInvalid("update/metadata_unsupported_unit_selector.jsonl",
            "Invalid entry at index: 1. Custom selector key are not supported yet");
    }

    private void assertInvalid(String resourcesFile, String expectedMessage) throws Exception {
        JsonlMetadataFileParser parser = new JsonlMetadataFileParser();
        File jsonlMetadataFile = PropertiesUtils.getResourceFile(resourcesFile);
        File transformedFile = temporaryFolder.newFile("transformed_metadata.jsonl");
        assertThatThrownBy(() -> parser.process(jsonlMetadataFile, transformedFile))
            .isInstanceOf(CollectInternalInvalidRequestException.class)
            .hasMessageContaining(expectedMessage);
        Files.delete(transformedFile.toPath());
    }
}
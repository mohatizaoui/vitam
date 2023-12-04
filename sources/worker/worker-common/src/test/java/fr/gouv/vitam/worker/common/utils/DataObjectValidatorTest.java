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

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import fr.gouv.vitam.common.model.administration.DataObjectVersionType;
import fr.gouv.vitam.worker.common.utils.DataObjectValidator;
import fr.gouv.vitam.worker.common.utils.InvalidDataObjectException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static fr.gouv.vitam.common.SedaConstants.TAG_DO_VERSION;

public class DataObjectValidatorTest {

    @Test
    public void shouldValidateDataObjectVersionWithDefaultVersion() throws InvalidDataObjectException {
        validateDataObjectVersions("BinaryMaster");
    }

    @Test
    public void shouldValidateDataObjectVersionWithStrictlyPositiveSuffix() throws InvalidDataObjectException {
        validateDataObjectVersions(
            "BinaryMaster_1",
            "BinaryMaster_12",
            "BinaryMaster_4",
            "PhysicalMaster_1",
            "BinaryMaster_" + 999999
        );
    }

    @Test
    public void shouldThrowDataObjectExceptionWhenSuffixIsInvalid() {
        assertDataObjectExceptionForInvalidVersions(
            "BinaryMaster_Toto",
            "BinaryMaster_+1",
            "BinaryMaster_-1",
            "BinaryMaster_0.0001",
            "BinaryMaster_" + 1000000
        );
    }

    @Test
    public void shouldThrowDataObjectExceptionWhenSuffixIsNotStrictlyPositive() {
        assertDataObjectExceptionForInvalidVersions(
            "BinaryMaster_0",
            "BinaryMaster_00",
            "BinaryMaster_-1"
        );
    }

    @Test
    public void shouldThrowDataObjectExceptionWhenSuffixIsNotOptimized() {
        assertDataObjectExceptionForInvalidVersions(
            "BinaryMaster_0000001",
            "BinaryMaster_00001000",
            "BinaryMaster_00100800"
        );
    }

    private void validateDataObjectVersions(String... versions) throws InvalidDataObjectException {
        for (String version : versions) {
            DataObjectValidator.validateVersionDataObject(version);
        }
    }

    private void assertDataObjectExceptionForInvalidVersions(String... invalidVersions) {
        Arrays.stream(invalidVersions)
            .map(this::generateAllQualifierDataObjects)
            .flatMap(List::stream)
            .forEach(dataObjectVersion -> {
                Assert.assertThrows(InvalidDataObjectException.class,
                    () -> DataObjectValidator.validateVersionDataObject(dataObjectVersion));
            });
    }

    private ObjectNode dataObject(final String dataObjectVersion) {
        return new ObjectNode(JsonNodeFactory.instance).set(TAG_DO_VERSION, new TextNode(dataObjectVersion));
    }

    private List<String> generateAllQualifierDataObjects(final String version) {
        return Arrays.stream(DataObjectVersionType.values())
            .map(DataObjectVersionType::getName)
            .map(qualifier -> String.format("%s_%s", qualifier, version))
            .collect(Collectors.toList());
    }
}
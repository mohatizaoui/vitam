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
package fr.gouv.vitam.metadata.core.reconstruction.domain.extractor;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.metadata.core.reconstruction.model.PurgedPersistentIdentifier;
import fr.gouv.vitam.metadata.core.reconstruction.model.ReconstructionOperation;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class VersionPurgedPersistentIdentifierExtractorTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private VersionPurgedPersistentIdentifierExtractor versionPurgedPersistentIdentifierExtractor;

    @Before
    public void setup() throws Exception {
        versionPurgedPersistentIdentifierExtractor = new VersionPurgedPersistentIdentifierExtractor();
    }

    @Test
    public void testExtractAndSavePurgedPersistentIdentifier() throws Exception {
        ReconstructionOperation operation = createSampleOperation();

        final JsonNode reportLineAsJsonNode = JsonHandler.getFromFile(
            PropertiesUtils.getResourceFile("deleting_versions_expectedReport_line.json")
        );

        final List<PurgedPersistentIdentifier> purgedPersistentIdentifiers =
            versionPurgedPersistentIdentifierExtractor.extractPurgedPersistentIdentifier(
                reportLineAsJsonNode,
                operation
            );

        assertThat(purgedPersistentIdentifiers).isNotEmpty();

        assertThat(purgedPersistentIdentifiers.get(0).getTenant()).isEqualTo(0);
        assertThat(purgedPersistentIdentifiers.get(0).getType()).isEqualTo("Object");
        assertThat(purgedPersistentIdentifiers.get(0).getPersistentIdentifier().size()).isEqualTo(2);
    }

    private ReconstructionOperation createSampleOperation() {
        return ReconstructionOperation.builder()
            .setTenant(0)
            .setId("aeeaaaaaace6eg5mabvsaamkdsdfzeaaaaaq")
            .setType("DELETE_GOT_VERSIONS")
            .build();
    }
}

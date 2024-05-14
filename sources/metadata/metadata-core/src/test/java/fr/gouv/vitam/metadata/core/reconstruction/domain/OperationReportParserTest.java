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
package fr.gouv.vitam.metadata.core.reconstruction.domain;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.metadata.core.config.MetaDataConfiguration;
import fr.gouv.vitam.metadata.core.reconstruction.domain.extractor.PurgedPersistentIdentifierExtractor;
import fr.gouv.vitam.metadata.core.reconstruction.domain.extractor.PurgedPersistentIdentifierExtractorFactory;
import fr.gouv.vitam.metadata.core.reconstruction.exception.ReconstructionException;
import fr.gouv.vitam.metadata.core.reconstruction.model.PurgedPersistentIdentifier;
import fr.gouv.vitam.metadata.core.reconstruction.model.ReconstructionOperation;
import fr.gouv.vitam.metadata.core.reconstruction.model.ReportLine.ReportLineType;
import fr.gouv.vitam.metadata.core.reconstruction.repository.OperationReportRepository;
import fr.gouv.vitam.metadata.core.reconstruction.repository.PersistentIdentifierRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class OperationReportParserTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private OperationReportParser operationReportParser;

    @Mock
    private OperationReportRepository operationReportRepository;

    @Mock
    private MetaDataConfiguration metaDataConfiguration;

    @Mock
    private PersistentIdentifierRepository persistentIdentifierRepository;

    @Mock
    private PurgedPersistentIdentifierExtractorFactory purgedPersistentIdentifierExtractorFactory;

    @Mock
    private PurgedPersistentIdentifierExtractor purgedPersistentIdentifierExtractor;

    @Before
    public void setup() {
        operationReportParser = new OperationReportParser(
            operationReportRepository,
            metaDataConfiguration,
            persistentIdentifierRepository,
            purgedPersistentIdentifierExtractorFactory
        );
    }

    @Test
    public void processReportFromOperation2_Successful() throws Exception {
        ReconstructionOperation operation = new ReconstructionOperation.Builder()
            .setId("operationId")
            .setTenant(0)
            .setType("ELIMINATION_ACTION")
            .setLastPersistedDate("2017-10-31T15:11:18")
            .build();

        final InputStream inputStream = PropertiesUtils.getResourceAsStream("deleting_versions_expectedReport.jsonl");

        when(operationReportRepository.retrieveJsonReportForOperation("operationId")).thenReturn(inputStream);
        when(purgedPersistentIdentifierExtractorFactory.instance(any(ReportLineType.class))).thenReturn(
            purgedPersistentIdentifierExtractor
        );

        when(
            purgedPersistentIdentifierExtractor.extractPurgedPersistentIdentifier(any(JsonNode.class), eq(operation))
        ).thenReturn(
            Collections.singletonList(
                new PurgedPersistentIdentifier.Builder().setId("purgedId").setTenant(0).setType("sampleType").build()
            )
        );

        LocalDateTime result = operationReportParser.processReportFromOperation(operation);

        assertThat(result).isNotNull();
    }

    @Test
    public void processReportFromOperation_Successful() throws Exception {
        ReconstructionOperation operation = new ReconstructionOperation.Builder()
            .setId("operationId")
            .setTenant(0)
            .setType("ELIMINATION_ACTION")
            .setLastPersistedDate("2017-10-31T15:11:18")
            .build();

        final InputStream inputStream = PropertiesUtils.getResourceAsStream("elimination_expectedReport.jsonl");

        when(operationReportRepository.retrieveJsonReportForOperation("operationId")).thenReturn(inputStream);
        when(purgedPersistentIdentifierExtractorFactory.instance(any(ReportLineType.class))).thenReturn(
            purgedPersistentIdentifierExtractor
        );

        when(
            purgedPersistentIdentifierExtractor.extractPurgedPersistentIdentifier(any(JsonNode.class), eq(operation))
        ).thenReturn(
            Collections.singletonList(
                new PurgedPersistentIdentifier.Builder().setId("purgedId").setTenant(0).setType("sampleType").build()
            )
        );

        LocalDateTime result = operationReportParser.processReportFromOperation(operation);

        assertThat(result).isNotNull();
    }

    @Test
    public void processReportFromOperation_Exception() throws Exception {
        ReconstructionOperation operation = new ReconstructionOperation.Builder()
            .setId("operationId")
            .setTenant(0)
            .setType("sampleType")
            .setLastPersistedDate("2017-10-31T15:11:18")
            .build();

        when(operationReportRepository.retrieveJsonReportForOperation("operationId")).thenThrow(
            new ReconstructionException("Simulated exception")
        );

        assertThrows(ReconstructionException.class, () -> operationReportParser.processReportFromOperation(operation));
    }

    @Test
    public void processDeletingVersionsReportFromOperation_Successful() throws Exception {
        ReconstructionOperation operation = new ReconstructionOperation.Builder()
            .setId("operationId")
            .setTenant(0)
            .setType("DELETE_GOT_VERSIONS")
            .setLastPersistedDate("2017-10-31T15:11:18")
            .build();

        final InputStream inputStream = PropertiesUtils.getResourceAsStream("deleting_versions_expectedReport.jsonl");

        when(operationReportRepository.retrieveJsonReportForOperation("operationId")).thenReturn(inputStream);
        when(purgedPersistentIdentifierExtractorFactory.instance(any(ReportLineType.class))).thenReturn(
            purgedPersistentIdentifierExtractor
        );

        when(
            purgedPersistentIdentifierExtractor.extractPurgedPersistentIdentifier(any(JsonNode.class), eq(operation))
        ).thenReturn(
            Collections.singletonList(
                new PurgedPersistentIdentifier.Builder().setId("purgedId").setTenant(0).setType("sampleType").build()
            )
        );

        LocalDateTime result = operationReportParser.processReportFromOperation(operation);

        assertThat(result).isNotNull();
    }
}

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
import fr.gouv.vitam.common.LocalDateUtil;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.metadata.api.exception.MetaDataExecutionException;
import fr.gouv.vitam.metadata.core.config.MetaDataConfiguration;
import fr.gouv.vitam.metadata.core.reconstruction.domain.extractor.PurgedPersistentIdentifierExtractorFactory;
import fr.gouv.vitam.metadata.core.reconstruction.exception.ReconstructionException;
import fr.gouv.vitam.metadata.core.reconstruction.model.PurgedPersistentIdentifier;
import fr.gouv.vitam.metadata.core.reconstruction.model.ReconstructionOperation;
import fr.gouv.vitam.metadata.core.reconstruction.model.ReportLine;
import fr.gouv.vitam.metadata.core.reconstruction.repository.OperationReportRepository;
import fr.gouv.vitam.metadata.core.reconstruction.repository.PersistentIdentifierRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static fr.gouv.vitam.metadata.core.reconstruction.model.ReportLine.ReportLineType.DELETED_GOT_VERSION;
import static fr.gouv.vitam.metadata.core.reconstruction.model.ReportLine.ReportLineType.DELETED_OBJECT_GROUP;
import static fr.gouv.vitam.metadata.core.reconstruction.model.ReportLine.ReportLineType.DELETED_UNIT;
import static fr.gouv.vitam.metadata.core.reconstruction.model.ReportLine.ReportLineType.TRANSFERRED_OBJECT_GROUP;
import static fr.gouv.vitam.metadata.core.reconstruction.model.ReportLine.ReportLineType.TRANSFERRED_UNIT;
import static fr.gouv.vitam.metadata.core.reconstruction.model.ReportLine.ReportLineType.UNDEFINED;

public class OperationReportParser {

    public static final String UNIT = "Unit";
    public static final String OBJECT_GROUP = "ObjectGroup";
    public static final String ELIMINATION_ACTION = "ELIMINATION_ACTION";
    public static final String DELETE_GOT_VERSIONS = "DELETE_GOT_VERSIONS";
    public static final String DETAIL_TYPE = "detailType";
    public static final String PARAMS = "params";
    public static final String TYPE = "type";
    public static final String TRANSFER_REPLY = "TRANSFER_REPLY";

    final private OperationReportRepository operationReportRepository;
    final private PurgedPersistentIdentifierExtractorFactory purgedPersistentIdentifierExtractorFactory;
    private final MetaDataConfiguration metaDataConfiguration;
    private final PersistentIdentifierRepository persistentIdentifierRepository;

    public OperationReportParser(OperationReportRepository operationReportRepository,
        MetaDataConfiguration metaDataConfiguration,
        PersistentIdentifierRepository persistentIdentifierRepository,
        PurgedPersistentIdentifierExtractorFactory purgedPersistentIdentifierExtractorFactory) {
        this.operationReportRepository = operationReportRepository;
        this.purgedPersistentIdentifierExtractorFactory = purgedPersistentIdentifierExtractorFactory;
        this.metaDataConfiguration = metaDataConfiguration;
        this.persistentIdentifierRepository = persistentIdentifierRepository;
    }

    public LocalDateTime processReportFromOperation(ReconstructionOperation operation) throws ReconstructionException {
        final PurgedPersistentIdentifierBulkInserter purgedPersistentIdentifierBulkInserter = new PurgedPersistentIdentifierBulkInserter(metaDataConfiguration, persistentIdentifierRepository);

        try (InputStream is = operationReportRepository.retrieveJsonReportForOperation(operation.getId());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            Iterator<String> lines = bufferedReader.lines().iterator();

            while (lines.hasNext()) {
                final JsonNode element = JsonHandler.getFromString(lines.next());
                processJsonElement(element, operation, purgedPersistentIdentifierBulkInserter);
            }

            purgedPersistentIdentifierBulkInserter.flush();

            return Optional.ofNullable(operation.getLastPersistedDate())
                .filter(date -> !date.isBlank())
                .map(LocalDateUtil::parseMongoFormattedDate)
                .orElse(LocalDateUtil.now());
        } catch (ReconstructionException | IOException | InvalidParseOperationException | MetaDataExecutionException | DateTimeParseException e) {
            throw new ReconstructionException("Could not process report operation : " + operation.getId(), e);
        }
    }

    private void processJsonElement(JsonNode element, ReconstructionOperation operation, PurgedPersistentIdentifierBulkInserter purgedPersistentIdentifierBulkInserter)
        throws MetaDataExecutionException {

        final ReportLine reportLine = pullOutReportLineFromJsonElement(element, operation);

        if(!UNDEFINED.equals(reportLine.getType())) {
            List<PurgedPersistentIdentifier> purgedPersistentIdentifiers =
                purgedPersistentIdentifierExtractorFactory.instance(reportLine.getType())
                    .extractPurgedPersistentIdentifier(reportLine.getLine(), operation);

            for (PurgedPersistentIdentifier purgedPersistentIdentifier : purgedPersistentIdentifiers) {
                purgedPersistentIdentifierBulkInserter.append(purgedPersistentIdentifier);
            }
        }
    }

    private ReportLine pullOutReportLineFromJsonElement(JsonNode element, ReconstructionOperation operation)
        throws MetaDataExecutionException {
        final String operationType = operation.getType();

        switch (operationType) {
            case ELIMINATION_ACTION:
                return getReportLineFromEliminationReport(element);
            case DELETE_GOT_VERSIONS:
                return getReportLineFromDeletingVersionsReport(element);
            case TRANSFER_REPLY:
                return getReportLineFromTransferredReport(element);
            default:
                throw new MetaDataExecutionException("Illegal reconstruction type parameter");
        }
    }

    private ReportLine getReportLineFromDeletingVersionsReport(JsonNode element) {
        if (element.has(DETAIL_TYPE) && DELETE_GOT_VERSIONS.equals(element.get(DETAIL_TYPE).asText())) {
            return new ReportLine(element, DELETED_GOT_VERSION);
        }
        return new ReportLine(element, UNDEFINED);
    }

    private ReportLine getReportLineFromEliminationReport(JsonNode element) throws MetaDataExecutionException {
        if (element.has(PARAMS)) {
            JsonNode params = element.get(PARAMS);
            if (params.has(TYPE)) {
                String type = params.get(TYPE).asText();
                switch (type) {
                    case UNIT:
                        return new ReportLine(params, DELETED_UNIT);
                    case OBJECT_GROUP:
                        return new ReportLine(params, DELETED_OBJECT_GROUP);
                    default:
                        throw new MetaDataExecutionException(
                            "Illegal reconstruction type parameter '" + type + "'");
                }
            }
        }
        return new ReportLine(element, UNDEFINED);
    }

    private ReportLine getReportLineFromTransferredReport(JsonNode element) throws MetaDataExecutionException {
        if (element.has(PARAMS)) {
            JsonNode params = element.get(PARAMS);
            if (params.has(TYPE)) {
                String type = params.get(TYPE).asText();
                switch (type) {
                    case UNIT:
                        return new ReportLine(params, TRANSFERRED_UNIT);
                    case OBJECT_GROUP:
                        return new ReportLine(params, TRANSFERRED_OBJECT_GROUP);
                    default:
                        throw new MetaDataExecutionException(
                            "Illegal reconstruction type parameter '" + type + "'");
                }
            }
        }
        return new ReportLine(element, UNDEFINED);
    }
}

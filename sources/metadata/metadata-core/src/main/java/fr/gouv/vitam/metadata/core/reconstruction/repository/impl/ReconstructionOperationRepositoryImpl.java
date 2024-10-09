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
package fr.gouv.vitam.metadata.core.reconstruction.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.LocalDateUtil;
import fr.gouv.vitam.common.database.builder.query.InQuery;
import fr.gouv.vitam.common.database.builder.query.QueryHelper;
import fr.gouv.vitam.common.database.builder.query.RangeQuery;
import fr.gouv.vitam.common.database.builder.query.VitamFieldsHelper;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitam.logbook.common.exception.LogbookClientException;
import fr.gouv.vitam.logbook.operations.client.LogbookOperationsClient;
import fr.gouv.vitam.logbook.operations.client.LogbookOperationsClientFactory;
import fr.gouv.vitam.metadata.core.reconstruction.exception.ReconstructionException;
import fr.gouv.vitam.metadata.core.reconstruction.model.ReconstructionOperation;
import fr.gouv.vitam.metadata.core.reconstruction.repository.ReconstructionOperationRepository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static fr.gouv.vitam.common.database.builder.query.QueryHelper.and;
import static fr.gouv.vitam.common.json.JsonHandler.getFromJsonNodeList;

public class ReconstructionOperationRepositoryImpl implements ReconstructionOperationRepository {

    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(ReconstructionOperationRepository.class);

    private final LogbookOperationsClientFactory logbookOperationsClientFactory;

    public ReconstructionOperationRepositoryImpl() {
        this(LogbookOperationsClientFactory.getInstance());
    }

    public ReconstructionOperationRepositoryImpl(LogbookOperationsClientFactory logbookOperationsClientFactory) {
        this.logbookOperationsClientFactory = logbookOperationsClientFactory;
    }

    @Override
    public List<ReconstructionOperation> fetchReconstructionOperations(LocalDateTime startDate, LocalDateTime endDate)
        throws ReconstructionException {
        try (LogbookOperationsClient client = logbookOperationsClientFactory.getClient()) {
            JsonNode queryDsl = getDslForSelectOperation(startDate, endDate);

            final JsonNode result = client.selectOperation(queryDsl, false, false);
            RequestResponseOK<JsonNode> requestResponse = RequestResponseOK.getFromJsonNode(result);
            List<LogbookOperation> logbookOperations = getFromJsonNodeList(
                (requestResponse).getResults(),
                new TypeReference<>() {}
            );

            return logbookOperations
                .stream()
                .map(
                    logbookOperation ->
                        ReconstructionOperation.builder()
                            .setId(logbookOperation.getId())
                            .setTenant(logbookOperation.getTenant())
                            .setType(logbookOperation.getEvType())
                            .setLastPersistedDate(
                                Optional.ofNullable(logbookOperation.getLastPersistentDate()).orElse("")
                            )
                            .build()
                )
                .collect(Collectors.toList());
        } catch (final LogbookClientException | InvalidParseOperationException e) {
            throw new ReconstructionException("Error fetching reconstruction operations", e);
        }
    }

    JsonNode getDslForSelectOperation(LocalDateTime startDate, LocalDateTime endDate) {
        Select select = new Select();
        try {
            final Date from = LocalDateUtil.getDate(startDate);
            final Date to = LocalDateUtil.getDate(endDate);

            RangeQuery range = QueryHelper.range(VitamFieldsHelper.lastPersistedDate(), from, true, to, false);
            final String[] evTypes = { "ELIMINATION_ACTION", "DELETE_GOT_VERSIONS", "TRANSFER_REPLY" };
            final InQuery type = QueryHelper.in("evType", evTypes);
            final String[] operationOutDetails = {
                "ELIMINATION_ACTION.OK",
                "ELIMINATION_ACTION.WARNING",
                "DELETE_GOT_VERSIONS.OK",
                "DELETE_GOT_VERSIONS.WARNING",
                "TRANSFER_REPLY.OK",
                "TRANSFER_REPLY.WARNING"
            };
            final InQuery status = QueryHelper.in("events" + "." + "outDetail", operationOutDetails);
            select.setLimitFilter(0, 10000);
            select.addOrderByAscFilter(VitamFieldsHelper.lastPersistedDate());
            select.setQuery(and().add(range, type, status));
        } catch (InvalidCreateOperationException | InvalidParseOperationException e) {
            throw new IllegalStateException("Error when generate DSL for get Operations", e);
        }
        return select.getFinalSelect();
    }
}

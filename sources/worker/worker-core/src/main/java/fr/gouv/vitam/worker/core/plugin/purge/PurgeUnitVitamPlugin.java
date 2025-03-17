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
package fr.gouv.vitam.worker.core.plugin.purge;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.annotations.VisibleForTesting;
import fr.gouv.vitam.common.database.utils.MetadataDocumentHelper;
import fr.gouv.vitam.common.model.StatusCode;
import fr.gouv.vitam.logbook.common.exception.LogbookClientBadRequestException;
import fr.gouv.vitam.logbook.common.exception.LogbookClientServerException;
import fr.gouv.vitam.logbook.lifecycles.client.LogbookLifeCyclesClientFactory;
import fr.gouv.vitam.metadata.api.exception.MetaDataClientServerException;
import fr.gouv.vitam.metadata.api.exception.MetaDataExecutionException;
import fr.gouv.vitam.storage.engine.client.exception.StorageServerClientException;
import fr.gouv.vitam.worker.common.HandlerIO;
import fr.gouv.vitam.worker.core.exception.ProcessingStatusException;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Purge unit plugin.
 */
public class PurgeUnitVitamPlugin extends PurgeUnitPlugin {

    private final PurgeDeleteService purgeDeleteService;

    /**
     * Default constructor
     *
     * @param actionId
     */
    public PurgeUnitVitamPlugin(String actionId) {
        this(
            actionId,
            new PurgeDeleteService(),
            new PurgeReportService(),
            LogbookLifeCyclesClientFactory.getInstance()
        );
    }

    /***
     * Test only constructor
     */
    @VisibleForTesting
    protected PurgeUnitVitamPlugin(
        String actionId,
        PurgeDeleteService purgeDeleteService,
        PurgeReportService purgeReportService,
        LogbookLifeCyclesClientFactory llfcClientFactory
    ) {
        super(actionId, purgeReportService, llfcClientFactory);
        this.purgeDeleteService = purgeDeleteService;
    }

    public void deleteUnit(Map<String, JsonNode> unitsById, Set<String> unitsToDelete, HandlerIO handler)
        throws ProcessingStatusException {
        try {
            Map<String, String> unitIdsWithStrategiesToDelete = unitsById
                .entrySet()
                .stream()
                .filter(entry -> unitsToDelete.contains(entry.getKey()))
                .collect(
                    Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> MetadataDocumentHelper.getStrategyIdFromUnit(entry.getValue())
                    )
                );
            this.purgeDeleteService.deleteUnits(unitIdsWithStrategiesToDelete, handler);
        } catch (
            MetaDataExecutionException
            | MetaDataClientServerException
            | LogbookClientBadRequestException
            | StorageServerClientException
            | LogbookClientServerException e
        ) {
            throw new ProcessingStatusException(
                StatusCode.FATAL,
                "Could not delete units [" + String.join(", ", unitsToDelete) + "]",
                e
            );
        }
    }
}

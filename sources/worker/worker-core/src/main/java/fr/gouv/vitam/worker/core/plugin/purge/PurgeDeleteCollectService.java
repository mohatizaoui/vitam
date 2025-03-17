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

import com.google.common.annotations.VisibleForTesting;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.common.model.StatusCode;
import fr.gouv.vitam.logbook.common.exception.LogbookClientBadRequestException;
import fr.gouv.vitam.logbook.common.exception.LogbookClientServerException;
import fr.gouv.vitam.logbook.lifecycles.client.LogbookLifeCyclesClient;
import fr.gouv.vitam.logbook.lifecycles.client.LogbookLifeCyclesClientFactory;
import fr.gouv.vitam.metadata.api.exception.MetaDataClientServerException;
import fr.gouv.vitam.metadata.api.exception.MetaDataExecutionException;
import fr.gouv.vitam.metadata.client.MetaDataClient;
import fr.gouv.vitam.storage.engine.common.model.DataCategory;
import fr.gouv.vitam.worker.common.HandlerIO;
import fr.gouv.vitam.worker.core.exception.ProcessingStatusException;
import fr.gouv.vitam.workspace.api.exception.ContentAddressableStorageNotFoundException;
import fr.gouv.vitam.workspace.api.exception.ContentAddressableStorageServerException;
import fr.gouv.vitam.workspace.client.WorkspaceClient;
import fr.gouv.vitam.workspace.client.WorkspaceCollectClientFactory;
import joptsimple.internal.Strings;

import java.util.List;
import java.util.stream.Collectors;

/**
 * PurgeDeleteService class
 */
public class PurgeDeleteCollectService extends CommonPurgeDeleteService {

    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(PurgeDeleteCollectService.class);

    private final WorkspaceCollectClientFactory workspaceCollectClientFactory;
    private final LogbookLifeCyclesClientFactory logbookLifeCyclesClientFactory;

    @VisibleForTesting
    PurgeDeleteCollectService(
        WorkspaceCollectClientFactory workspaceCollectClientFactory,
        LogbookLifeCyclesClientFactory logbookLifeCyclesClientFactory
    ) {
        this.workspaceCollectClientFactory = workspaceCollectClientFactory;
        this.logbookLifeCyclesClientFactory = logbookLifeCyclesClientFactory;
    }

    public PurgeDeleteCollectService() {
        this(WorkspaceCollectClientFactory.getInstance(), LogbookLifeCyclesClientFactory.getInstance());
    }

    public void deleteObjects(List<PurgeObjectGroupParams> objectGroupParams, HandlerIO handler)
        throws ProcessingStatusException {
        try {
            storageDeleteBinaries(objectGroupParams, DataCategory.OBJECT, Strings.EMPTY, handler);
        } catch (ContentAddressableStorageNotFoundException | ContentAddressableStorageServerException e) {
            throw new ProcessingStatusException(
                StatusCode.FATAL,
                "Could not delete object groups [" +
                objectGroupParams.stream().map(PurgeObjectGroupParams::getId).collect(Collectors.joining(", ")) +
                "]",
                e
            );
        }
    }

    @Override
    public void deleteObjectGroups(List<PurgeObjectGroupParams> objectGroupParams, HandlerIO handler)
        throws InvalidParseOperationException, MetaDataExecutionException, MetaDataClientServerException, LogbookClientBadRequestException, LogbookClientServerException {
        List<String> objectGroupIds = objectGroupParams.stream().map(PurgeObjectGroupParams::getId).toList();
        try (LogbookLifeCyclesClient logbookLifeCyclesClient = logbookLifeCyclesClientFactory.getClient()) {
            logbookLifeCyclesClient.deleteLifecycleObjectGroupBulk(objectGroupIds);
        }

        try (MetaDataClient metaDataClient = handler.getMetaDataClient()) {
            metaDataClient.deleteObjectGroupBulk(objectGroupIds);
        }
    }

    @Override
    public void storageDeleteBinaries(
        List<PurgeObjectGroupParams> objectGroupParams,
        DataCategory dataCategory,
        String fileExtension,
        HandlerIO handler
    ) throws ContentAddressableStorageNotFoundException, ContentAddressableStorageServerException {
        if (DataCategory.OBJECT == dataCategory) {
            List<PurgeObjectParams> objectParams = objectGroupParams
                .stream()
                .flatMap(objectGroup -> objectGroup.getObjects().stream())
                .toList();
            //FIXME use FileNameresolver
            try (WorkspaceClient workspaceCollectClient = workspaceCollectClientFactory.getClient()) {
                for (PurgeObjectParams objectPurgeElt : objectParams) {
                    workspaceCollectClient.deleteObject(objectPurgeElt.getOpi(), objectPurgeElt.getUri());
                }
            }
        }
    }
}

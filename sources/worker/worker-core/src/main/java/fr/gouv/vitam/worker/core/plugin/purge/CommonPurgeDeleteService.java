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

import fr.gouv.vitam.common.database.builder.query.VitamFieldsHelper;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.multiple.UpdateMultiQuery;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.common.model.StatusCode;
import fr.gouv.vitam.common.thread.VitamThreadUtils;
import fr.gouv.vitam.logbook.common.exception.LogbookClientBadRequestException;
import fr.gouv.vitam.logbook.common.exception.LogbookClientServerException;
import fr.gouv.vitam.metadata.api.exception.MetaDataClientServerException;
import fr.gouv.vitam.metadata.api.exception.MetaDataExecutionException;
import fr.gouv.vitam.metadata.client.MetaDataClient;
import fr.gouv.vitam.storage.engine.client.exception.StorageServerClientException;
import fr.gouv.vitam.storage.engine.common.model.DataCategory;
import fr.gouv.vitam.worker.common.HandlerIO;
import fr.gouv.vitam.worker.core.exception.ProcessingStatusException;
import fr.gouv.vitam.workspace.api.exception.ContentAddressableStorageNotFoundException;
import fr.gouv.vitam.workspace.api.exception.ContentAddressableStorageServerException;
import joptsimple.internal.Strings;

import java.util.List;
import java.util.Set;

import static fr.gouv.vitam.common.database.builder.query.action.UpdateActionHelper.add;
import static fr.gouv.vitam.common.database.builder.query.action.UpdateActionHelper.pull;

/**
 * CommonPurgeDeleteService class
 */
public abstract class CommonPurgeDeleteService {

    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(CommonPurgeDeleteService.class);

    public void deleteUnits(Set<String> unitsGuids, HandlerIO handler)
        throws MetaDataExecutionException, MetaDataClientServerException {
        try (MetaDataClient metaDataClient = handler.getMetaDataClient()) {
            metaDataClient.deleteUnitsBulk(unitsGuids);
        }
    }

    public void deleteObjects(List<PurgeObjectGroupParams> objectGroupParams, HandlerIO handler)
        throws ProcessingStatusException {
        try {
            storageDeleteBinaries(objectGroupParams, DataCategory.OBJECT, Strings.EMPTY, handler);
        } catch (
            StorageServerClientException
            | ContentAddressableStorageNotFoundException
            | ContentAddressableStorageServerException e
        ) {
            throw new ProcessingStatusException(StatusCode.FATAL, "An error occurred during object deleting", e);
        }
    }

    public void detachObjectGroupFromDeleteParentUnits(
        String objectGroupId,
        Set<String> parentUnitsToRemove,
        HandlerIO handler
    ) throws ProcessingStatusException {
        try (MetaDataClient metaDataClient = handler.getMetaDataClient()) {
            UpdateMultiQuery updateMultiQuery = new UpdateMultiQuery();
            updateMultiQuery.addActions(
                pull(VitamFieldsHelper.unitups(), parentUnitsToRemove.toArray(new String[0])),
                add(VitamFieldsHelper.operations(), VitamThreadUtils.getVitamSession().getRequestId())
            );

            metaDataClient.updateObjectGroupById(updateMultiQuery.getFinalUpdate(), objectGroupId);
        } catch (
            MetaDataClientServerException
            | MetaDataExecutionException
            | InvalidParseOperationException
            | InvalidCreateOperationException e
        ) {
            throw new ProcessingStatusException(
                StatusCode.FATAL,
                "An error occurred during object group detachment",
                e
            );
        }
    }

    public abstract void deleteObjectGroups(List<PurgeObjectGroupParams> objectGroupParams, HandlerIO handler)
        throws InvalidParseOperationException, MetaDataExecutionException, MetaDataClientServerException, StorageServerClientException, LogbookClientBadRequestException, LogbookClientServerException, ContentAddressableStorageNotFoundException, ContentAddressableStorageServerException;

    public abstract void storageDeleteBinaries(
        List<PurgeObjectGroupParams> objectGroupParams,
        DataCategory dataCategory,
        String fileExtension,
        HandlerIO handler
    )
        throws StorageServerClientException, ContentAddressableStorageNotFoundException, ContentAddressableStorageServerException;
}

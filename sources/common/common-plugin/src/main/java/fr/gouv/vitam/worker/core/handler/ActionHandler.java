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
package fr.gouv.vitam.worker.core.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitam.common.ParametersChecker;
import fr.gouv.vitam.common.SedaConstants;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.common.model.ItemStatus;
import fr.gouv.vitam.common.model.StatusCode;
import fr.gouv.vitam.common.model.VitamAutoCloseable;
import fr.gouv.vitam.logbook.common.parameters.LogbookParameterName;
import fr.gouv.vitam.processing.common.exception.ProcessingException;
import fr.gouv.vitam.processing.common.parameter.WorkerParameters;
import fr.gouv.vitam.worker.common.HandlerIO;

import java.util.ArrayList;
import java.util.List;

/**
 * ActionHandler abstract class of interface Action<br/>
 * <br/>
 * Each ActionHandler must implements some public static methods:</br>
 *
 * <pre>
 * <code>
 * // Return the unique Id of this Handler
 * public static final String getId();
 * // Later on (not available now), some other methods for Input/Output arguments
 * </code></code>
 */
public abstract class ActionHandler implements VitamAutoCloseable {

    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(ActionHandler.class);

    /**
     * Check mandatory parameters
     *
     * @param parameters parameter to check
     * @throws IllegalArgumentException thrown when a mandatory parameter is null or empty
     */
    public void checkMandatoryParameters(WorkerParameters parameters) {
        ParametersChecker.checkNullOrEmptyParameters(parameters);
    }

    /**
     * Check mandatory parameter
     *
     * @param handler input output list
     * @throws ProcessingException when handler io is not complete
     */
    public void checkMandatoryIOParameter(HandlerIO handler) throws ProcessingException {
        throw new IllegalStateException("Not implemented.");
    }

    @Override
    public void close() {
        // nothing;
    }

    /**
     * Execute an action
     *
     * @param param {@link WorkerParameters}
     * @param handler the handlerIo
     * @return CompositeItemStatus:response contains a list of functional message and status code
     * @throws ProcessingException if an error is encountered when executing the action
     */
    public ItemStatus execute(WorkerParameters param, HandlerIO handler) throws ProcessingException {
        throw new IllegalStateException("Not implemented.");
    }

    /**
     * @param workerParameters
     * @param handler
     * @return
     * @throws ProcessingException
     */
    public List<ItemStatus> executeList(WorkerParameters workerParameters, HandlerIO handler)
        throws ProcessingException {
        try {
            List<ItemStatus> aggregateItemStatus = new ArrayList<>();

            List<String> objectNameList = workerParameters.getObjectNameList();
            List<JsonNode> objectMetadataList = workerParameters.getObjectMetadataList();

            for (int i = 0; i < objectNameList.size(); i++) {
                String objectId = objectNameList.get(i);
                JsonNode metadata = objectMetadataList != null && !objectMetadataList.isEmpty()
                    ? objectMetadataList.get(i)
                    : null;

                workerParameters.setObjectName(objectId);
                workerParameters.setObjectMetadata(metadata);
                handler.setCurrentObjectId(objectId);

                ItemStatus itemStatus = execute(workerParameters, handler);

                aggregateItemStatus.add(itemStatus);
            }
            return aggregateItemStatus;
        } finally {
            handler.setCurrentObjectId(null);
        }
    }

    /**
     * Update a detail item status
     *
     * @param globalCompositeItemStatus
     * @param value
     * @param globalOutcomeDetailSubCode
     */
    // TODO: extract to utilities class?
    public static void updateDetailItemStatus(
        final ItemStatus globalCompositeItemStatus,
        final String value,
        final String globalOutcomeDetailSubCode
    ) {
        try {
            if (value != null) {
                ObjectNode evDetData = (ObjectNode) JsonHandler.getFromString(
                    globalCompositeItemStatus.getEvDetailData()
                );
                String oldValue = "";
                if (evDetData.has(SedaConstants.EV_DET_TECH_DATA)) {
                    oldValue = evDetData.get(SedaConstants.EV_DET_TECH_DATA).textValue() + " \n";
                }
                evDetData.put(SedaConstants.EV_DET_TECH_DATA, oldValue + value);
                globalCompositeItemStatus.setEvDetailData(JsonHandler.unprettyPrint(evDetData));
                globalCompositeItemStatus.setMasterData(
                    LogbookParameterName.eventDetailData.name(),
                    JsonHandler.unprettyPrint(evDetData)
                );
            }
            if (null != globalOutcomeDetailSubCode) {
                globalCompositeItemStatus.setGlobalOutcomeDetailSubcode(globalOutcomeDetailSubCode);
            }
        } catch (InvalidParseOperationException e1) {
            LOGGER.error("Unexpected exception : evDetData invalid", e1);
            globalCompositeItemStatus.increment(StatusCode.FATAL);
        }
    }
}

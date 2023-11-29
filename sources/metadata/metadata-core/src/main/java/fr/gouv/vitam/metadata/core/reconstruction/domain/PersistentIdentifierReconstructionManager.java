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

import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.metadata.core.config.MetaDataConfiguration;
import fr.gouv.vitam.metadata.core.reconstruction.exception.ReconstructionException;
import fr.gouv.vitam.metadata.core.reconstruction.model.ReconstructionOperation;
import fr.gouv.vitam.metadata.core.reconstruction.repository.OperationReportRepository;
import fr.gouv.vitam.metadata.core.reconstruction.repository.PersistentIdentifierRepository;
import fr.gouv.vitam.metadata.core.reconstruction.repository.ReconstructionOperationRepository;
import fr.gouv.vitam.metadata.core.reconstruction.repository.ReconstructionResponse;

import java.time.LocalDateTime;
import java.util.List;

import static fr.gouv.vitam.metadata.core.reconstruction.repository.ReconstructionResponse.ReconstructionStatus.FAILURE;
import static fr.gouv.vitam.metadata.core.reconstruction.repository.ReconstructionResponse.ReconstructionStatus.SUCCESS;

public class PersistentIdentifierReconstructionManager {

    private static final VitamLogger LOGGER =
        VitamLoggerFactory.getInstance(PersistentIdentifierReconstructionManager.class);


    final private OperationReportParser operationReportParser;
    final private ReconstructionOperationRepository reconstructionOperationRepository;

    public PersistentIdentifierReconstructionManager(OperationReportRepository operationReportRepository,
        ReconstructionOperationRepository reconstructionOperationRepository,
        MetaDataConfiguration metaDataConfiguration,
        PersistentIdentifierRepository persistentIdentifierRepository) {
        this.reconstructionOperationRepository = reconstructionOperationRepository;
        this.operationReportParser =
            new OperationReportParser(operationReportRepository, metaDataConfiguration, persistentIdentifierRepository,
                new PurgedPersistentIdentifierExtractorFactory());
    }

    public PersistentIdentifierReconstructionManager(
        ReconstructionOperationRepository reconstructionOperationRepository,
        OperationReportParser operationReportParser) {
        this.reconstructionOperationRepository = reconstructionOperationRepository;
        this.operationReportParser = operationReportParser;
    }

    public ReconstructionResponse reconstruct(LocalDateTime startDate, LocalDateTime endDate) {

        LocalDateTime lastSuccessfulOperationDate = startDate;
        try {
            final List<ReconstructionOperation> reconstructionOperations = reconstructionOperationRepository
                .fetchReconstructionOperations(startDate, endDate);
            LOGGER.info(
                "Persistent Identifier Reconstruction : number of operations to reconstruct between dates {} and {} : {}",
                startDate, endDate, reconstructionOperations.size());
            for (ReconstructionOperation operation : reconstructionOperations) {
                lastSuccessfulOperationDate = operationReportParser.processReportFromOperation(operation);
            }
        } catch (ReconstructionException e) {
            LOGGER.error("Persistent Identifier Reconstruction failed ", e);
            return new ReconstructionResponse.Builder().status(FAILURE)
                .lastSuccessfulOperationDate(lastSuccessfulOperationDate).build();
        }
        return new ReconstructionResponse.Builder().status(SUCCESS)
            .lastSuccessfulOperationDate(lastSuccessfulOperationDate).build();
    }
}

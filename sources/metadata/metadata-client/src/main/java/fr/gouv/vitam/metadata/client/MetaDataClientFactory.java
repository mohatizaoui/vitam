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
package fr.gouv.vitam.metadata.client;

import fr.gouv.vitam.common.ParametersChecker;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.client.VitamClientFactory;
import fr.gouv.vitam.common.client.configuration.ClientConfiguration;
import fr.gouv.vitam.common.client.configuration.ClientConfigurationImpl;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.common.model.processing.WorkFlowExecutionContext;

import java.io.IOException;
import java.net.URI;

/**
 * Metadata client factory
 */
public class MetaDataClientFactory extends VitamClientFactory<MetaDataClient> {

    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(MetaDataClientFactory.class);
    private static final String CONFIGURATION_FILENAME = "metadata-client.conf";

    private static final MetaDataClientFactory META_DATA_CLIENT_FACTORY = new MetaDataClientFactory("/metadata/v1");
    private static final MetaDataClientFactory META_DATA_COLLECT_CLIENT_FACTORY = new MetaDataClientFactory(
        "/metadata-collect/v1"
    );

    private MetaDataClientFactory(String resourcePath) {
        // All requests from client are SMALL, but responses from server could be Huge
        // So Chunked mode inactive on client side
        super(changeConfigurationFile(CONFIGURATION_FILENAME), resourcePath, false);
    }

    /**
     * Get MetaDataClientFactory instance
     *
     * @return the instance
     */
    public static MetaDataClientFactory getInstance() {
        return getInstance(WorkFlowExecutionContext.VITAM);
    }

    /**
     * Get the MetaDataClientFactory instance for the given workflow execution context
     *
     * @param executionContext the workflow execution context
     * @return the instance
     */
    public static MetaDataClientFactory getInstance(WorkFlowExecutionContext executionContext) {
        return switch (executionContext) {
            case VITAM -> META_DATA_CLIENT_FACTORY;
            case COLLECT -> META_DATA_COLLECT_CLIENT_FACTORY;
        };
    }

    @Override
    public MetaDataClient getClient() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Actually only one client implementation exists, so ignore client type value");
        }
        MetaDataClient client;
        switch (getVitamClientType()) {
            case MOCK:
                client = new MetaDataClientMock();
                break;
            case PRODUCTION:
                client = new MetaDataClientRest(this);
                break;
            default:
                throw new IllegalArgumentException("metadata type unknown");
        }
        return client;
    }

    /**
     * Change client configuration from a Yaml files
     *
     * @param configurationPath the path to the configuration file
     * @return ClientConfiguration
     */
    static ClientConfiguration changeConfigurationFile(String configurationPath) {
        ClientConfiguration configuration = null;
        try {
            configuration = PropertiesUtils.readYaml(
                PropertiesUtils.findFile(configurationPath),
                ClientConfigurationImpl.class
            );
        } catch (final IOException fnf) {
            LOGGER.debug("Error when retrieving configuration file {}, using mock", configurationPath, fnf);
        }
        if (configuration == null) {
            LOGGER.error("Error when retrieving configuration file {}, using mock", configurationPath);
        }
        return configuration;
    }

    /**
     * @param configuration null for MOCK
     */
    public static void changeMode(ClientConfiguration configuration) {
        for (WorkFlowExecutionContext executionContext : WorkFlowExecutionContext.values()) {
            getInstance(executionContext).initialisation(
                configuration,
                getInstance(executionContext).getResourcePath()
            );
        }
    }

    /**
     * change mode client by server url
     *
     * @param serviceUrl as String
     */
    public static void changeMode(String serviceUrl, WorkFlowExecutionContext executionContext) {
        ParametersChecker.checkParameter("Server Url can not be null", serviceUrl);
        final URI uri = URI.create(serviceUrl);
        final ClientConfiguration configuration = new ClientConfigurationImpl(uri.getHost(), uri.getPort());
        changeMode(configuration, executionContext);
    }

    /**
     * @param configuration null for MOCK
     */
    public static void changeMode(ClientConfiguration configuration, WorkFlowExecutionContext executionContext) {
        MetaDataClientFactory instance = getInstance(executionContext);
        instance.initialisation(configuration, instance.getResourcePath());
    }
}

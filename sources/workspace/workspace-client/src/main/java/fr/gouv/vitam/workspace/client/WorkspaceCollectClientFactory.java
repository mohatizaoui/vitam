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
package fr.gouv.vitam.workspace.client;

import fr.gouv.vitam.common.ParametersChecker;
import fr.gouv.vitam.common.client.configuration.ClientConfiguration;
import fr.gouv.vitam.common.client.configuration.ClientConfigurationImpl;
import fr.gouv.vitam.common.model.processing.WorkFlowExecutionContext;

import java.net.URI;

/**
 * WorkspaceClient factory for creating workspace client
 */
public class WorkspaceCollectClientFactory extends WorkspaceClientFactory {

    private static final WorkspaceCollectClientFactory WORKSPACE_COLLECT_CLIENT_FACTORY =
        new WorkspaceCollectClientFactory("/workspace-collect/v1");

    private WorkspaceCollectClientFactory(String resourcePath) {
        super(resourcePath);
    }

    /**
     * Get the WorkspaceClientFactory instance
     *
     * @return the instance
     */

    public static WorkspaceCollectClientFactory getInstance() {
        return WORKSPACE_COLLECT_CLIENT_FACTORY;
    }

    @Override
    public WorkspaceClient getClient() {
        return new WorkspaceClient(this);
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
    static void changeMode(ClientConfiguration configuration, WorkFlowExecutionContext executionContext) {
        WorkspaceCollectClientFactory instance = getInstance(/*executionContext*/); //TODO fix me
        instance.initialisation(configuration, instance.getResourcePath());
    }
}

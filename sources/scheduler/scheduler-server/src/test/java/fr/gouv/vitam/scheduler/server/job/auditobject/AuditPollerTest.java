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

package fr.gouv.vitam.scheduler.server.job.auditobject;

import fr.gouv.vitam.common.exception.BadRequestException;
import fr.gouv.vitam.common.exception.InternalServerException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.exception.VitamException;
import fr.gouv.vitam.common.exception.WorkflowNotFoundException;
import fr.gouv.vitam.processing.management.client.ProcessingManagementClient;
import fr.gouv.vitam.processing.management.client.ProcessingManagementClientFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static fr.gouv.vitam.scheduler.server.job.auditobject.RandomValuesFixture.randomVitamException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

public class AuditPollerTest {

    private static final Integer SOME_TENANT_ID = 1;
    private static final String SOME_OPERATION_ID = "SOME ID";
    @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Mock private ProcessingManagementClientFactory processingManagementClientFactory;
    @Mock private ProcessingManagementClient processingManagementClient;
    private AuditPoller auditPoller;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.auditPoller = new AuditPoller(processingManagementClientFactory, SOME_TENANT_ID, SOME_OPERATION_ID);
        given(processingManagementClientFactory.getClient()).willReturn(processingManagementClient);
    }

    @Test
    public void shouldReturnTrueWhenWorkflowNotFound()
        throws VitamClientException, InternalServerException, BadRequestException {
        // GIVEN
        given(processingManagementClient.getOperationProcessStatus(SOME_OPERATION_ID)).willThrow(
            WorkflowNotFoundException.class);

        // WHEN
        boolean result = auditPoller.waitForTermination();

        // THEN
        assertThat(result).isTrue();
    }

    @Test
    public void shouldReturnFalseWhenAnExceptionIsThrown()
        throws VitamClientException, InternalServerException, BadRequestException {
        // GIVEN
        Class<? extends VitamException>[] nokVitanExceptions =
            new Class[] {VitamClientException.class, InternalServerException.class, BadRequestException.class};
        Class<? extends VitamException> someException = randomVitamException(nokVitanExceptions);
        given(processingManagementClient.getOperationProcessStatus(SOME_OPERATION_ID)).willThrow(someException);

        // WHEN
        boolean result = auditPoller.waitForTermination();

        // THEN
        assertThat(result).isFalse();
    }

}
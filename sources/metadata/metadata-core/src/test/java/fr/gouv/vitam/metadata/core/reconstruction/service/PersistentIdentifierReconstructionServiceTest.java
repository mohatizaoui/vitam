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
package fr.gouv.vitam.metadata.core.reconstruction.service;

import fr.gouv.vitam.common.LocalDateUtil;
import fr.gouv.vitam.common.thread.RunWithCustomExecutor;
import fr.gouv.vitam.common.thread.RunWithCustomExecutorRule;
import fr.gouv.vitam.common.thread.VitamThreadPoolExecutor;
import fr.gouv.vitam.common.thread.VitamThreadUtils;
import fr.gouv.vitam.metadata.api.model.PersistentIdentifierReconstructionRequest;
import fr.gouv.vitam.metadata.core.config.MetaDataConfiguration;
import fr.gouv.vitam.metadata.core.reconstruction.domain.OffsetManager;
import fr.gouv.vitam.metadata.core.reconstruction.domain.PersistentIdentifierReconstructionManager;
import fr.gouv.vitam.metadata.core.reconstruction.repository.ReconstructionResponse;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static fr.gouv.vitam.metadata.core.reconstruction.repository.ReconstructionResponse.ReconstructionStatus.FAILURE;
import static fr.gouv.vitam.metadata.core.reconstruction.repository.ReconstructionResponse.ReconstructionStatus.SUCCESS;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWithCustomExecutor
public class PersistentIdentifierReconstructionServiceTest {

    @ClassRule
    public static RunWithCustomExecutorRule runInThread = new RunWithCustomExecutorRule(
        VitamThreadPoolExecutor.getDefaultExecutor()
    );

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private OffsetManager offsetManager;

    @Mock
    private PersistentIdentifierReconstructionManager persistentIdentifierReconstructionManager;

    @Mock
    private MetaDataConfiguration metaDataConfiguration;

    private PersistentIdentifierReconstructionService persistentIdentifierReconstructionService;

    @Before
    public void setup() {
        when(metaDataConfiguration.getPersistentIdentifierReconstructionThreadPoolSize()).thenReturn(10);
        persistentIdentifierReconstructionService = new PersistentIdentifierReconstructionService(
            offsetManager,
            persistentIdentifierReconstructionManager,
            metaDataConfiguration
        );
    }

    @Test
    @RunWithCustomExecutor
    public void testReconstruct_Success() {
        VitamThreadUtils.getVitamSession().setTenantId(0);

        //Given
        LocalDateTime startDate = LocalDateUtil.now().minusDays(100);
        LocalDateTime endDate = LocalDateUtil.now().plusDays(1);
        List<Integer> tenantList = Arrays.asList(1, 2, 3);
        PersistentIdentifierReconstructionRequest persistentIdentifierReconstructionRequest =
            new PersistentIdentifierReconstructionRequest();
        persistentIdentifierReconstructionRequest.setTenants(tenantList);
        when(offsetManager.retrieveLastReconstructionDateFromOffset(any(Integer.class))).thenReturn(startDate);
        when(offsetManager.retrieveEndDateWithDelay(anyLong())).thenReturn(endDate.minusDays(1));
        when(persistentIdentifierReconstructionManager.reconstruct(startDate, endDate.minusDays(1))).thenReturn(
            new ReconstructionResponse.Builder()
                .status(SUCCESS)
                .lastSuccessfulOperationDate(endDate.minusDays(1))
                .build()
        );
        //When
        persistentIdentifierReconstructionService.reconstruct(persistentIdentifierReconstructionRequest);

        //Then
        verify(persistentIdentifierReconstructionManager, times(3)).reconstruct(startDate, endDate.minusDays(1)); // 3 tenants
        verify(offsetManager, times(3)).saveNextReconstructionDateInOffset(
            any(Integer.class),
            any(LocalDateTime.class)
        ); // 3 tenants
    }

    @Test
    @RunWithCustomExecutor
    public void reconstruct_Failure() {
        VitamThreadUtils.getVitamSession().setTenantId(0);

        List<Integer> tenantList = Arrays.asList(1, 2, 3);
        PersistentIdentifierReconstructionRequest persistentIdentifierReconstructionRequest =
            new PersistentIdentifierReconstructionRequest();
        persistentIdentifierReconstructionRequest.setTenants(tenantList);

        when(offsetManager.retrieveLastReconstructionDateFromOffset(anyInt())).thenThrow(
            new RuntimeException("Simulated exception")
        );

        ReconstructionResponse response = persistentIdentifierReconstructionService.reconstruct(
            persistentIdentifierReconstructionRequest
        );

        assertThat(FAILURE).isEqualTo(response.status);
    }

    @Test
    public void reconstruct_EmptyTenantList_ReturnsFailure() {
        List<Integer> tenants = Arrays.asList();
        PersistentIdentifierReconstructionRequest persistentIdentifierReconstructionRequest =
            new PersistentIdentifierReconstructionRequest();
        persistentIdentifierReconstructionRequest.setTenants(tenants);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> persistentIdentifierReconstructionService.reconstruct(persistentIdentifierReconstructionRequest)
        );

        assertThat("List of tenants cannot be null and must contain at least one element").isEqualTo(
            exception.getMessage()
        );
    }
}

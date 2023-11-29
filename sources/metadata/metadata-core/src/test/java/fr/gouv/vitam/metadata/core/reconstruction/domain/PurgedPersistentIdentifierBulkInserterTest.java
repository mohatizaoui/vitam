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

import fr.gouv.vitam.common.model.unit.PersistentIdentifierModel;
import fr.gouv.vitam.metadata.core.config.MetaDataConfiguration;
import fr.gouv.vitam.metadata.core.reconstruction.model.PurgedPersistentIdentifier;
import fr.gouv.vitam.metadata.core.reconstruction.repository.PersistentIdentifierRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class PurgedPersistentIdentifierBulkInserterTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private PurgedPersistentIdentifierBulkInserter bulkInserter;

    @Mock
    private MetaDataConfiguration metaDataConfiguration;

    @Mock
    private PersistentIdentifierRepository persistentIdentifierRepository;

    @Before
    public void setUp() {

        when(metaDataConfiguration.getPersistentIdentifierReconstructionBulkSize()).thenReturn(4);

        bulkInserter = new PurgedPersistentIdentifierBulkInserter(
            metaDataConfiguration,
            persistentIdentifierRepository
        );
    }

    @Test
    public void append_BelowBulkSize_NoInsertion() throws Exception {
        PurgedPersistentIdentifier identifier = new PurgedPersistentIdentifier.Builder()
            .setId("1")
            .setTenant(0)
            .setType("Unit")
            .build();

        bulkInserter.append(identifier);

        verify(persistentIdentifierRepository, never()).insert(anyList());
    }


    @Test
    public void append_AtBulkSize_InsertsAndClearsList() throws Exception {

        List<PersistentIdentifierModel> persistentIdentifier = new ArrayList<>();
        final PersistentIdentifierModel persistentIdentifierModel = new PersistentIdentifierModel();
        persistentIdentifierModel.setPersistentIdentifierType("ark");
        persistentIdentifierModel.setPersistentIdentifierContent("ark:/666567/001a957db5eadaac");
        persistentIdentifierModel.setPersistentIdentifierOrigin("OriginatingAgency");
        persistentIdentifierModel.setPersistentIdentifierReference("Agency-00221");
        persistentIdentifier.add(persistentIdentifierModel);

        PurgedPersistentIdentifier identifier1 = new PurgedPersistentIdentifier.Builder()
            .setId("1")
            .setTenant(0)
            .setType("Object")
            .setPersistentIdentifier(persistentIdentifier)
            .build();
        PurgedPersistentIdentifier identifier2 = new PurgedPersistentIdentifier.Builder()
            .setId("2")
            .setTenant(0)
            .setType("Unit")
            .setPersistentIdentifier(persistentIdentifier)
            .build();
        PurgedPersistentIdentifier identifier3 = new PurgedPersistentIdentifier.Builder()
            .setId("3")
            .setTenant(0)
            .setType("Unit")
            .setPersistentIdentifier(persistentIdentifier)
            .build();
        PurgedPersistentIdentifier identifier4 = new PurgedPersistentIdentifier.Builder()
            .setId("4")
            .setTenant(0)
            .setType("Unit")
            .setPersistentIdentifier(persistentIdentifier)
            .build();
        PurgedPersistentIdentifier identifier5 = new PurgedPersistentIdentifier.Builder()
            .setId("5")
            .setTenant(0)
            .setType("Object")
            .setPersistentIdentifier(persistentIdentifier)
            .build();

        doNothing().when(persistentIdentifierRepository).insert(anyList());

        bulkInserter.append(identifier1);
        bulkInserter.append(identifier2);
        bulkInserter.append(identifier3);
        bulkInserter.append(identifier4);
        bulkInserter.append(identifier5);

        verify(persistentIdentifierRepository).insert(anyList());
        assertThat(bulkInserter.getPurgedPersistentIdentifiers()).isEmpty();
    }

    @Test
    public void flush_EmptyList_NoInsertion() throws Exception {
        bulkInserter.flush();

        verify(persistentIdentifierRepository, never()).insert(anyList());
    }

    @Test
    public void flush_NonEmptyList_InsertsAndClearsList() throws Exception {

        List<PersistentIdentifierModel> persistentIdentifier = new ArrayList<>();
        final PersistentIdentifierModel persistentIdentifierModel = new PersistentIdentifierModel();
        persistentIdentifierModel.setPersistentIdentifierType("ark");
        persistentIdentifierModel.setPersistentIdentifierContent("ark:/666567/001a957db5eadaac");
        persistentIdentifierModel.setPersistentIdentifierOrigin("OriginatingAgency");
        persistentIdentifierModel.setPersistentIdentifierReference("Agency-00221");
        persistentIdentifier.add(persistentIdentifierModel);

        PurgedPersistentIdentifier identifier = new PurgedPersistentIdentifier.Builder()
            .setId("1")
            .setTenant(0)
            .setType("Object")
            .setPersistentIdentifier(persistentIdentifier)
            .build();

        bulkInserter.append(identifier);
        bulkInserter.flush();

        verify(persistentIdentifierRepository).insert(anyList());
        assertThat(bulkInserter.getPurgedPersistentIdentifiers()).isEmpty();
    }
}

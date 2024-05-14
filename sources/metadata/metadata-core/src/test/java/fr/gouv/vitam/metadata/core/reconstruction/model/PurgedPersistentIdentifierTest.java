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
package fr.gouv.vitam.metadata.core.reconstruction.model;

import fr.gouv.vitam.common.database.server.mongodb.VitamDocument;
import fr.gouv.vitam.common.model.unit.PersistentIdentifierModel;
import org.bson.Document;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class PurgedPersistentIdentifierTest {

    @Test
    public void fromPurgedPersistentIdentifier_CreatesDocumentCorrectly() {
        PersistentIdentifierModel persistentIdentifierModel = new PersistentIdentifierModel();
        persistentIdentifierModel.setPersistentIdentifierType("ark");
        persistentIdentifierModel.setPersistentIdentifierContent("ark/2345/679934346673");
        persistentIdentifierModel.setPersistentIdentifierReference("reference");
        PurgedPersistentIdentifier purgedIdentifier = new PurgedPersistentIdentifier.Builder()
            .setId("1")
            .setTenant(0)
            .setPersistentIdentifier(Collections.singletonList(persistentIdentifierModel))
            .setVersion(0)
            .setType("sampleType")
            .setObjectGroupId("sampleObjectGroupId")
            .setOperationId("sampleOperationId")
            .setOperationType("sampleOperationType")
            .setOperationLastPersistentDate("2023-01-01")
            .build();

        Document result = PurgedPersistentIdentifier.toDocument(purgedIdentifier);

        assertThat(result).isNotNull();
        assertThat("1").isEqualTo(result.getString(VitamDocument.ID));
        assertThat(0).isEqualTo(result.getInteger(VitamDocument.TENANT_ID));
        assertThat(0).isEqualTo(result.getInteger(VitamDocument.VERSION).intValue());
        assertThat("sampleType").isEqualTo(result.getString("type"));
        assertThat("sampleObjectGroupId").isEqualTo(result.getString("idObjectGroup"));
        assertThat("sampleOperationId").isEqualTo(result.getString("opId"));
        assertThat("sampleOperationType").isEqualTo(result.getString("opType"));
        assertThat("2023-01-01").isEqualTo(result.getString("opEndDate"));
    }

    @Test
    public void convertListToDocumentList_CreatesDocumentListCorrectly() {
        PurgedPersistentIdentifier purgedIdentifier1 = new PurgedPersistentIdentifier.Builder()
            .setId("1")
            .setTenant(0)
            .setPersistentIdentifier(Collections.singletonList(new PersistentIdentifierModel()))
            .setVersion(0)
            .setType("sampleType1")
            .setObjectGroupId("sampleObjectGroupId1")
            .setOperationId("sampleOperationId1")
            .setOperationType("sampleOperationType1")
            .setOperationLastPersistentDate("2023-01-01")
            .build();

        PurgedPersistentIdentifier purgedIdentifier2 = new PurgedPersistentIdentifier.Builder()
            .setId("2")
            .setTenant(0)
            .setPersistentIdentifier(Collections.singletonList(new PersistentIdentifierModel()))
            .setVersion(0)
            .setType("sampleType2")
            .setObjectGroupId("sampleObjectGroupId2")
            .setOperationId("sampleOperationId2")
            .setOperationType("sampleOperationType2")
            .setOperationLastPersistentDate("2023-01-02")
            .build();

        List<PurgedPersistentIdentifier> purgedIdentifierList = Arrays.asList(purgedIdentifier1, purgedIdentifier2);

        List<Document> result = PurgedPersistentIdentifier.convertListToDocumentList(purgedIdentifierList);

        assertThat(result).isNotNull();
        assertThat(2).isEqualTo(result.size());
    }

    @Test
    public void test_fromDocument() throws Exception {
        Document persistentIdentifierModel = new Document();
        persistentIdentifierModel.put("PersistentIdentifierType", "ark");
        persistentIdentifierModel.put("PersistentIdentifierContent", "ark/2345/679934346673");
        persistentIdentifierModel.put("PersistentIdentifierReference", "reference");
        Document document = new Document();
        document.put("_id", "123");
        document.put("_tenant", 1);
        document.put("_v", 0);
        document.put("idObjectGroup", "778");
        document.put("persistentIdentifier", Arrays.asList(persistentIdentifierModel));
        document.put("type", "Unit");
        document.put("opId", "345");
        document.put("opType", "ELIMINATION_ACTION");
        document.put("opEndDate", "2023-11-28T21:21:11.485");

        final PurgedPersistentIdentifier purgedPersistentIdentifier = PurgedPersistentIdentifier.fromDocument(document);

        assertThat(purgedPersistentIdentifier).isNotNull();
    }
}

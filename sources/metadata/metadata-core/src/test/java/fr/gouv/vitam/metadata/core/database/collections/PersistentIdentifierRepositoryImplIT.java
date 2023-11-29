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
package fr.gouv.vitam.metadata.core.database.collections;

import com.google.common.collect.Lists;
import com.mongodb.client.MongoCollection;
import fr.gouv.vitam.common.database.server.elasticsearch.ElasticsearchNode;
import fr.gouv.vitam.common.database.server.mongodb.MongoDbAccess;
import fr.gouv.vitam.common.database.server.mongodb.VitamDocument;
import fr.gouv.vitam.common.elasticsearch.ElasticsearchRule;
import fr.gouv.vitam.common.guid.GUIDFactory;
import fr.gouv.vitam.common.model.unit.PersistentIdentifierModel;
import fr.gouv.vitam.common.mongo.MongoRule;
import fr.gouv.vitam.common.thread.RunWithCustomExecutor;
import fr.gouv.vitam.common.thread.RunWithCustomExecutorRule;
import fr.gouv.vitam.common.thread.VitamThreadPoolExecutor;
import fr.gouv.vitam.common.thread.VitamThreadUtils;
import fr.gouv.vitam.metadata.core.config.ElasticsearchMetadataIndexManager;
import fr.gouv.vitam.metadata.core.reconstruction.model.PurgedPersistentIdentifier;
import fr.gouv.vitam.metadata.core.reconstruction.repository.PersistentIdentifierRepository;
import fr.gouv.vitam.metadata.core.utils.MappingLoaderTestUtils;
import org.bson.Document;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static fr.gouv.vitam.common.database.server.mongodb.VitamDocument.VERSION;
import static fr.gouv.vitam.metadata.core.database.collections.MetadataCollections.OBJECTGROUP;
import static fr.gouv.vitam.metadata.core.database.collections.MetadataCollections.UNIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.groups.Tuple.tuple;

@RunWithCustomExecutor
public class PersistentIdentifierRepositoryImplIT {

    public static final String PURGED_PERSISTENT_IDENTIFIER_COLLECTION = "PurgedPersistentIdentifier";
    public static final int TENANT_ID = 0;
    public static final String PREFIX = GUIDFactory.newGUID().getId();
    static final List<Integer> tenantList = Arrays.asList(TENANT_ID);
    private static final ElasticsearchMetadataIndexManager metadataIndexManager = MetadataCollectionsTestUtils
        .createTestIndexManager(tenantList, Collections.emptyMap(), MappingLoaderTestUtils.getTestMappingLoader());
    @ClassRule
    public static RunWithCustomExecutorRule runInThread =
        new RunWithCustomExecutorRule(VitamThreadPoolExecutor.getDefaultExecutor());
    private static ElasticsearchAccessMetadata esClient;
    @Rule
    public MongoRule mongoRule =
        new MongoRule(MongoDbAccess.getMongoClientSettingsBuilder(Unit.class),
            PREFIX + PURGED_PERSISTENT_IDENTIFIER_COLLECTION);
    private PersistentIdentifierRepository persistentIdentifierRepository;

    @BeforeClass
    public static void setupOne() throws Exception {
        List<ElasticsearchNode> esNodes =
            org.assertj.core.util.Lists
                .newArrayList(new ElasticsearchNode(ElasticsearchRule.getHost(), ElasticsearchRule.getPort()));

        esClient = new ElasticsearchAccessMetadata(ElasticsearchRule.getClusterName(), esNodes,
            metadataIndexManager);
    }

    @Before
    public void setUp() {
        MongoDbAccessMetadataImpl mongoDbAccess =
            new MongoDbAccessMetadataImpl(mongoRule.getMongoClient(), mongoRule.getMongoDatabase().getName(), true,
                esClient, UNIT, OBJECTGROUP);
        persistentIdentifierRepository = new PersistentIdentifierRepositoryImpl(mongoDbAccess, PREFIX);
    }

    @Test
    @RunWithCustomExecutor
    public void test_insert() throws Exception {
        VitamThreadUtils.getVitamSession().setTenantId(0);

        // Given
        String id1 = GUIDFactory.newUnitGUID(TENANT_ID).toString();
        String id2 = GUIDFactory.newUnitGUID(TENANT_ID).toString();
        PurgedPersistentIdentifierDocument purgedPersistentIdentifierDocument1 = createPurgedPersistentIdentifier(id1);
        PurgedPersistentIdentifierDocument purgedPersistentIdentifierDocument2 = createPurgedPersistentIdentifier(id2);

        // When
        persistentIdentifierRepository
            .insert(Lists.newArrayList(purgedPersistentIdentifierDocument1, purgedPersistentIdentifierDocument2));

        // Then
        MongoCollection<Document> mongoCollection =
            mongoRule.getMongoCollection(PREFIX + PURGED_PERSISTENT_IDENTIFIER_COLLECTION);
        assertThat(mongoCollection.countDocuments()).isEqualTo(2);
        assertThat(mongoCollection.find())
            .extracting("_id", VitamDocument.TENANT_ID, VERSION)
            .containsExactlyInAnyOrder(tuple(id1, TENANT_ID, 0), tuple(id2, TENANT_ID, 0));
    }

    @Test
    @RunWithCustomExecutor
    public void should_ignore_duplicates_during_bulk_insert() throws Exception {

        VitamThreadUtils.getVitamSession().setTenantId(0);

        // Given
        String id1 = GUIDFactory.newUnitGUID(TENANT_ID).toString();
        String id2 = GUIDFactory.newUnitGUID(TENANT_ID).toString();
        String id3 = GUIDFactory.newUnitGUID(TENANT_ID).toString();
        PurgedPersistentIdentifierDocument purgedPersistentIdentifierDocument1 = createPurgedPersistentIdentifier(id1);
        PurgedPersistentIdentifierDocument purgedPersistentIdentifierDocument2 = createPurgedPersistentIdentifier(id2);
        PurgedPersistentIdentifierDocument purgedPersistentIdentifierDocument3 = createPurgedPersistentIdentifier(id3);


        // When
        persistentIdentifierRepository
            .insert(Lists.newArrayList(purgedPersistentIdentifierDocument1, purgedPersistentIdentifierDocument2));
        persistentIdentifierRepository
            .insert(Lists.newArrayList(purgedPersistentIdentifierDocument1, purgedPersistentIdentifierDocument2,
                purgedPersistentIdentifierDocument3));

        // Then
        MongoCollection<Document> mongoCollection =
            mongoRule.getMongoCollection(PREFIX + PURGED_PERSISTENT_IDENTIFIER_COLLECTION);
        assertThat(mongoCollection.countDocuments()).isEqualTo(3);
        assertThat(mongoCollection.find())
            .extracting("_id", VitamDocument.TENANT_ID, VERSION)
            .containsExactlyInAnyOrder(tuple(id1, TENANT_ID, 0), tuple(id2, TENANT_ID, 0), tuple(id3, TENANT_ID, 0));
    }

    @Test
    @RunWithCustomExecutor
    public void should_skip_when_store_two_document_with_same_id() {
        // Given
        String id1 = GUIDFactory.newUnitGUID(TENANT_ID).toString();
        String id3 = GUIDFactory.newUnitGUID(TENANT_ID).toString();
        PurgedPersistentIdentifierDocument purgedPersistentIdentifierDocument1 = createPurgedPersistentIdentifier(id1);
        PurgedPersistentIdentifierDocument purgedPersistentIdentifierDocument2 = createPurgedPersistentIdentifier(id1);
        PurgedPersistentIdentifierDocument purgedPersistentIdentifierDocument3 = createPurgedPersistentIdentifier(id3);

        // When
        assertThatCode(() -> persistentIdentifierRepository
            .insert(Lists.newArrayList(purgedPersistentIdentifierDocument1, purgedPersistentIdentifierDocument2,
                purgedPersistentIdentifierDocument3))
        )
            .doesNotThrowAnyException();
    }

    private PurgedPersistentIdentifierDocument createPurgedPersistentIdentifier(String id) {

        PersistentIdentifierModel persistentIdentifierModel = new PersistentIdentifierModel();
        persistentIdentifierModel.setPersistentIdentifierContent("1");
        persistentIdentifierModel.setPersistentIdentifierOrigin("2");
        persistentIdentifierModel.setPersistentIdentifierType("3");
        persistentIdentifierModel.setPersistentIdentifierReference("4");
        ArrayList<PersistentIdentifierModel> persistentIdentifierModels = Lists.newArrayList(persistentIdentifierModel);

        PurgedPersistentIdentifier purgedPersistentIdentifier = PurgedPersistentIdentifier.builder()
            .setId(id)
            .setTenant(0)
            .setPersistentIdentifier(persistentIdentifierModels)
            .setVersion(0)
            .setType("Object")
            .setOperationId("operationId")
            .setOperationType("EliminationAction")
            .build();
        return PurgedPersistentIdentifier.fromPurgedPersistentIdentifier(purgedPersistentIdentifier);
    }

}
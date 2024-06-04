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

package fr.gouv.vitam.common.database.server.elasticsearch;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonpDeserializer;
import co.elastic.clients.json.jackson.JacksonJsonProvider;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.model.FacetResult;
import jakarta.json.stream.JsonParser;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ElasticsearchFacetResultHelperTest.
 */
public class ElasticsearchFacetResultHelperTest {

    @Test
    public void shouldParseSTermsAggregate() throws IOException {
        SearchResponse<ObjectNode> objectNodeSearchResponse = loadResponseRessourceFile();
        Aggregate aggregate = objectNodeSearchResponse.aggregations().get("facet_desclevel");
        FacetResult facet = ElasticsearchFacetResultHelper.transformFromEsAggregation("facet_desclevel", aggregate);

        assertThat(facet.getName()).isEqualTo("facet_desclevel");
        assertThat(facet.getBuckets()).hasSize(2);
        assertThat(facet.getBuckets().get(0).getValue()).isEqualTo("Item");
        assertThat(facet.getBuckets().get(0).getCount()).isEqualTo(13L);
        assertThat(facet.getBuckets().get(1).getValue()).isEqualTo("RecordGrp");
        assertThat(facet.getBuckets().get(1).getCount()).isEqualTo(7L);
    }

    @Test
    public void shouldParseLTermsAggregate() throws IOException {
        SearchResponse<ObjectNode> objectNodeSearchResponse = loadResponseRessourceFile();
        Aggregate aggregate = objectNodeSearchResponse.aggregations().get("facet_versions");
        FacetResult facet = ElasticsearchFacetResultHelper.transformFromEsAggregation("facet_versions", aggregate);

        assertThat(facet.getName()).isEqualTo("facet_versions");
        assertThat(facet.getBuckets()).hasSize(2);
        assertThat(facet.getBuckets().get(0).getValue()).isEqualTo("0");
        assertThat(facet.getBuckets().get(0).getCount()).isEqualTo(20L);
        assertThat(facet.getBuckets().get(1).getValue()).isEqualTo("1");
        assertThat(facet.getBuckets().get(1).getCount()).isEqualTo(10L);
    }

    @Test
    public void shouldParseBooleanLTermsAggregate() throws IOException {
        SearchResponse<ObjectNode> objectNodeSearchResponse = loadResponseRessourceFile();
        Aggregate aggregate = objectNodeSearchResponse.aggregations().get("facet_PreventRearrangement");
        FacetResult facet = ElasticsearchFacetResultHelper.transformFromEsAggregation(
            "facet_PreventRearrangement",
            aggregate
        );

        assertThat(facet.getName()).isEqualTo("facet_PreventRearrangement");
        assertThat(facet.getBuckets()).hasSize(2);
        assertThat(facet.getBuckets().get(0).getValue()).isEqualTo("false");
        assertThat(facet.getBuckets().get(0).getCount()).isEqualTo(2L);
        assertThat(facet.getBuckets().get(1).getValue()).isEqualTo("true");
        assertThat(facet.getBuckets().get(1).getCount()).isEqualTo(1L);
    }

    @Test
    public void shouldParseDateRangeAggregate() throws IOException {
        SearchResponse<ObjectNode> objectNodeSearchResponse = loadResponseRessourceFile();
        Aggregate aggregate = objectNodeSearchResponse.aggregations().get("EndDate");
        FacetResult facet = ElasticsearchFacetResultHelper.transformFromEsAggregation("EndDate", aggregate);

        assertThat(facet.getName()).isEqualTo("EndDate");
        assertThat(facet.getBuckets()).hasSize(3);
        assertThat(facet.getBuckets().get(0).getValue()).isEqualTo("*-2007");
        assertThat(facet.getBuckets().get(0).getCount()).isEqualTo(10L);
        assertThat(facet.getBuckets().get(1).getValue()).isEqualTo("1900-*");
        assertThat(facet.getBuckets().get(1).getCount()).isEqualTo(17L);
        assertThat(facet.getBuckets().get(2).getValue()).isEqualTo("2010-2018");
        assertThat(facet.getBuckets().get(2).getCount()).isEqualTo(7L);
    }

    @Test
    public void shouldParseFilterAggregate() throws IOException {
        SearchResponse<ObjectNode> objectNodeSearchResponse = loadResponseRessourceFile();
        Aggregate aggregate = objectNodeSearchResponse.aggregations().get("facet_title_langs");
        FacetResult facet = ElasticsearchFacetResultHelper.transformFromEsAggregation("facet_title_langs", aggregate);

        assertThat(facet.getName()).isEqualTo("facet_title_langs");
        assertThat(facet.getBuckets()).hasSize(2);
        assertThat(facet.getBuckets().get(0).getValue()).isEqualTo("english_title");
        assertThat(facet.getBuckets().get(0).getCount()).isEqualTo(9L);
        assertThat(facet.getBuckets().get(1).getValue()).isEqualTo("french_title");
        assertThat(facet.getBuckets().get(1).getCount()).isEqualTo(9L);
    }

    private SearchResponse<ObjectNode> loadResponseRessourceFile() throws IOException {
        JsonParser jsonParser = JacksonJsonProvider.provider()
            .createParser(PropertiesUtils.getResourceAsStream("es_aggregate_response.json"));
        JsonpDeserializer<ObjectNode> deserializer = JacksonJsonpMapper.findDeserializer(ObjectNode.class);

        JsonpDeserializer<SearchResponse<ObjectNode>> searchResponseDeserializer =
            SearchResponse.createSearchResponseDeserializer(deserializer);
        return searchResponseDeserializer.deserialize(jsonParser, new JacksonJsonpMapper());
    }
}

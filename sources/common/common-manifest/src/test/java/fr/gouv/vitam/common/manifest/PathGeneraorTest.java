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
package fr.gouv.vitam.common.manifest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class PathGeneraorTest {

    private List<JsonNode> parseJsonToList(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);
        return Arrays.asList(mapper.convertValue(root, JsonNode[].class));
    }

    @Test
    public void testGeneratePath() throws Exception {
        // Préparation des données
        String json =
            "[{\"#id\":\"1\",\"Title\":\"titi\",\"#unitups\":[\"2\"]}," +
            "{\"#id\":\"2\",\"Title\":\"toto\",\"#unitups\":[\"3\"]}," +
            "{\"#id\":\"3\",\"Title\":\"root\",\"#unitups\":[]}]";

        List<JsonNode> items = parseJsonToList(json);

        ListMultimap<String, String> multimap = ArrayListMultimap.create();
        multimap.put("3", "2");
        multimap.put("2", "1");

        List<String> linkedUnits = Arrays.asList("1");
        Set<String> existingDirectoryNames = new HashSet<>();

        // Appel de la méthode generatePath
        String path = PathGenerator.generatePath(items, multimap, linkedUnits, existingDirectoryNames);

        // Vérification du résultat
        assertEquals("root/toto/titi", path);
    }

    @Test
    public void testGeneratePathWithMultipleParents() throws Exception {
        // Préparation des données
        String json =
            "[{\"#id\":\"1\",\"Title\":\"titi\",\"#unitups\":[\"2\"]}," +
            "{\"#id\":\"2\",\"Title\":\"toto\",\"#unitups\":[\"3\", \"4\"]}," +
            "{\"#id\":\"3\",\"Title\":\"root1\",\"#unitups\":[]}," +
            "{\"#id\":\"4\",\"Title\":\"root2\",\"#unitups\":[]}]";

        List<JsonNode> items = parseJsonToList(json);

        ListMultimap<String, String> multimap = ArrayListMultimap.create();
        multimap.put("3", "2");
        multimap.put("4", "2");
        multimap.put("2", "1");

        List<String> linkedUnits = Arrays.asList("1");
        Set<String> existingDirectoryNames = new HashSet<>();

        // Vérification de l'exception pour plusieurs parents
        Exception exception = assertThrows(Exception.class, () -> {
            PathGenerator.generatePath(items, multimap, linkedUnits, existingDirectoryNames);
        });

        assertEquals("The unit with ID 2 has multiple parents!", exception.getMessage());
    }

    @Test
    public void testGeneratePathWithExistingDirectoryName() throws Exception {
        // Préparation des données
        String json =
            "[{\"#id\":\"1\",\"Title\":\"titi\",\"#unitups\":[\"2\"]}," +
            "{\"#id\":\"2\",\"Title\":\"toto\",\"#unitups\":[\"3\"]}," +
            "{\"#id\":\"3\",\"Title\":\"root\",\"#unitups\":[]}]";

        List<JsonNode> items = parseJsonToList(json);

        ListMultimap<String, String> multimap = ArrayListMultimap.create();
        multimap.put("3", "2");
        multimap.put("2", "1");

        List<String> linkedUnits = Arrays.asList("1");
        Set<String> existingDirectoryNames = new HashSet<>(Arrays.asList("titi"));

        // Appel de la méthode generatePath
        String path = PathGenerator.generatePath(items, multimap, linkedUnits, existingDirectoryNames);

        // Vérification du résultat
        assertTrue(path.matches("root/toto/titi[0-9]*"));
    }

    @Test
    public void testGeneratePathWithLongDirectoryName() throws Exception {
        // Préparation des données
        String longTitle = "a".repeat(PathGenerator.FULL_DIRECTORY_NAME_SIZE_LIMIT + 10);
        String json =
            "[{\"#id\":\"1\",\"Title\":\"" +
            longTitle +
            "\",\"unitups\":[\"2\"]}," +
            "{\"#id\":\"2\",\"Title\":\"toto\",\"#unitups\":[\"3\"]}," +
            "{\"#id\":\"3\",\"Title\":\"root\",\"#unitups\":[]}]";

        List<JsonNode> items = parseJsonToList(json);

        ListMultimap<String, String> multimap = ArrayListMultimap.create();
        multimap.put("3", "2");
        multimap.put("2", "1");

        List<String> linkedUnits = Arrays.asList("1");
        Set<String> existingDirectoryNames = new HashSet<>();

        // Appel de la méthode generatePath
        String path = PathGenerator.generatePath(items, multimap, linkedUnits, existingDirectoryNames);

        // Vérification du résultat
        assertTrue(path.endsWith("root/toto/" + longTitle.substring(0, PathGenerator.FULL_DIRECTORY_NAME_SIZE_LIMIT)));
    }
}

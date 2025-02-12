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

package fr.gouv.vitam.collect.internal.core.transformers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitam.collect.internal.core.exceptions.CollectInvalidJsltTransformerException;
import fr.gouv.vitam.collect.internal.core.exceptions.CollectJsltTransformationFailedException;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import net.javacrumbs.jsonunit.JsonAssert;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JsltTransformerTest {

    @Test
    public void testValidJsltTemplate() {
        String validTemplate = "{ \"name\": .name, \"age\": .age }";
        assertThatCode(() -> JsltTransformer.validate(validTemplate)).doesNotThrowAnyException();
    }

    @Test
    public void testInvalidJsltTemplate() {
        String invalidTemplate = "{ \"name\": .name, \"age\": .age"; // Missing closing brace
        assertThatThrownBy(() -> JsltTransformer.validate(invalidTemplate))
            .isInstanceOf(CollectInvalidJsltTransformerException.class)
            .hasMessageStartingWith("Invalid JSLT template: Parse error: Encountered \"<EOF>\" at line 1, column 28.");
    }

    @Test
    public void testTransformOK()
        throws CollectInvalidJsltTransformerException, InvalidParseOperationException, CollectJsltTransformationFailedException {
        JsltTransformer jsltTransformer = new JsltTransformer(
            """
            {
              "Title": if (.Title != null) .Title + " - TRANSFORMED" else null,
              *: .
            }
            """
        );

        // When
        ObjectNode transformed = jsltTransformer.transform(
            JsonHandler.getFromString(
                """
                {
                    "Title": "My title",
                    "DescriptionLevel": "Item"
                }
                """
            )
        );

        // Then
        JsonAssert.assertJsonEquals(
            JsonHandler.getFromString(
                """
                {
                    "Title": "My title - TRANSFORMED",
                    "DescriptionLevel": "Item"
                }
                """
            ),
            transformed
        );
    }

    @Test
    public void testTransformPreserveNull()
        throws CollectInvalidJsltTransformerException, InvalidParseOperationException, CollectJsltTransformationFailedException {
        JsltTransformer jsltTransformer = new JsltTransformer(
            """
            {
              "Title": if (.Title != null) .Title + " - TRANSFORMED" else null,
              *: .
            }
            """
        );

        // When
        ObjectNode transformed = jsltTransformer.transform(
            JsonHandler.getFromString(
                """
                {
                    "Title": null,
                    "DescriptionLevel": "Item"
                }
                """
            )
        );

        // Then
        JsonAssert.assertJsonEquals(
            JsonHandler.getFromString(
                """
                {
                    "Title": null,
                    "DescriptionLevel": "Item"
                }
                """
            ),
            transformed
        );
    }

    @Test
    public void testTransformKO() throws CollectInvalidJsltTransformerException {
        JsltTransformer jsltTransformer = new JsltTransformer(
            """
            {
              "Title": $unknown,
              *: .
            }
            """
        );

        // When / Then
        assertThatThrownBy(() ->
            jsltTransformer.transform(
                JsonHandler.getFromString(
                    """
                    {
                        "Title": "My title",
                        "DescriptionLevel": "Item"
                    }
                    """
                )
            ))
            .isInstanceOf(CollectJsltTransformationFailedException.class)
            .hasMessageContaining("No such variable 'unknown'");
    }
}

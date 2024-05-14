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
package fr.gouv.vitam.worker.core.utils;

import fr.gouv.vitam.common.model.administration.DataObjectVersionType;
import fr.gouv.vitam.common.model.dip.DataObjectVersions;
import fr.gouv.vitam.common.model.dip.QualifierVersion;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static fr.gouv.vitam.common.model.administration.DataObjectVersionType.BINARY_MASTER;
import static fr.gouv.vitam.common.model.administration.DataObjectVersionType.DISSEMINATION;
import static fr.gouv.vitam.common.model.administration.DataObjectVersionType.PHYSICAL_MASTER;
import static fr.gouv.vitam.common.model.administration.DataObjectVersionType.TEXT_CONTENT;
import static fr.gouv.vitam.common.model.administration.DataObjectVersionType.THUMBNAIL;
import static fr.gouv.vitam.common.model.dip.QualifierVersion.LAST;
import static fr.gouv.vitam.worker.core.utils.DataObjectVersionToPatternsConvertor.computeDataObjectVersionsPatterns;
import static org.assertj.core.api.Assertions.assertThat;

public class DataObjectVersionToPatternsConvertorTest {

    @Test
    public void should_default_to_map_of_all_object_types_with_LAST_value() {
        assertThat(computeDataObjectVersionsPatterns(new DataObjectVersions())).isEqualTo(
            Map.of(
                BINARY_MASTER,
                Set.of(LAST),
                DISSEMINATION,
                Set.of(LAST),
                PHYSICAL_MASTER,
                Set.of(LAST),
                TEXT_CONTENT,
                Set.of(LAST),
                THUMBNAIL,
                Set.of(LAST)
            )
        );
    }

    @Test
    public void should_convert_empty_set_to_map_of_all_object_types_with_LAST_value() {
        assertThat(computeDataObjectVersionsPatterns(new DataObjectVersions(Set.of()))).isEqualTo(
            Map.of(
                BINARY_MASTER,
                Set.of(LAST),
                DISSEMINATION,
                Set.of(LAST),
                PHYSICAL_MASTER,
                Set.of(LAST),
                TEXT_CONTENT,
                Set.of(LAST),
                THUMBNAIL,
                Set.of(LAST)
            )
        );
    }

    @Test
    public void should_return_map_as_is() {
        final Map<DataObjectVersionType, Set<QualifierVersion>> map = Map.of();
        assertThat(computeDataObjectVersionsPatterns(new DataObjectVersions(map))).isSameAs(map);
    }

    @Test
    public void should_convert_set_to_map_with_LAST_value() {
        assertThat(
            computeDataObjectVersionsPatterns(new DataObjectVersions(Set.of(BINARY_MASTER.getName())))
        ).isEqualTo(Collections.singletonMap(BINARY_MASTER, Set.of(LAST)));

        assertThat(
            computeDataObjectVersionsPatterns(
                new DataObjectVersions(Set.of(BINARY_MASTER.getName(), THUMBNAIL.getName()))
            )
        ).isEqualTo(Map.of(BINARY_MASTER, Set.of(LAST), THUMBNAIL, Set.of(LAST)));

        assertThat(
            computeDataObjectVersionsPatterns(
                new DataObjectVersions(Set.of(BINARY_MASTER.getName(), THUMBNAIL.getName(), TEXT_CONTENT.getName()))
            )
        ).isEqualTo(Map.of(BINARY_MASTER, Set.of(LAST), THUMBNAIL, Set.of(LAST), TEXT_CONTENT, Set.of(LAST)));
    }
}

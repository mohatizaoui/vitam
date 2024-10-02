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

package fr.gouv.vitam.common.manifest.naming;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OriginalFilenameResolverTest {

    private static final String GUID1 = "aeaqaaaaaaec3hn6abc5gamr7y4om3aaaaaq";
    private static final String GUID2 = "aeeaaaaaacec3hn6ab74mamr7y4ntuiaaaaq";
    private static final String GUID3 = "aebqaaaaacec3hn6abc5gamr7y4omzyaaaba";
    private static final String GUID4 = "aebaaaaaaaec3hn6abc5gamr7y4omzyaaaaq";
    private static final String GUID5 = "aeaqaaaaaaec3hn6abc5gamr7y4onfyaaaaq";
    private static final String GUID6 = "aeaqaaaaaaec3hn6abc5gamr7y4om3aaaaaq";
    private static final String GUID7 = "aedqaaaaacec3hn6abc5gamr7y4onxiaaaca";
    private static final String GUID8 = "aeeaaaaaacec3hn6ab74mamr7y4ntuiaaaaq";
    private static final String GUID9 = "aedqaaaaacec3hn6abc5gamr7y4onxqaaaaq";
    private static final String GUID10 = "aedqaaaaacec3hn6abc5gamr7y4ovwyaaaaq";
    private static final String GUID11 = "aeeaaaaaakec2x2zabui2amsikhynmyaaaaq";
    private static final String EMPTY_PATH = "";
    private static final String PARENT_PATH = "parent";
    private static final String URI = "Content/contract.pdf";

    @Test
    public void testResolveSimpleWithoutBasePath() {
        OriginalFilenameResolver resolver = new OriginalFilenameResolver();
        assertThat(resolver.resolve(EMPTY_PATH, GUID1, URI, "filename.docx")).isEqualTo("Content/filename.docx");
    }

    @Test
    public void testResolveSimpleWithoutBasePathNoExtension() {
        OriginalFilenameResolver resolver = new OriginalFilenameResolver();
        assertThat(resolver.resolve(EMPTY_PATH, GUID1, URI, "filename")).isEqualTo("Content/filename");
    }

    @Test
    public void testResolveLongFileNameWithExtensionWithoutBasePath() {
        OriginalFilenameResolver resolver = new OriginalFilenameResolver();
        assertThat(
            resolver.resolve(
                EMPTY_PATH,
                GUID1,
                URI,
                "very_long_filename_to_ensure_we_go_over_the_character_limit_for_most_filesystems_very_long_filename_to_ensure_we_go_over_the_character_limit_for_most_filesystems.docx"
            )
        ).isEqualTo(
            "Content/very_long_filename_to_ensure_we_go_over_the_character_limit_for_most_filesystems_very_long_filename_to_ensur_aeaqaaaaaaec3hn6abc5gamr7y4om3aaaaaq.docx"
        );
    }

    @Test
    public void testResolveSimpleWithBasePath() {
        OriginalFilenameResolver resolver = new OriginalFilenameResolver();
        assertThat(resolver.resolve(PARENT_PATH, GUID1, URI, "filename.docx")).isEqualTo(
            "Content/parent/filename.docx"
        );
    }

    @Test
    public void testResolveSimpleWithBasePathWithoutExtension() {
        OriginalFilenameResolver resolver = new OriginalFilenameResolver();
        assertThat(resolver.resolve(PARENT_PATH, GUID1, URI, "filename")).isEqualTo("Content/parent/filename");
    }

    @Test
    public void testResolveLongFileNameWithExtensionAndBasePath() {
        OriginalFilenameResolver resolver = new OriginalFilenameResolver();
        assertThat(
            resolver.resolve(
                PARENT_PATH,
                GUID1,
                URI,
                "very_long_filename_to_ensure_we_go_over_the_character_limit_for_most_filesystems_very_long_filename_to_ensure_we_go_over_the_character_limit_for_most_filesystems.docx"
            )
        ).isEqualTo(
            "Content/parent/very_long_filename_to_ensure_we_go_over_the_character_limit_for_most_filesystems_very_long_filename_to_ensur_aeaqaaaaaaec3hn6abc5gamr7y4om3aaaaaq.docx"
        );
    }

    @Test
    public void testResolveLongFileNameWithoutExtensionWithBasePath() {
        OriginalFilenameResolver resolver = new OriginalFilenameResolver();
        assertThat(
            resolver.resolve(
                PARENT_PATH,
                GUID1,
                URI,
                "very_long_filename_to_ensure_we_go_over_the_character_limit_for_most_filesystems_very_long_filename_to_ensure_we_go_over_the_character_limit_for_most_filesystems"
            )
        ).isEqualTo(
            "Content/parent/very_long_filename_to_ensure_we_go_over_the_character_limit_for_most_filesystems_very_long_filename_to_ensure_we__aeaqaaaaaaec3hn6abc5gamr7y4om3aaaaaq"
        );
    }

    @Test
    public void testResolveNullFileNameAndUriWithExtensionThenFallbackToGuidResolver() {
        OriginalFilenameResolver resolver = new OriginalFilenameResolver();
        assertThat(resolver.resolve(PARENT_PATH, GUID1, URI, null)).isEqualTo("Content/parent/" + GUID1 + ".pdf");
    }

    @Test
    public void testResolveNullFileNameAndUriWithoutExtensionThenFallbackToGuidResolver() {
        OriginalFilenameResolver resolver = new OriginalFilenameResolver();
        assertThat(resolver.resolve(PARENT_PATH, GUID1, "contract", null)).isEqualTo("Content/parent/" + GUID1);
    }

    @Test
    public void testResolveNonAscii() {
        OriginalFilenameResolver resolver = new OriginalFilenameResolver();
        assertThat(resolver.resolve(EMPTY_PATH, GUID1, URI, ".àẑèrẗÿ~2.docx")).isEqualTo("Content/azerty_2.docx");
        assertThat(resolver.resolve(PARENT_PATH, GUID2, URI, "عقد-1.docx")).isEqualTo("Content/parent/___-1.docx");
    }

    @Test
    public void testResolveComplex() {
        OriginalFilenameResolver resolver = new OriginalFilenameResolver();

        // First file ==> no conflict
        assertThat(resolver.resolve(EMPTY_PATH, GUID1, URI, "file1.docx")).isEqualTo("Content/file1.docx");
        // Another file ==> no conflict
        assertThat(resolver.resolve(EMPTY_PATH, GUID2, URI, "file2.docx")).isEqualTo("Content/file2.docx");
        // Same file ==> suffix with _guid & ext
        assertThat(resolver.resolve(EMPTY_PATH, GUID3, URI, "file1.docx")).isEqualTo(
            "Content/file1_aebqaaaaacec3hn6abc5gamr7y4omzyaaaba.docx"
        );
        // Same file in another folder ==> no conflict
        assertThat(resolver.resolve(PARENT_PATH, GUID4, URI, "file1.docx")).isEqualTo("Content/parent/file1.docx");
        // Same file in same folder ==> suffix with _guid & ext
        assertThat(resolver.resolve(PARENT_PATH, GUID5, URI, "file1.docx")).isEqualTo(
            "Content/parent/file1_aeaqaaaaaaec3hn6abc5gamr7y4onfyaaaaq.docx"
        );
        // Some long file name ==> truncate / suffix with _guid & ext
        assertThat(
            resolver.resolve(
                PARENT_PATH,
                GUID6,
                URI,
                "very_long_filename_to_ensure_we_go_over_the_character_limit_for_most_filesystems_very_long_filename_to_ensure_we_go_over_the_character_limit_for_most_filesystems.docx"
            )
        ).isEqualTo(
            "Content/parent/very_long_filename_to_ensure_we_go_over_the_character_limit_for_most_filesystems_very_long_filename_to_ensur_aeaqaaaaaaec3hn6abc5gamr7y4om3aaaaaq.docx"
        );
        // Existing long file name ==> truncate / suffix with _guid & ext
        assertThat(
            resolver.resolve(
                PARENT_PATH,
                GUID7,
                URI,
                "very_long_filename_to_ensure_we_go_over_the_character_limit_for_most_filesystems_very_long_filename_to_ensure_we_go_over_the_character_limit_for_most_filesystems.docx"
            )
        ).isEqualTo(
            "Content/parent/very_long_filename_to_ensure_we_go_over_the_character_limit_for_most_filesystems_very_long_filename_to_ensur_aedqaaaaacec3hn6abc5gamr7y4onxiaaaca.docx"
        );
        // Some long file name without extension ==> truncate / suffix with _guid
        assertThat(
            resolver.resolve(
                PARENT_PATH,
                GUID8,
                URI,
                "very_long_filename_to_ensure_we_go_over_the_character_limit_for_most_filesystems_very_long_filename_to_ensure_we_go_over_the_character_limit_for_most_filesystems"
            )
        ).isEqualTo(
            "Content/parent/very_long_filename_to_ensure_we_go_over_the_character_limit_for_most_filesystems_very_long_filename_to_ensure_we__aeeaaaaaacec3hn6ab74mamr7y4ntuiaaaaq"
        );
        // Some almost identical long file name without extension ==> truncate / suffix with _guid
        assertThat(
            resolver.resolve(
                PARENT_PATH,
                GUID9,
                URI,
                "very_long_filename_to_ensure_we_go_over_the_character_limit_for_most_filesystems_very_long_filename_to_ensure_we_go_over_the_character_limit_for_most_filesystems2"
            )
        ).isEqualTo(
            "Content/parent/very_long_filename_to_ensure_we_go_over_the_character_limit_for_most_filesystems_very_long_filename_to_ensure_we__aedqaaaaacec3hn6abc5gamr7y4onxqaaaaq"
        );

        // Null filename ==> fall back to Guid resolver
        assertThat(resolver.resolve(PARENT_PATH, GUID10, URI, null)).isEqualTo(
            "Content/parent/aedqaaaaacec3hn6abc5gamr7y4ovwyaaaaq.pdf"
        );

        // Duplicate over guid
        assertThat(resolver.resolve(PARENT_PATH, GUID11, URI, "aedqaaaaacec3hn6abc5gamr7y4ovwyaaaaq.pdf")).isEqualTo(
            "Content/parent/aedqaaaaacec3hn6abc5gamr7y4ovwyaaaaq_aeeaaaaaakec2x2zabui2amsikhynmyaaaaq.pdf"
        );
    }

    @Test
    public void testResolveIllegalDuplicateExactFilenameWithSameObjectId() {
        OriginalFilenameResolver resolver = new OriginalFilenameResolver();

        // First long file ==> truncated
        assertThat(
            resolver.resolve(
                PARENT_PATH,
                GUID1,
                URI,
                "very_long_filename_to_ensure_we_go_over_the_character_limit_for_most_filesystems_very_long_filename_to_ensure_we_go_over_the_character_limit_for_most_filesystems.docx"
            )
        ).isEqualTo(
            "Content/parent/very_long_filename_to_ensure_we_go_over_the_character_limit_for_most_filesystems_very_long_filename_to_ensur_aeaqaaaaaaec3hn6abc5gamr7y4om3aaaaaq.docx"
        );

        // Second file with exact same truncated/suffixed name ==> KO
        assertThatThrownBy(
            () ->
                resolver.resolve(
                    PARENT_PATH,
                    GUID1,
                    URI,
                    "very_long_filename_to_ensure_we_go_over_the_character_limit_for_most_filesystems_very_long_filename_to_ensure_we_go_over_the_character_limit_for_most_filesystems.docx"
                )
        ).isInstanceOf(IllegalStateException.class);
    }
}

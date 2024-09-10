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
package fr.gouv.vitam.functional.administration.core.profile;

import fr.gouv.vitam.common.model.administration.ProfileSedaVersion;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ProfileSax2Handler extends DefaultHandler {

    private ProfileSedaVersion sedaVersion = null;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        final boolean isGrammarNode = qName.contains("grammar");
        final boolean isSchemaNode = qName.contains("schema");
        if (!(isGrammarNode || isSchemaNode)) {
            return;
        }

        final String sedaVersion = extractVersion(
            attributes.getValue("xmlns"),
            attributes.getValue("xmlns:seda"),
            attributes.getValue("ns")
        );

        if (sedaVersion != null) this.sedaVersion = ProfileSedaVersion.forVersion(sedaVersion);
    }

    private String extractVersion(String... namespaces) {
        for (String namespace : namespaces) {
            if (namespace != null) {
                String[] parts = namespace.split(":");
                if (parts.length > 0) {
                    return parts[parts.length - 1].substring(1);
                }
            }
        }
        return null;
    }

    public ProfileSedaVersion getSedaVersion() {
        return this.sedaVersion;
    }
}

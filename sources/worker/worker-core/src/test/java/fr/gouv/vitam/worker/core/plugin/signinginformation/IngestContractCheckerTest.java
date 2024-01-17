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
package fr.gouv.vitam.worker.core.plugin.signinginformation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.administration.IngestContractModel;
import fr.gouv.vitam.common.model.administration.SignaturePolicy;
import fr.gouv.vitam.worker.core.plugin.signingInformation.IngestContractChecker;
import fr.gouv.vitam.worker.core.plugin.signingInformation.SigningInformationEnum;
import fr.gouv.vitam.worker.core.plugin.signingInformation.exception.SigningInformationException;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;


public class IngestContractCheckerTest {


    private JsonNode archiveUnit;

    public IngestContractCheckerTest() {
    }

    @Before
    public void initData() throws FileNotFoundException, InvalidParseOperationException {
        this.archiveUnit = JsonHandler
            .getFromFile(PropertiesUtils.getResourceFile("SigningInformation/archiveUnitForSigningInformation.json"));
    }

    @Test
    public void testMandatorySignedDocument()  {
        JsonNode archiveUnit = modifyArchiveUnit(false, false, false, false, true);
        IngestContractModel ingestContractModel =
            createSampleIngestContractModel(SignaturePolicy.SignedDocumentPolicyEnum.MANDATORY, true, false, false);
        IngestContractChecker ingestContractChecker = new IngestContractChecker(archiveUnit, ingestContractModel);
        SigningInformationException exception =
            assertThrows(SigningInformationException.class, ingestContractChecker::check);
        assertEquals(SigningInformationEnum.MANDATORY_SIGNED_DOCUMENT.name(), exception.getErrorCode());

    }

    @Test
    public void testMandatorySignedDocumentWithoutSignedDocument() {
        JsonNode archiveUnit = modifyArchiveUnit(false, false, false, false, false);
        IngestContractModel ingestContractModel =
            createSampleIngestContractModel(SignaturePolicy.SignedDocumentPolicyEnum.MANDATORY, true, false, false);
        IngestContractChecker ingestContractChecker = new IngestContractChecker(archiveUnit, ingestContractModel);
        SigningInformationException exception =
            assertThrows(SigningInformationException.class, ingestContractChecker::check);
        assertEquals(SigningInformationEnum.MANDATORY_SIGNED_DOCUMENT.name(), exception.getErrorCode());

    }

    @Test
    public void testForbiddenSignedDocument() {
        JsonNode archiveUnit = modifyArchiveUnit(true, false, false, false, true);
        IngestContractModel ingestContractModel =
            createSampleIngestContractModel(SignaturePolicy.SignedDocumentPolicyEnum.FORBIDDEN, false, false, false);
        IngestContractChecker ingestContractChecker = new IngestContractChecker(archiveUnit, ingestContractModel);
        assertThrows(SigningInformationException.class, ingestContractChecker::check);
        SigningInformationException exception =
            assertThrows(SigningInformationException.class, ingestContractChecker::check);
        assertEquals(SigningInformationEnum.FORBIDDEN_SIGNED_DOCUMENT.name(), exception.getErrorCode());

    }

    @Test
    public void testDeclaredFieldsMandatory()
        throws SigningInformationException {
        JsonNode archiveUnit = modifyArchiveUnit(true, true, true, true, true);
        IngestContractModel ingestContractModel =
            createSampleIngestContractModel(SignaturePolicy.SignedDocumentPolicyEnum.MANDATORY, true, true, false);
        IngestContractChecker ingestContractChecker = new IngestContractChecker(archiveUnit, ingestContractModel);
        ingestContractChecker.check();
    }

    @Test
    public void testDeclaredFieldsMandatoryWithoutSignedDocument()
        throws SigningInformationException {
        JsonNode archiveUnit = modifyArchiveUnit(true, false, true, true, false);
        IngestContractModel ingestContractModel =
            createSampleIngestContractModel(SignaturePolicy.SignedDocumentPolicyEnum.MANDATORY, true, true, false);
        IngestContractChecker ingestContractChecker = new IngestContractChecker(archiveUnit, ingestContractModel);
        ingestContractChecker.check();
    }


    @Test
    public void testDeclaredFieldsForbidden(){
        JsonNode archiveUnit = modifyArchiveUnit(true, true, false, false, true);
        IngestContractModel ingestContractModel =
            createSampleIngestContractModel(SignaturePolicy.SignedDocumentPolicyEnum.FORBIDDEN, true, false, false);
        IngestContractChecker ingestContractChecker = new IngestContractChecker(archiveUnit, ingestContractModel);
        SigningInformationException exception =
            assertThrows(SigningInformationException.class, ingestContractChecker::check);
        assertEquals(SigningInformationEnum.FORBIDDEN_SIGNED_DOCUMENT.name(), exception.getErrorCode());

    }

    @Test
    public void testMissingDeclaredSignature()  {
        JsonNode archiveUnit = modifyArchiveUnit(true, false, true, true, true);
        IngestContractModel ingestContractModel =
            createSampleIngestContractModel(SignaturePolicy.SignedDocumentPolicyEnum.MANDATORY, true, true, false);
        IngestContractChecker ingestContractChecker = new IngestContractChecker(archiveUnit, ingestContractModel);
        SigningInformationException exception =
            assertThrows(SigningInformationException.class, ingestContractChecker::check);
        assertEquals(SigningInformationEnum.MISSING_DECLARED_SIGNATURE.name(), exception.getErrorCode());

    }

    @Test
    public void testMissingDeclaredTimestamp() {
        JsonNode archiveUnit = modifyArchiveUnit(true, true, false, true, true);
        IngestContractModel ingestContractModel =
            createSampleIngestContractModel(SignaturePolicy.SignedDocumentPolicyEnum.MANDATORY, true, true, false);
        IngestContractChecker ingestContractChecker = new IngestContractChecker(archiveUnit, ingestContractModel);
        SigningInformationException exception =
            assertThrows(SigningInformationException.class, ingestContractChecker::check);
        assertEquals(SigningInformationEnum.MISSING_DECLARED_TIMESTAMP.name(), exception.getErrorCode());

    }

    @Test
    public void testMissingDeclaredAdditionalProof() {

        JsonNode archiveUnit = modifyArchiveUnit(true, true, true, false, true);
        IngestContractModel ingestContractModel =
            createSampleIngestContractModel(SignaturePolicy.SignedDocumentPolicyEnum.MANDATORY, true, true, true);
        IngestContractChecker ingestContractChecker = new IngestContractChecker(archiveUnit, ingestContractModel);
        SigningInformationException exception =
            assertThrows(SigningInformationException.class, ingestContractChecker::check);
        assertEquals(SigningInformationEnum.MISSING_DECLARED_ADDITIONAL_PROOF.name(), exception.getErrorCode());

    }

    // Add more test cases as needed


    private JsonNode modifyArchiveUnit(boolean hasSigningInformation, boolean hasSignature, boolean hasTimestamp,
        boolean hasAdditionalProof, boolean hasSignedDocument) {

        JsonNode archiveUnit = this.archiveUnit.deepCopy();
        ObjectNode signingInformationNode = (ObjectNode) archiveUnit.path("ArchiveUnit").path("SigningInformation");

        if (hasSigningInformation) {
            // Add or modify fields as needed
            ArrayNode signingRole = signingInformationNode.putArray("SigningRole");
            ArrayNode detachedSigningRole = signingInformationNode.putArray("DetachedSigningRole");
            if (hasSignedDocument) {
                signingRole.add("SignedDocument");
            }
            if (hasTimestamp) {
                signingRole.add("Timestamp");
            }
            if (hasSignature) {
                detachedSigningRole.add("Signature");
            }
            if (hasAdditionalProof) {
                signingRole.add("AdditionalProof");
            }


        } else {
            // Remove the SigningInformation node
            //signingInformationNode.removeAll();
            ((ObjectNode) archiveUnit.path("ArchiveUnit")).remove("SigningInformation");
            return archiveUnit;
        }


        return archiveUnit;
    }



    private IngestContractModel createSampleIngestContractModel(
        SignaturePolicy.SignedDocumentPolicyEnum signedDocumentPolicy,
        boolean isDeclaredSignature, boolean isDeclaredTimestamp, boolean isDeclaredAdditionalProof) {
        IngestContractModel ingestContractModel = new IngestContractModel();
        SignaturePolicy signaturePolicy = new SignaturePolicy();
        signaturePolicy
            .setSignedDocument(signedDocumentPolicy);
        signaturePolicy.setDeclaredSignature(isDeclaredSignature);
        signaturePolicy.setDeclaredTimestamp(isDeclaredTimestamp);

        signaturePolicy.setDeclaredAdditionalProof(isDeclaredAdditionalProof);
        ingestContractModel.setSignaturePolicy(signaturePolicy);
        return ingestContractModel;
    }

    @Test
    public void testDeclaredFieldsAllowedWithoutSigningInformation() throws SigningInformationException {
        JsonNode archiveUnit = modifyArchiveUnit(false, false, false, false, false);
        IngestContractModel ingestContractModel = createSampleIngestContractModel(
            SignaturePolicy.SignedDocumentPolicyEnum.ALLOWED, true, false, false);
        IngestContractChecker ingestContractChecker = new IngestContractChecker(archiveUnit, ingestContractModel);
        ingestContractChecker.check();

    }


    @Test
    public void testDeclaredFieldsAllowedWithoutSignedDocument() throws SigningInformationException {
        JsonNode archiveUnit = modifyArchiveUnit(true, false, false, false, false);
        IngestContractModel ingestContractModel = createSampleIngestContractModel(
            SignaturePolicy.SignedDocumentPolicyEnum.ALLOWED, true, false, false);
        IngestContractChecker ingestContractChecker = new IngestContractChecker(archiveUnit, ingestContractModel);
        ingestContractChecker.check();

    }

    @Test
    public void testDeclaredFieldsAllowedWithSigningInformation() throws SigningInformationException {
        JsonNode archiveUnit = this.archiveUnit.deepCopy();
        ObjectNode signingInformationNode = (ObjectNode) archiveUnit.path("ArchiveUnit").path("SigningInformation");

        ArrayNode signingRole = signingInformationNode.putArray("SigningRole");

        signingInformationNode.remove("DetachedSigningRole");
        signingRole.add("SignedDocument");
        signingRole.add("Signature");
        IngestContractModel ingestContractModel = createSampleIngestContractModel(
            SignaturePolicy.SignedDocumentPolicyEnum.ALLOWED, true, false, false);
        IngestContractChecker ingestContractChecker = new IngestContractChecker(archiveUnit, ingestContractModel);
        ingestContractChecker.check();

    }

}

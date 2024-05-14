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
package fr.gouv.vitam.worker.core.plugin.signingInformation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import fr.gouv.vitam.common.model.administration.IngestContractModel;
import fr.gouv.vitam.common.model.administration.SignaturePolicy;
import fr.gouv.vitam.worker.core.plugin.signingInformation.exception.SigningInformationException;

public class IngestContractChecker {

    private static final String TAG_ARCHIVE_UNIT = "ArchiveUnit";
    private static final String TAG_SIGNING_INFORMATION = "SigningInformation";
    private static final String PATH_SIGNING_INFORMATION = TAG_ARCHIVE_UNIT + "." + TAG_SIGNING_INFORMATION;

    private static final String TAG_SIGNING_ROLE = "SigningRole";
    private static final String TAG_DETACHED_SIGNING_ROLE = "DetachedSigningRole";

    private static final String SIGNATURE = "Signature";
    private static final String TIMESTAMP = "Timestamp";
    private static final String ADDITIONAL_PROOF = "AdditionalProof";
    private static final String SIGNED_DOCUMENT = "SignedDocument";

    private JsonNode archiveUnit;
    private IngestContractModel ingestContractModel;

    public IngestContractChecker(JsonNode archiveUnit, IngestContractModel ingestContractModel) {
        this.archiveUnit = archiveUnit;
        this.ingestContractModel = ingestContractModel;
    }

    public void check() throws SigningInformationException {
        SignaturePolicy signaturePolicy = ingestContractModel.getSignaturePolicy();
        if (signaturePolicy != null && signaturePolicy.getSignedDocument() != null) {
            validateSignedDocumentPolicy(signaturePolicy);
        }
    }

    private void validateSignedDocumentPolicy(SignaturePolicy signaturePolicy) throws SigningInformationException {
        SignaturePolicy.SignedDocumentPolicyEnum signedDocumentPolicy = signaturePolicy.getSignedDocument();

        switch (signedDocumentPolicy) {
            case MANDATORY:
                validateMandatorySignedDocument();
                if (hasSigningInformation() && verifySigningRoleValue(SIGNED_DOCUMENT)) {
                    validateDeclaredFields(signaturePolicy);
                }
                break;
            case FORBIDDEN:
                validateForbiddenSignedDocument();
                break;
            case ALLOWED:
                if (hasSigningInformation() && verifySigningRoleValue(SIGNED_DOCUMENT)) {
                    validateDeclaredFields(signaturePolicy);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown SignedDocument policy");
        }
    }

    private void validateMandatorySignedDocument() throws SigningInformationException {
        if (!hasSigningInformation()) {
            throw new SigningInformationException(SigningInformationEnum.MANDATORY_SIGNED_DOCUMENT);
        }
    }

    private void validateForbiddenSignedDocument() throws SigningInformationException {
        if (hasSigningInformation()) {
            throw new SigningInformationException(SigningInformationEnum.FORBIDDEN_SIGNED_DOCUMENT);
        }
    }

    private boolean hasSigningInformation() {
        JsonNode signingInformationNode = archiveUnit.path(TAG_ARCHIVE_UNIT).path(TAG_SIGNING_INFORMATION);
        return !signingInformationNode.isMissingNode();
    }

    private void validateDeclaredFields(SignaturePolicy signaturePolicy) throws SigningInformationException {
        validateFieldIfRequired(
            signaturePolicy.isDeclaredSignature(),
            SigningInformationEnum.MISSING_DECLARED_SIGNATURE,
            SIGNATURE
        );
        validateFieldIfRequired(
            signaturePolicy.isDeclaredTimestamp(),
            SigningInformationEnum.MISSING_DECLARED_TIMESTAMP,
            TIMESTAMP
        );
        validateFieldIfRequired(
            signaturePolicy.isDeclaredAdditionalProof(),
            SigningInformationEnum.MISSING_DECLARED_ADDITIONAL_PROOF,
            ADDITIONAL_PROOF
        );
    }

    private void validateFieldIfRequired(
        Boolean isDeclared,
        SigningInformationEnum signingInformationEnum,
        String value
    ) throws SigningInformationException {
        if (Boolean.TRUE.equals(isDeclared) && !hasSigningRoleAttributeWithValue(value)) {
            throw new SigningInformationException(signingInformationEnum);
        }
    }

    private boolean verifySigningRoleValue(String value) {
        JsonNode signingInformationNode = archiveUnit.path(TAG_ARCHIVE_UNIT).path(TAG_SIGNING_INFORMATION);
        ArrayNode signingRoleNode = (ArrayNode) signingInformationNode.path(TAG_SIGNING_ROLE);
        return hasValue(signingRoleNode, value);
    }

    private boolean hasSigningRoleAttributeWithValue(String value) {
        JsonNode signingInformationNode = archiveUnit.path(TAG_ARCHIVE_UNIT).path(TAG_SIGNING_INFORMATION);

        JsonNode detachedSigningRoleNode = signingInformationNode.path(TAG_DETACHED_SIGNING_ROLE);
        return (
            verifySigningRoleValue(value) ||
            (!detachedSigningRoleNode.isMissingNode() && hasValue((ArrayNode) detachedSigningRoleNode, value))
        );
    }

    private boolean hasValue(ArrayNode arrayNode, String targetValue) {
        for (JsonNode node : arrayNode) {
            if (node.isTextual() && targetValue.equals(node.asText())) {
                return true;
            }
        }
        return false;
    }
}

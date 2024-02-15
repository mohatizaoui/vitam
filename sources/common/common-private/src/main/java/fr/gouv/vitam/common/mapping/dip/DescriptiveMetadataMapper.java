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
package fr.gouv.vitam.common.mapping.dip;

import fr.gouv.culture.archivesdefrance.seda.v2.AdditionalProofType;
import fr.gouv.culture.archivesdefrance.seda.v2.CodeKeywordType;
import fr.gouv.culture.archivesdefrance.seda.v2.CoverageType;
import fr.gouv.culture.archivesdefrance.seda.v2.CustodialHistoryType;
import fr.gouv.culture.archivesdefrance.seda.v2.DescriptiveMetadataContentType;
import fr.gouv.culture.archivesdefrance.seda.v2.DetachedSigningRoleType;
import fr.gouv.culture.archivesdefrance.seda.v2.EventType;
import fr.gouv.culture.archivesdefrance.seda.v2.ExtendedType;
import fr.gouv.culture.archivesdefrance.seda.v2.GpsType;
import fr.gouv.culture.archivesdefrance.seda.v2.IdentifierType;
import fr.gouv.culture.archivesdefrance.seda.v2.KeyType;
import fr.gouv.culture.archivesdefrance.seda.v2.LevelType;
import fr.gouv.culture.archivesdefrance.seda.v2.LinkingAgentIdentifierType;
import fr.gouv.culture.archivesdefrance.seda.v2.ManagementHistoryDataType;
import fr.gouv.culture.archivesdefrance.seda.v2.ManagementHistoryType;
import fr.gouv.culture.archivesdefrance.seda.v2.MessageDigestBinaryObjectType;
import fr.gouv.culture.archivesdefrance.seda.v2.OrganizationDescriptiveMetadataType;
import fr.gouv.culture.archivesdefrance.seda.v2.OrganizationType;
import fr.gouv.culture.archivesdefrance.seda.v2.ReferencedObjectType;
import fr.gouv.culture.archivesdefrance.seda.v2.SignatureDescriptionType;
import fr.gouv.culture.archivesdefrance.seda.v2.SignatureType;
import fr.gouv.culture.archivesdefrance.seda.v2.SigningInformationType;
import fr.gouv.culture.archivesdefrance.seda.v2.SigningRoleType;
import fr.gouv.culture.archivesdefrance.seda.v2.TextType;
import fr.gouv.culture.archivesdefrance.seda.v2.TimestampingInformationType;
import fr.gouv.vitam.common.ParametersChecker;
import fr.gouv.vitam.common.exception.ExportException;
import fr.gouv.vitam.common.model.unit.ArchiveUnitHistoryModel;
import fr.gouv.vitam.common.model.unit.DescriptiveMetadataModel;
import fr.gouv.vitam.common.model.unit.EventTypeModel;
import fr.gouv.vitam.common.model.unit.KeywordsType;
import fr.gouv.vitam.common.model.unit.LinkingAgentIdentifierTypeModel;
import fr.gouv.vitam.common.model.unit.ReferencedObjectTypeModel;
import fr.gouv.vitam.common.model.unit.SignatureDescriptionTypeModel;
import fr.gouv.vitam.common.model.unit.SignatureInformationExtendedModel;
import fr.gouv.vitam.common.model.unit.SignatureTypeModel;
import fr.gouv.vitam.common.model.unit.SignedObjectDigestModel;
import fr.gouv.vitam.common.model.unit.SigningInformationTypeModel;
import fr.gouv.vitam.common.model.unit.TextByLang;
import fr.gouv.vitam.common.model.unit.TimestampingInformationTypeModel;
import fr.gouv.vitam.common.utils.SupportedSedaVersions;
import org.apache.commons.collections4.CollectionUtils;
import org.w3c.dom.Element;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static javax.xml.datatype.DatatypeFactory.newInstance;

/**
 * Map the object DescriptiveMetadataModel generated from Unit data base model To a jaxb object
 * DescriptiveMetadataContentType This help convert DescriptiveMetadataModel to xml using jaxb
 */
public class DescriptiveMetadataMapper {

    private final CustodialHistoryMapper custodialHistoryMapper = new CustodialHistoryMapper();
    private final ManagementMapper managementMapper = new ManagementMapper(new RuleMapper());


    /**
     * Map local DescriptiveMetadataModel to jaxb DescriptiveMetadataContentType
     *
     * @param metadataModel
     * @param historyListModel
     * @return a descriptive Metadata Content Type
     * @throws DatatypeConfigurationException
     */
    public DescriptiveMetadataContentType map(DescriptiveMetadataModel metadataModel,
        List<ArchiveUnitHistoryModel> historyListModel, SupportedSedaVersions supportedSedaVersion)
        throws DatatypeConfigurationException, ExportException {

        checkSedaCompatibility(metadataModel, supportedSedaVersion);

        DescriptiveMetadataContentType dmc = new DescriptiveMetadataContentType();
        dmc.setAcquiredDate(metadataModel.getAcquiredDate());

        if (metadataModel.getAddressee() != null) {
            dmc.getAddressee().addAll(metadataModel.getAddressee());
        }
        dmc.getAny().addAll(
            TransformJsonTreeToListOfXmlElement.mapJsonToElement(metadataModel.getAny()));

        dmc.setCoverage(mapCoverage(metadataModel.getCoverage()));
        dmc.setCreatedDate(metadataModel.getCreatedDate());

        CustodialHistoryType custodialHistory = custodialHistoryMapper.map(metadataModel.getCustodialHistory());
        dmc.setCustodialHistory(custodialHistory);

        if (metadataModel.getDescription_() != null) {
            dmc.getDescription().addAll(mapTextByLang(metadataModel.getDescription_()));
        }

        if (metadataModel.getDescription() != null) {
            TextType description = new TextType();
            description.setValue(metadataModel.getDescription());
            dmc.getDescription().add(description);
        }

        dmc.setDescriptionLanguage(metadataModel.getDescriptionLanguage());
        dmc.setDescriptionLevel(mapLevelType(metadataModel.getDescriptionLevel()));
        dmc.setDocumentType(mapTextType(metadataModel.getDocumentType()));
        dmc.setEndDate(metadataModel.getEndDate());
        if (metadataModel.getEvent() != null) {
            dmc.getEvent().addAll(mapEvents(metadataModel.getEvent()));
        }
        dmc.setGps(mapGps(metadataModel.getGps()));
        dmc.setOriginatingAgency(mapOrganizationType(metadataModel.getOriginatingAgency()));
        if (metadataModel.getPersistentIdentifier() != null) {
            dmc.getPersistentIdentifier().addAll(metadataModel.getPersistentIdentifier());
        }

        if (metadataModel.getFilePlanPosition() != null && !metadataModel.getFilePlanPosition().isEmpty()) {
            dmc.getFilePlanPosition().addAll(metadataModel.getFilePlanPosition());
        }

        if (metadataModel.getSystemId() != null && !metadataModel.getSystemId().isEmpty()) {
            dmc.getSystemId().addAll(metadataModel.getSystemId());
        }

        if (metadataModel.getOriginatingSystemId() != null && !metadataModel.getOriginatingSystemId().isEmpty()) {
            dmc.getOriginatingSystemId().addAll(metadataModel.getOriginatingSystemId());
        }

        if (metadataModel.getArchivalAgencyArchiveUnitIdentifier() != null &&
            !metadataModel.getArchivalAgencyArchiveUnitIdentifier().isEmpty()) {
            dmc.getArchivalAgencyArchiveUnitIdentifier().addAll(metadataModel.getArchivalAgencyArchiveUnitIdentifier());
        }

        if (metadataModel.getOriginatingAgencyArchiveUnitIdentifier() != null &&
            !metadataModel.getOriginatingAgencyArchiveUnitIdentifier().isEmpty()) {
            dmc.getOriginatingAgencyArchiveUnitIdentifier()
                .addAll(metadataModel.getOriginatingAgencyArchiveUnitIdentifier());
        }

        if (metadataModel.getTransferringAgencyArchiveUnitIdentifier() != null &&
            !metadataModel.getTransferringAgencyArchiveUnitIdentifier().isEmpty()) {
            dmc.getTransferringAgencyArchiveUnitIdentifier().addAll(
                metadataModel.getTransferringAgencyArchiveUnitIdentifier());
        }

        if (metadataModel.getLanguage() != null && !metadataModel.getLanguage().isEmpty()) {
            dmc.getLanguage().addAll(metadataModel.getLanguage());
        }

        if (metadataModel.getAuthorizedAgent() != null && !metadataModel.getAuthorizedAgent().isEmpty()) {
            dmc.getAuthorizedAgent().addAll(metadataModel.getAuthorizedAgent());
        }

        // seda 2.2 fields
        if (CollectionUtils.isNotEmpty(metadataModel.getAgent())) {
            dmc.getAgent().addAll(metadataModel.getAgent());
        }

        if (CollectionUtils.isNotEmpty(metadataModel.getTextContent())) {
            dmc.getTextContent().addAll(metadataModel.getTextContent());
        }

        dmc.setOriginatingSystemIdReplyTo(metadataModel.getOriginatingSystemIdReplyTo());
        dmc.setDateLitteral(metadataModel.getDateLitteral());

        // Deprecated Old Signature model (Seda 2.1 & 2.2). Superseded by SigningInformation model in Seda 2.3+.
        if (CollectionUtils.isNotEmpty(metadataModel.getSignature())) {
            dmc.getSignature().addAll(mapSignatures(metadataModel.getSignature()));
        }

        dmc.setSigningInformation(mapSigningInformation(metadataModel.getSigningInformation()));

        if (metadataModel.getRecipient() != null && !metadataModel.getRecipient().isEmpty()) {
            dmc.getRecipient().addAll(metadataModel.getRecipient());
        }

        if (metadataModel.getKeyword() != null && !metadataModel.getKeyword().isEmpty()) {
            dmc.getKeyword().addAll(mapKeywords(metadataModel.getKeyword()));
        }

        dmc.setReceivedDate(metadataModel.getReceivedDate());
        dmc.setRegisteredDate(metadataModel.getRegisteredDate());
        dmc.setRelatedObjectReference(metadataModel.getRelatedObjectReference());
        dmc.setRegisteredDate(metadataModel.getRegisteredDate());
        dmc.setSentDate(metadataModel.getSentDate());
        dmc.setSource(metadataModel.getSource());
        dmc.setStartDate(metadataModel.getStartDate());
        dmc.setStatus(metadataModel.getStatus());
        dmc.setSubmissionAgency(mapOrganizationType(metadataModel.getSubmissionAgency()));

        if (metadataModel.getTag() != null) {
            dmc.getTag().addAll(metadataModel.getTag());
        }

        if (metadataModel.getTitle_() != null) {
            dmc.getTitle().addAll(mapTextByLang(metadataModel.getTitle_()));
        }

        TextType title = new TextType();
        title.setValue(metadataModel.getTitle());
        dmc.getTitle().add(title);

        dmc.setTransactedDate(metadataModel.getTransactedDate());
        dmc.setType(mapTextType(metadataModel.getType()));
        dmc.setVersion(metadataModel.getVersion());
        if (metadataModel.getWriter() != null) {
            dmc.getWriter().addAll(metadataModel.getWriter());
        }
        if (metadataModel.getTransmitter() != null) {
            dmc.getTransmitter().addAll(metadataModel.getTransmitter());
        }
        if (metadataModel.getSender() != null) {
            dmc.getSender().addAll(metadataModel.getSender());
        }

        fillHistory(historyListModel, dmc.getHistory());

        return dmc;
    }

    private Collection<? extends TextType> mapTextByLang(TextByLang textByLang) {
        if (textByLang == null || textByLang.isEmpty()) {
            return Collections.emptyList();
        }
        return textByLang.getTextByLang().entrySet()
            .stream().map(entry -> {
                TextType textType = new TextType();
                textType.setLang(entry.getKey());
                textType.setValue(entry.getValue());
                return textType;
            })
            .collect(Collectors.toList());
    }

    private OrganizationType mapOrganizationType(fr.gouv.vitam.common.model.unit.OrganizationType organization) {
        if (organization == null) {
            return null;
        }
        OrganizationType organizationType = new OrganizationType();
        organizationType.setIdentifier(mapIdentifier(organization.getIdentifier()));

        if (organization.getOrganizationDescriptiveMetadata() != null) {
            List<Element> elements = TransformJsonTreeToListOfXmlElement.mapJsonToElement(
                organization.getOrganizationDescriptiveMetadata());
            OrganizationDescriptiveMetadataType organizationDescriptiveMetadataType =
                new OrganizationDescriptiveMetadataType();
            organizationDescriptiveMetadataType.getAny().addAll(elements);
            organizationType.setOrganizationDescriptiveMetadata(organizationDescriptiveMetadataType);
        }
        return organizationType;
    }

    private static CoverageType mapCoverage(fr.gouv.vitam.common.model.unit.CoverageType coverage) {
        if (coverage == null) {
            return null;
        }
        CoverageType coverageType = new CoverageType();
        if (CollectionUtils.isNotEmpty(coverage.getSpatial())) {
            coverageType.getSpatial().addAll(coverage.getSpatial().stream().map(DescriptiveMetadataMapper::mapTextType)
                .collect(Collectors.toList()));
        }
        if (CollectionUtils.isNotEmpty(coverage.getTemporal())) {
            coverageType.getTemporal()
                .addAll(coverage.getTemporal().stream().map(DescriptiveMetadataMapper::mapTextType)
                    .collect(Collectors.toList()));
        }
        if (CollectionUtils.isNotEmpty(coverage.getJuridictional())) {
            coverageType.getJuridictional()
                .addAll(coverage.getJuridictional().stream().map(DescriptiveMetadataMapper::mapTextType)
                    .collect(Collectors.toList()));
        }
        return coverageType;
    }

    private static TextType mapTextType(String value) {
        if (value == null) {
            return null;
        }
        TextType textType = new TextType();
        textType.setValue(value);
        return textType;
    }

    private static List<fr.gouv.culture.archivesdefrance.seda.v2.KeywordsType> mapKeywords(
        List<KeywordsType> keywords) {
        List<fr.gouv.culture.archivesdefrance.seda.v2.KeywordsType> result = new ArrayList<>();
        for (KeywordsType keyword : keywords) {
            fr.gouv.culture.archivesdefrance.seda.v2.KeywordsType sedaKeyword =
                new fr.gouv.culture.archivesdefrance.seda.v2.KeywordsType();
            if (keyword.getKeywordContent() != null) {
                TextType content = new TextType();
                content.setValue(keyword.getKeywordContent());
                sedaKeyword.setKeywordContent(content);
            }
            sedaKeyword.setKeywordReference(mapIdentifier(keyword.getKeywordReference()));
            if (keyword.getKeywordType() != null) {
                KeyType keyType = new KeyType();
                keyType.setValue(CodeKeywordType.fromValue(keyword.getKeywordType().value()));
                sedaKeyword.setKeywordType(keyType);
            }
            result.add(sedaKeyword);
        }
        return result;
    }

    private static IdentifierType mapIdentifier(String value) {
        if (value == null) {
            return null;
        }
        IdentifierType identifier = new IdentifierType();
        identifier.setValue(value);
        return identifier;
    }

    private GpsType mapGps(fr.gouv.vitam.common.model.unit.GpsType gps) {
        if (gps == null) {
            return null;
        }
        GpsType result = new GpsType();
        result.setGpsVersionID(gps.getGpsVersionID());
        result.setGpsAltitude(gps.getGpsAltitude());
        result.setGpsAltitudeRef(gps.getGpsAltitudeRef());
        result.setGpsLatitude(gps.getGpsLatitude());
        result.setGpsLatitudeRef(gps.getGpsLatitudeRef());
        result.setGpsLongitude(gps.getGpsLongitude());
        result.setGpsLongitudeRef(gps.getGpsLongitudeRef());
        result.setGpsDateStamp(gps.getGpsDateStamp());
        return result;
    }

    private LevelType mapLevelType(fr.gouv.vitam.common.model.unit.LevelType descriptionLevel) {
        if (descriptionLevel == null) {
            return null;
        }
        return LevelType.fromValue(descriptionLevel.value());
    }

    private static void checkSedaCompatibility(DescriptiveMetadataModel metadataModel,
        SupportedSedaVersions supportedSedaVersion)
        throws ExportException {

        if (supportedSedaVersion.equals(SupportedSedaVersions.SEDA_2_3)) {
            if (CollectionUtils.isNotEmpty(metadataModel.getSignature())) {
                throw new ExportException("Cannot export obsolete Signature tag into SEDA 2.3+.");
            }
        }

        if (supportedSedaVersion.equals(SupportedSedaVersions.SEDA_2_1) ||
            supportedSedaVersion.equals(SupportedSedaVersions.SEDA_2_2)) {
            if (metadataModel.getSigningInformation() != null &&
                (metadataModel.getGps() != null || CollectionUtils.isNotEmpty(metadataModel.getTextContent()) ||
                    CollectionUtils.isNotEmpty(metadataModel.getSignature()) ||
                    metadataModel.getOriginatingSystemIdReplyTo() != null)) {
                throw new ExportException(
                    "Cannot export SigningInformation tag in SEDA " + supportedSedaVersion.getVersion() +
                        " with Signature, Gps, OriginatingSystemIdReplyTo or OriginatingSystemIdReplyTo fields");
            }
        }
    }

    private List<SignatureType> mapSignatures(List<SignatureTypeModel> signatures) {
        if (signatures == null) {
            return null;
        }
        return signatures.stream()
            .map(this::mapSignature)
            .collect(Collectors.toList());
    }

    private SignatureType mapSignature(SignatureTypeModel signatureType) {
        SignatureType result = new SignatureType();
        if (signatureType.getSigner() != null) {
            result.getSigner().addAll(signatureType.getSigner());
        }
        result.setValidator(signatureType.getValidator());
        result.setReferencedObject(mapReferencedObject(signatureType.getReferencedObject()));
        // Not supported in R11
        result.setMasterdata(signatureType.getMasterdata());
        return result;
    }

    private ReferencedObjectType mapReferencedObject(ReferencedObjectTypeModel referencedObject) {
        if (referencedObject == null) {
            return null;
        }
        ReferencedObjectType result = new ReferencedObjectType();
        result.setSignedObjectId(referencedObject.getSignedObjectId());
        result.setSignedObjectDigest(mapSignedObjectDigest(referencedObject.getSignedObjectDigest()));
        return result;
    }

    private MessageDigestBinaryObjectType mapSignedObjectDigest(SignedObjectDigestModel signedMessageDigest) {
        if (signedMessageDigest == null) {
            return null;
        }
        MessageDigestBinaryObjectType result = new MessageDigestBinaryObjectType();
        result.setAlgorithm(signedMessageDigest.getAlgorithm());
        result.setValue(signedMessageDigest.getValue());
        return result;
    }

    private SigningInformationType mapSigningInformation(SigningInformationTypeModel signingInformation)
        throws DatatypeConfigurationException {
        if (signingInformation == null) {
            return null;
        }
        SigningInformationType signingInformationType = new SigningInformationType();
        if (signingInformation.getSigningRole() != null) {
            signingInformationType.getSigningRole()
                .addAll(mapSigningRole(signingInformation.getSigningRole()));
        }
        if (signingInformation.getDetachedSigningRole() != null) {
            signingInformationType.getDetachedSigningRole()
                .addAll(mapDetachedSigningRole(signingInformation.getDetachedSigningRole()));
        }
        if (signingInformation.getSignatureDescription() != null) {
            signingInformationType.getSignatureDescription().addAll(
                mapSignaturesDescription(signingInformation.getSignatureDescription()));
        }
        if (signingInformation.getTimestampingInformation() != null) {
            signingInformationType.getTimestampingInformation().addAll(
                mapTimestampingInformation(signingInformation.getTimestampingInformation()));
        }
        if (signingInformation.getAdditionalProof() != null) {
            signingInformationType.getAdditionalProof().addAll(
                mapAdditionalProofs(signingInformation.getAdditionalProof()));
        }
        signingInformationType.setExtended(mapExtendedParams(signingInformation.getExtended()));
        return signingInformationType;
    }

    private List<SigningRoleType> mapSigningRole(
        List<fr.gouv.vitam.common.model.unit.SigningRoleType> signingRole) {
        return signingRole.stream()
            .map(role -> {
                switch (role) {
                    case SIGNED_DOCUMENT:
                        return SigningRoleType.SIGNED_DOCUMENT;
                    case TIMESTAMP:
                        return SigningRoleType.TIMESTAMP;
                    case SIGNATURE:
                        return SigningRoleType.SIGNATURE;
                    case ADDITIONAL_PROOF:
                        return SigningRoleType.ADDITIONAL_PROOF;
                    default:
                        throw new IllegalStateException("Unexpected value: " + role);
                }
            })
            .collect(Collectors.toList());
    }

    private List<DetachedSigningRoleType> mapDetachedSigningRole(
        List<fr.gouv.vitam.common.model.unit.DetachedSigningRoleType> detachedSigningRole) {
        return detachedSigningRole.stream()
            .map(role -> {
                switch (role) {
                    case TIMESTAMP:
                        return DetachedSigningRoleType.TIMESTAMP;
                    case SIGNATURE:
                        return DetachedSigningRoleType.SIGNATURE;
                    case ADDITIONAL_PROOF:
                        return DetachedSigningRoleType.ADDITIONAL_PROOF;
                    default:
                        throw new IllegalStateException("Unexpected value: " + role);
                }
            })
            .collect(Collectors.toList());
    }

    private List<SignatureDescriptionType> mapSignaturesDescription(
        List<SignatureDescriptionTypeModel> signature) {
        return signature.stream()
            .map(this::mapSignature)
            .collect(Collectors.toList());
    }

    private SignatureDescriptionType mapSignature(SignatureDescriptionTypeModel signatureTypeModel) {
        SignatureDescriptionType signatureDescriptionType = new SignatureDescriptionType();
        signatureDescriptionType.setSigner(signatureTypeModel.getSigner());
        signatureDescriptionType.setValidator(signatureTypeModel.getValidator());
        signatureDescriptionType.setSigningType(signatureTypeModel.getSigningType());
        return signatureDescriptionType;
    }

    private List<TimestampingInformationType> mapTimestampingInformation(
        List<TimestampingInformationTypeModel> timestampingInformation) throws DatatypeConfigurationException {
        List<TimestampingInformationType> timestampingInformationTypes = new ArrayList<>();
        for (TimestampingInformationTypeModel timestampingInfo : timestampingInformation) {
            timestampingInformationTypes.add(mapTimestampingInformation(timestampingInfo));
        }
        return timestampingInformationTypes;
    }

    private TimestampingInformationType mapTimestampingInformation(
        TimestampingInformationTypeModel timestampingInfo) throws DatatypeConfigurationException {
        TimestampingInformationType timestampingInformationType = new TimestampingInformationType();

        Optional<XMLGregorianCalendar> timeStamp =
            stringToXMLGregorianCalendar(timestampingInfo.getTimeStamp());
        timeStamp.ifPresent(timestampingInformationType::setTimeStamp);

        timestampingInformationType.setAdditionalTimestampingInformation(
            timestampingInfo.getAdditionalTimestampingInformation());
        return timestampingInformationType;
    }

    private List<AdditionalProofType> mapAdditionalProofs(
        List<fr.gouv.vitam.common.model.unit.AdditionalProofType> additionalProofs) {
        return additionalProofs.stream()
            .map(additionalProof -> {
                AdditionalProofType additionalProofType = new AdditionalProofType();
                additionalProofType.getAdditionalProofInformation()
                    .addAll(additionalProof.getAdditionalProofInformation());
                return additionalProofType;
            })
            .collect(Collectors.toList());
    }

    private ExtendedType mapExtendedParams(SignatureInformationExtendedModel extended) {
        if (extended == null || extended.getAny() == null || extended.getAny().isEmpty()) {
            return null;
        }
        ExtendedType extendedType = new ExtendedType();
        extendedType.getAny()
            .addAll(TransformJsonTreeToListOfXmlElement.mapJsonToElement(extended.getAny()));
        return extendedType;
    }

    private List<EventType> mapEvents(List<EventTypeModel> eventTypes) {
        return eventTypes.stream()
            .map(this::mapEvent)
            .collect(Collectors.toList());
    }

    private EventType mapEvent(EventTypeModel event) {
        EventType eventType = new EventType();
        eventType.setEventDateTime(event.getEventDateTime());
        eventType.setEventDetail(event.getEventDetail());
        eventType.setEventDetailData(event.getEventDetailData());
        eventType.setEventIdentifier(event.getEventIdentifier());
        eventType.setEventType(event.getEventType());
        eventType.setEventTypeCode(event.getEventTypeCode());
        eventType.setOutcome(event.getOutcome());
        eventType.setOutcomeDetail(event.getOutcomeDetail());
        eventType.setOutcomeDetailMessage(event.getOutcomeDetailMessage());
        if (Objects.nonNull(event.getLinkingAgentIdentifier())) {
            eventType.getLinkingAgentIdentifier().addAll(
                event.getLinkingAgentIdentifier().stream().map(this::mapLinkingAgentIdentifier)
                    .collect(Collectors.toList()));
        }
        return eventType;
    }

    private LinkingAgentIdentifierType mapLinkingAgentIdentifier(
        LinkingAgentIdentifierTypeModel linkingAgentIdentifierTypeModel) {
        if (linkingAgentIdentifierTypeModel == null) {
            return null;
        }
        LinkingAgentIdentifierType linkingAgentIdentifierType = new LinkingAgentIdentifierType();
        linkingAgentIdentifierType.setLinkingAgentIdentifierType(
            linkingAgentIdentifierTypeModel.getLinkingAgentIdentifierType());
        linkingAgentIdentifierType.setLinkingAgentIdentifierValue(
            linkingAgentIdentifierTypeModel.getLinkingAgentIdentifierValue());
        linkingAgentIdentifierType.setLinkingAgentRole(linkingAgentIdentifierTypeModel.getLinkingAgentRole());
        return linkingAgentIdentifierType;
    }

    private void fillHistory(List<ArchiveUnitHistoryModel> archiveUnitHistoryModel,
        List<ManagementHistoryType> managementHistoryType)
        throws DatatypeConfigurationException {

        for (ArchiveUnitHistoryModel historyModel : archiveUnitHistoryModel) {

            ManagementHistoryType historyType = new ManagementHistoryType();
            historyType.setData(new ManagementHistoryDataType());
            historyType.getData().setVersion(historyModel.getData().getVersion());
            historyType.getData().setManagement(managementMapper.map(historyModel.getData().getManagement()));
            Optional<XMLGregorianCalendar> updateDate = stringToXMLGregorianCalendar(historyModel.getUpdateDate());
            if (updateDate.isPresent()) {
                historyType.setUpdateDate(updateDate.get());
            }

            managementHistoryType.add(historyType);
        }
    }

    private Optional<XMLGregorianCalendar> stringToXMLGregorianCalendar(String date)
        throws DatatypeConfigurationException {
        if (ParametersChecker.isNotEmpty(date)) {
            return Optional.of(newInstance().newXMLGregorianCalendar(date));
        } else {
            return Optional.empty();
        }
    }

}

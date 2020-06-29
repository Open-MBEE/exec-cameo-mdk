package gov.nasa.jpl.mbee.mdk.mms.validation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.export.image.ImageExporter;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.http.ServerException;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.mms.MMSArtifact;
import gov.nasa.jpl.mbee.mdk.mms.MMSUtils;
import gov.nasa.jpl.mbee.mdk.mms.actions.CommitDiagramArtifactsAction;
import gov.nasa.jpl.mbee.mdk.mms.actions.ValidateElementAction;
import gov.nasa.jpl.mbee.mdk.util.MDUtils;
import gov.nasa.jpl.mbee.mdk.util.Pair;
import gov.nasa.jpl.mbee.mdk.validation.ValidationRule;
import gov.nasa.jpl.mbee.mdk.validation.ValidationRuleViolation;
import gov.nasa.jpl.mbee.mdk.validation.ValidationSuite;
import gov.nasa.jpl.mbee.mdk.validation.ViolationSeverity;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.entity.ContentType;

import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.stream.Collectors;

public class DiagramValidator implements RunnableWithProgress {
    private static final Map<String, Pair<Integer, ContentType>> EXPORT_FORMATS = new LinkedHashMap<>(2);

    static {
        EXPORT_FORMATS.put("svg", new Pair<>(ImageExporter.SVG, ContentType.create("image/svg+xml")));
        EXPORT_FORMATS.put("png", new Pair<>(ImageExporter.PNG, ContentType.create("image/png")));
    }

    private final Set<Diagram> diagrams;
    private final Project project;
    private ValidationSuite suite = new ValidationSuite("Image Validation");

    private ValidationRule imageEquivalenceRule = new ValidationRule("Artifact(s) Equivalence", "Artifact(s) shall be equivalent.", ViolationSeverity.ERROR);
    private ValidationRule diagramExistenceRule = new ValidationRule("Diagram Existence", "The source Diagram of artifact(s) shall exist on MMS.", ViolationSeverity.ERROR);

    {
        suite.addValidationRule(imageEquivalenceRule);
        suite.addValidationRule(diagramExistenceRule);
    }

    public DiagramValidator(Set<Diagram> diagrams, Project project) {
        this.diagrams = diagrams;
        this.project = project;
    }

    @Override
    public void run(ProgressStatus progressStatus) {
        if (diagrams.isEmpty()) {
            return;
        }
        progressStatus.setIndeterminate(true);

        boolean initialUseSVGTestTag = Application.getInstance().getEnvironmentOptions().getGeneralOptions().isUseSVGTextTag();
        Application.getInstance().getEnvironmentOptions().getGeneralOptions().setUseSVGTextTag(true);
        try {
            Set<String> diagramIds = diagrams.stream().map(Converters.getElementToIdConverter()).filter(Objects::nonNull).collect(Collectors.toSet());
            Map<String, ObjectNode> diagramElementsMap = new LinkedHashMap<>();
            Map<String, Set<String>> elementArtifactMap = new LinkedHashMap<>();

            if (!diagramIds.isEmpty()) {
                ObjectNode diagramElementsResponse;
                try {
                    File responseFile = MMSUtils.getElements(project, diagramIds, progressStatus);
                    try (JsonParser jsonParser = JacksonUtils.getJsonFactory().createParser(responseFile)) {
                        diagramElementsResponse = JacksonUtils.parseJsonObject(jsonParser);
                    }
                } catch (IOException | ServerException | URISyntaxException | CertificateException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
                    e.printStackTrace();
                    Application.getInstance().getGUILog().log("[ERROR] An unexpected error occurred while generating diagrams. Skipping image validation. Reason: " + e.getMessage());
                    return;

                }
                JsonNode diagramElementsArray;
                if (diagramElementsResponse == null || (diagramElementsArray = diagramElementsResponse.get("elements")) == null || !diagramElementsArray.isArray()) {
                    Application.getInstance().getGUILog().log("[ERROR] Response of request to get diagram elements is malformed. Skipping image validation.");
                    return;
                }

                for (JsonNode jsonNode : diagramElementsArray) {
                    JsonNode idNode;
                    if (!jsonNode.isObject()) {
                        continue;
                    }
                    if ((idNode = jsonNode.get(MDKConstants.ID_KEY)) == null || !idNode.isTextual()) {
                        continue;
                    }
                    diagramElementsMap.put(idNode.asText(), (ObjectNode) jsonNode);
                    Set<String> artifactIds = elementArtifactMap.computeIfAbsent(idNode.asText(), o -> new LinkedHashSet<>());
                    JsonNode artifactsNode;
                    if ((artifactsNode = jsonNode.get(MDKConstants.ARTIFACT_IDS_KEY)) != null && artifactsNode.isArray()) {
                        artifactsNode.forEach(node -> artifactIds.add(node.asText()));
                    }
                }
            }

            Set<String> artifactIds = elementArtifactMap.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
            Map<String, ObjectNode> artifactsMap = new LinkedHashMap<>();

            if (!artifactIds.isEmpty()) {
                ObjectNode artifactsResponse;
                try {
                    File responseFile = MMSUtils.getArtifacts(project, artifactIds, progressStatus);
                    try (JsonParser jsonParser = JacksonUtils.getJsonFactory().createParser(responseFile)) {
                        artifactsResponse = JacksonUtils.parseJsonObject(jsonParser);
                    }
                } catch (IOException | ServerException | URISyntaxException | CertificateException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
                    e.printStackTrace();
                    Application.getInstance().getGUILog().log("[ERROR] An unexpected error occurred while generating diagrams. Skipping image validation. Reason: " + e.getMessage());
                    return;
                }
                JsonNode artifactsArray;
                if (artifactsResponse == null || (artifactsArray = artifactsResponse.get("artifacts")) == null || !artifactsArray.isArray()) {
                    Application.getInstance().getGUILog().log("[ERROR] Response of request to get artifacts is malformed. Skipping image validation.");
                    return;
                }
                for (JsonNode jsonNode : artifactsArray) {
                    JsonNode idNode;
                    if (!jsonNode.isObject()) {
                        continue;
                    }
                    if ((idNode = jsonNode.get(MDKConstants.ID_KEY)) == null || !idNode.isTextual()) {
                        continue;
                    }
                    artifactsMap.put(idNode.asText(), (ObjectNode) jsonNode);
                }
            }

            for (Diagram diagram : diagrams) {
                String diagramId = Converters.getElementToIdConverter().apply(diagram);
                if (diagramId == null) {
                    continue;
                }
                if (!diagramElementsMap.containsKey(diagramId)) {
                    ValidationRuleViolation vrv = new ValidationRuleViolation(diagram, diagramExistenceRule.getDescription());
                    vrv.addAction(new ValidateElementAction(Collections.singleton(diagram), "Validate"));
                    diagramExistenceRule.addViolation(vrv);
                    continue;
                }
                DiagramPresentationElement diagramPresentationElement = project.getDiagram(diagram);
                if (diagramPresentationElement == null) {
                    continue;
                }

                Set<MMSArtifact> disparateArtifacts = new LinkedHashSet<>(2);

                for (Map.Entry<String, Pair<Integer, ContentType>> entry : EXPORT_FORMATS.entrySet()) {
                    Path path;
                    String checksum;

                    Application.getInstance().getGUILog().log("[INFO] Generating Diagram of type" + diagramPresentationElement.getDiagramType().getRootType() + ".");

                    try {

                        path = Files.createTempFile(DiagramValidator.class.getSimpleName() + "-" + diagramId, "." + entry.getKey());
                        if (entry.getValue().getKey() == ImageExporter.SVG) {
                            MDUtils.exportSVG(path.toFile(),diagramPresentationElement);
                        }else {
                            ImageExporter.export(diagramPresentationElement, ImageExporter.PNG, path.toFile());
                        }
                        try (InputStream inputStream = new FileInputStream(path.toFile())) {
                            checksum = DigestUtils.md5Hex(inputStream);
                        }
                    } catch (IOException | TransformerException e) {
                        e.printStackTrace();
                        Application.getInstance().getGUILog().log("[ERROR] An unexpected error occurred while generating diagrams. Skipping image validation for " + Converters.getElementToHumanNameConverter().apply(diagram) + ". Reason: " + e.getMessage());
                        break;
                    }
                    ObjectNode existingArtifact = elementArtifactMap.getOrDefault(diagramId, Collections.emptySet()).stream().map(artifactsMap::get).filter(Objects::nonNull).filter(node -> {
                        JsonNode contentTypeNode = node.get(MDKConstants.CONTENT_TYPE_KEY);
                        return contentTypeNode != null && contentTypeNode.isTextual() && contentTypeNode.asText().equals(entry.getValue().getValue().getMimeType());
                    }).findFirst().orElse(null);
                    JsonNode checksumNode;
                    if (existingArtifact == null || (checksumNode = existingArtifact.get(MDKConstants.CHECKSUM_KEY)) == null || !checksumNode.isTextual() || !checksumNode.asText().equals(checksum)) {
                        disparateArtifacts.add(new MMSArtifact() {
                            private final String id = existingArtifact != null ? existingArtifact.get(MDKConstants.ID_KEY).asText() : UUID.randomUUID().toString();

                            @Override
                            public String getId() {
                                return id;
                            }

                            @Override
                            public String getChecksum() {
                                return checksum;
                            }

                            @Override
                            public InputStream getInputStream() {
                                try {
                                    return new FileInputStream(path.toFile());
                                } catch (FileNotFoundException e) {
                                    return null;
                                }
                            }

                            @Override
                            public ContentType getContentType() {
                                return entry.getValue().getValue();
                            }
                        });
                    }
                }
                if (!disparateArtifacts.isEmpty()) {
                    ValidationRuleViolation vrv = new ValidationRuleViolation(diagram, imageEquivalenceRule.getDescription());
                    vrv.addAction(new CommitDiagramArtifactsAction(diagram, elementArtifactMap.getOrDefault(diagramId, Collections.emptySet()), disparateArtifacts, project));
                    imageEquivalenceRule.addViolation(vrv);
                }
            }
        } finally {
            Application.getInstance().getEnvironmentOptions().getGeneralOptions().setUseSVGTextTag(initialUseSVGTestTag);
        }
    }

    public ValidationSuite getSuite() {
        return suite;
    }
}

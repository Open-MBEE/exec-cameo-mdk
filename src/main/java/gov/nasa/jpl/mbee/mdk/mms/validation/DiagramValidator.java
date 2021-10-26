package gov.nasa.jpl.mbee.mdk.mms.validation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BaseJsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
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
import gov.nasa.jpl.mbee.mdk.docgen.DocGenUtils;
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
import java.security.GeneralSecurityException;
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
            Map<String, ObjectNode> artifactsMap = new LinkedHashMap<>();
            Map<String, Set<String>> elementArtifactMap = new LinkedHashMap<>();

            //Get and/or check for existing diagram objects, collect their associated artifactIDs
            if (!diagramIds.isEmpty()) {
                ObjectNode diagramElementsResponse;
                try {
                    File responseFile = MMSUtils.getElementsRecursively(project, diagramIds, progressStatus);
                    try (JsonParser jsonParser = JacksonUtils.getJsonFactory().createParser(responseFile)) {
                        diagramElementsResponse = JacksonUtils.parseJsonObject(jsonParser);
                    }
                } catch (IOException | ServerException | URISyntaxException | GeneralSecurityException e) {
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
                    Set<String> artifactExtensions = elementArtifactMap.computeIfAbsent(idNode.asText(), o -> new LinkedHashSet<>());
                    JsonNode artifactsArray;
                    if ((artifactsArray = jsonNode.get(MDKConstants.ARTIFACTS_KEY)) != null && artifactsArray.isArray()) {
                        artifactsArray.forEach(node -> {
                            artifactExtensions.add(idNode.asText() + '_' + node.get("extension").asText());
                            if (node instanceof ObjectNode) {
                                artifactsMap.put(idNode.asText() + '_' + node.get("extension").asText(), (ObjectNode) node);
                            }
                        });
                    }
                }

            }

            for (Diagram diagram : diagrams) {
                String diagramId = Converters.getElementToIdConverter().apply(diagram);
                if (diagramId == null) {
                    continue;
                }
                if (!elementArtifactMap.containsKey(diagramId)) {
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

                    try {
                        path = Files.createTempFile(DiagramValidator.class.getSimpleName() + "-" + diagramId, "." + entry.getKey());
                        if (entry.getValue().getKey() == ImageExporter.SVG) {
                            MDUtils.exportSVG(path.toFile(), diagramPresentationElement);
                        }
                        else {
                            ImageExporter.export(diagramPresentationElement, ImageExporter.PNG, path.toFile(), false, DocGenUtils.DOCGEN_DIAGRAM_DPI, DocGenUtils.DOCGEN_DIAGRAM_SCALE_PERCENT);
                        }
                        try (InputStream inputStream = new FileInputStream(path.toFile())) {
                            checksum = DigestUtils.md5Hex(inputStream);
                        }
                    } catch (IOException | TransformerException e) {
                        e.printStackTrace();
                        Application.getInstance().getGUILog().log("[ERROR] An unexpected error occurred while generating diagrams. Skipping image validation for " + Converters.getElementToHumanNameConverter().apply(diagram) + ". Reason: " + e.getMessage());
                        break;
                    }
                    ObjectNode existingBinary = elementArtifactMap.getOrDefault(diagramId, Collections.emptySet()).stream().map(artifactsMap::get).filter(Objects::nonNull).filter(node -> {
                        JsonNode contentTypeNode = node.get(MDKConstants.MIMETYPE_KEY);
                        return contentTypeNode != null && contentTypeNode.isTextual() && contentTypeNode.asText().equals(entry.getValue().getValue().getMimeType());
                    }).findFirst().orElse(null);

                    JsonNode checksumNode;
                    if (existingBinary == null || (checksumNode = existingBinary.get(MDKConstants.CHECKSUM_KEY)) == null || !checksumNode.isTextual() || !checksumNode.asText().equals(checksum)) {
                        disparateArtifacts.add(new MMSArtifact() {
                            private final String id = diagramId;

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

                            @Override
                            public String getFileExtension() { return entry.getKey(); }
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

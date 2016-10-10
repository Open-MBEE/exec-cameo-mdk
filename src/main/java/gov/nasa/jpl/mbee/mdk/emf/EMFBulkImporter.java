package gov.nasa.jpl.mbee.mdk.emf;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.annotations.SessionManaged;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.JsonToElementFunction;
import gov.nasa.jpl.mbee.mdk.ems.ImportException;
import gov.nasa.jpl.mbee.mdk.ems.json.JsonEquivalencePredicate;
import gov.nasa.jpl.mbee.mdk.lib.Changelog;
import gov.nasa.jpl.mbee.mdk.lib.MDUtils;
import gov.nasa.jpl.mbee.mdk.lib.Pair;

import java.util.*;
import java.util.function.BiFunction;

/**
 * Created by igomes on 9/28/16.
 */
// TODO What about locks? @donbot
public class EMFBulkImporter implements BiFunction<Collection<ObjectNode>, Project, Changelog<String, Pair<Element, ObjectNode>>> {
    private final String sessionName;
    private int sessionCount;

    private Changelog<String, Pair<Element, ObjectNode>> changelog;
    private Map<Pair<Element, ObjectNode>, ImportException> failedElementMap;
    private Map<Element, ObjectNode> nonEquivalentElements;
    private Map<String, Element> elementCache;

    private final BiFunction<String, Project, Element> bulkIdToElementConverter = (id, project) -> {
        Element element = Converters.getIdToElementConverter().apply(id, project);
        //System.out.println("[NO CACHE] " + id + " -> " + element);
        if (element == null && elementCache != null) {
            element = elementCache.get(id);
            //System.out.println("[CACHE] " + id + " -> " + element);
        }
        return element;
    };

    public EMFBulkImporter(String sessionName) {
        this.sessionName = sessionName;
    }

    @SessionManaged
    @Override
    public Changelog<String, Pair<Element, ObjectNode>> apply(Collection<ObjectNode> objectNodes, Project project) {
        failedElementMap = new LinkedHashMap<>(objectNodes.size());
        nonEquivalentElements = new LinkedHashMap<>();

        JsonToElementFunction jsonToElementFunction = new EMFImporter() {
            @Override
            protected List<EStructuralFeatureOverride> getEStructuralFeatureOverrides() {
                if (eStructuralFeatureOverrides == null) {
                    eStructuralFeatureOverrides = new ArrayList<>(super.getEStructuralFeatureOverrides());
                    eStructuralFeatureOverrides.remove(EStructuralFeatureOverride.OWNER);
                    eStructuralFeatureOverrides.add(EStructuralFeatureOverride.getOwnerEStructuralFeatureOverride(bulkIdToElementConverter));
                }
                return eStructuralFeatureOverrides;
            }

            @Override
            protected List<PreProcessor> getPreProcessors() {
                if (preProcessors == null) {
                    preProcessors = new ArrayList<>(super.getPreProcessors());
                    preProcessors.remove(PreProcessor.CREATE);
                    preProcessors.add(0, PreProcessor.getCreatePreProcessor(bulkIdToElementConverter));
                }
                return preProcessors;
            }

            @Override
            protected BiFunction<String, Project, Element> getIdToElementConverter() {
                return bulkIdToElementConverter;
            }
        };

        bulkImport:
        while (failedElementMap.isEmpty() && !objectNodes.isEmpty()) {
            changelog = new Changelog<>();
            elementCache = new HashMap<>();

            List<ObjectNode> retryObjectNodes = new ArrayList<>();

            if (SessionManager.getInstance().isSessionCreated()) {
                SessionManager.getInstance().cancelSession();
            }
            SessionManager.getInstance().createSession(project, sessionName + " x" + objectNodes.size() + " #" + ++sessionCount);

            Iterator<ObjectNode> iterator = objectNodes.iterator();
            while (iterator.hasNext()) {
                ObjectNode objectNode = iterator.next();
                JsonNode sysmlIdJsonNode = objectNode.get(MDKConstants.SYSML_ID_KEY);
                String sysmlId = sysmlIdJsonNode != null && sysmlIdJsonNode.isTextual() ? sysmlIdJsonNode.asText() : null;
                if (MDUtils.isDeveloperMode()) {
                    System.out.println("[ATTEMPT 1] Attempting " + sysmlId);
                }
                Changelog.Change<Element> change = null;
                try {
                    change = jsonToElementFunction.apply(objectNode, project, false);
                } catch (ImportException e) {
                    if (MDUtils.isDeveloperMode()) {
                        e.printStackTrace();
                    }
                }
                if (change == null || change.getChanged() == null) {
                    if (MDUtils.isDeveloperMode()) {
                        System.err.println("[FAILED 1] Could not create " + sysmlId);
                    }
                    // Element may fail to create on first pass, ex: Diagram (because owner doesn't exist yet + custom creation), so we need to retry after everything else.
                    retryObjectNodes.add(objectNode);
                    //failedElementMap.put(new Pair<>(Converters.getIdToElementConverter().apply(objectNode.get(MDKConstants.SYSML_ID_KEY).asText(), project), objectNode), importException);
                    //iterator.remove();
                    //continue bulkImport;
                }
                else {
                    if (MDUtils.isDeveloperMode()) {
                        System.out.println("[SUCCESS 1] Imported " + sysmlId);
                    }
                    if (sysmlId != null) {
                        elementCache.put(sysmlId, change.getChanged());
                    }
                }
                //changelog.addChange(Converters.getElementToIdConverter().apply(change.getChanged()), new Pair<>(change.getChanged(), objectNode), change.getType());
            }

            for (ObjectNode objectNode : retryObjectNodes) {
                JsonNode sysmlIdJsonNode = objectNode.get(MDKConstants.SYSML_ID_KEY);
                String sysmlId = sysmlIdJsonNode != null && sysmlIdJsonNode.isTextual() ? sysmlIdJsonNode.asText() : null;
                if (MDUtils.isDeveloperMode()) {
                    System.out.println("[ATTEMPT 1.5] Attempting " + sysmlId);
                }
                Changelog.Change<Element> change = null;
                ImportException importException = new ImportException(null, objectNode, "Null on retry pass");
                try {
                    change = jsonToElementFunction.apply(objectNode, project, false);
                } catch (ImportException e) {
                    if (MDUtils.isDeveloperMode()) {
                        e.printStackTrace();
                    }
                    importException = e;
                }
                if (change == null || change.getChanged() == null) {
                    if (MDUtils.isDeveloperMode()) {
                        System.err.println("[FAILED 1.5] Could not create " + sysmlId);
                    }
                    failedElementMap.put(new Pair<>(Converters.getIdToElementConverter().apply(objectNode.get(MDKConstants.SYSML_ID_KEY).asText(), project), objectNode), importException);
                    objectNodes.remove(objectNode);
                    continue bulkImport;
                }
                else {
                    if (MDUtils.isDeveloperMode()) {
                        System.out.println("[SUCCESS 1.5] Imported " + sysmlId);
                    }
                    if (sysmlId != null) {
                        elementCache.put(sysmlId, change.getChanged());
                    }
                }
            }

            iterator = objectNodes.iterator();
            while (iterator.hasNext()) {
                ObjectNode objectNode = iterator.next();
                JsonNode sysmlIdJsonNode = objectNode.get(MDKConstants.SYSML_ID_KEY);
                String sysmlId = sysmlIdJsonNode != null && sysmlIdJsonNode.isTextual() ? sysmlIdJsonNode.asText() : "<>";
                if (MDUtils.isDeveloperMode()) {
                    System.out.println("[ATTEMPT 2] Attempting " + sysmlId);
                }
                Changelog.Change<Element> change = null;
                ImportException importException = new ImportException(null, objectNode, "Null on second pass");
                try {
                    change = jsonToElementFunction.apply(objectNode, project, true);
                } catch (ImportException e) {
                    if (MDUtils.isDeveloperMode()) {
                        e.printStackTrace();
                    }
                    importException = e;
                }
                if (change == null || change.getChanged() == null) {
                    if (MDUtils.isDeveloperMode()) {
                        System.err.println("[FAILED 2] Could not import " + sysmlId);
                    }
                    failedElementMap.put(new Pair<>(Converters.getIdToElementConverter().apply(objectNode.get(MDKConstants.SYSML_ID_KEY).asText(), project), objectNode), importException);
                    iterator.remove();
                    continue bulkImport;
                }
                else {
                    if (MDUtils.isDeveloperMode()) {
                        System.out.println("[SUCCESS 2] Imported " + sysmlId);
                    }
                    if (sysmlId != null) {
                        elementCache.put(sysmlId, change.getChanged());
                    }
                    changelog.addChange(Converters.getElementToIdConverter().apply(change.getChanged()), new Pair<>(change.getChanged(), objectNode), change.getType());
                }
            }

            for (Changelog.ChangeType changeType : Changelog.ChangeType.values()) {
                for (Map.Entry<String, Pair<Element, ObjectNode>> entry : changelog.get(changeType).entrySet()) {
                    Element element = entry.getValue().getFirst();
                    ObjectNode objectNode = entry.getValue().getSecond();

                    if (element.isInvalid()) {
                        if (MDUtils.isDeveloperMode()) {
                            JsonNode sysmlIdJsonNode = objectNode.get(MDKConstants.SYSML_ID_KEY);
                            String sysmlId = sysmlIdJsonNode != null && sysmlIdJsonNode.isTextual() ? sysmlIdJsonNode.asText() : "<>";
                            System.err.println("[FAILED 3] Could not create " + sysmlId);
                        }
                        failedElementMap.put(new Pair<>(element, objectNode), new ImportException(element, objectNode, "Element was found to be invalid after importing."));
                        objectNodes.remove(objectNode);
                        continue bulkImport;
                    }
                    ObjectNode sourceObjectNode = Converters.getElementToJsonConverter().apply(element, project);
                    if (!JsonEquivalencePredicate.getInstance().test(sourceObjectNode, objectNode)) {
                        // currently handled as a warning instead of an error
                        nonEquivalentElements.put(element, objectNode);
                    }
                }
            }
            if (SessionManager.getInstance().isSessionCreated()) {
                SessionManager.getInstance().closeSession();
            }
            return changelog;
        }
        if (SessionManager.getInstance().isSessionCreated()) {
            SessionManager.getInstance().cancelSession();
        }
        return changelog;
    }

    public String getSessionName() {
        return sessionName;
    }

    public int getSessionCount() {
        return sessionCount;
    }

    public Changelog<String, Pair<Element, ObjectNode>> getChangelog() {
        return changelog;
    }

    public Map<Pair<Element, ObjectNode>, ImportException> getFailedElementMap() {
        return failedElementMap;
    }

    public Map<Element, ObjectNode> getNonEquivalentElements() {
        return nonEquivalentElements;
    }
}

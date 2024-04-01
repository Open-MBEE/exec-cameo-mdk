package org.openmbee.mdk.emf;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.uml.transaction.RepositoryModelValidator;
import com.nomagic.task.ProgressStatus;
import com.nomagic.uml2.cache.ModelRepositoryCacheValidator;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.transaction.ModelValidationResult;
import org.openmbee.mdk.api.incubating.MDKConstants;
import org.openmbee.mdk.api.incubating.annotations.SessionManaged;
import org.openmbee.mdk.api.incubating.convert.Converters;
import org.openmbee.mdk.api.incubating.convert.JsonToElementFunction;
import org.openmbee.mdk.json.ImportException;
import org.openmbee.mdk.mms.json.JsonEquivalencePredicate;
import org.openmbee.mdk.util.Changelog;
import org.openmbee.mdk.util.MDUtils;
import org.openmbee.mdk.util.Pair;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.BiFunction;

/**
 * Created by igomes on 9/28/16.
 */
public class EMFBulkImporter implements BulkImportFunction {
    private final String sessionName;
    private int sessionCount;

    private Changelog<String, Pair<Element, ObjectNode>> changelog;
    private Map<Pair<Element, ObjectNode>, Exception> failedElementMap;
    private Map<Element, ObjectNode> nonEquivalentElements;
    private Map<String, Element> elementCache;

    private final BiFunction<String, Project, Element> bulkIdToElementConverter = (id, project) -> {
        Element element = Converters.getIdToElementConverter().apply(id, project);
        if (MDUtils.isDeveloperMode()) {
            System.out.println("[NO CACHE] " + id + " -> " + element);
        }
        if (element == null && elementCache != null) {
            element = elementCache.get(id);
            if (MDUtils.isDeveloperMode()) {
                System.out.println("[CACHE] " + id + " -> " + element);
            }
        }
        return element;
    };

    public EMFBulkImporter(String sessionName) {
        this.sessionName = sessionName;
    }

    @SessionManaged
    @Override
    public Changelog<String, Pair<Element, ObjectNode>> apply(Collection<ObjectNode> objectNodes, Project project, ProgressStatus progressStatus) {
        String initialProgressStatusDescription = null;
        long initialProgressStatusCurrent = 0;
        boolean initialProgressStatusIndeterminate = false;
        //uncomment for 2021x
        //RepositoryModelValidator validator = new RepositoryModelValidator(project);
        RepositoryModelValidator validator = getValidator(project);

        if (progressStatus != null) {
            initialProgressStatusDescription = progressStatus.getDescription();
            initialProgressStatusCurrent = progressStatus.getCurrent();
            initialProgressStatusIndeterminate = progressStatus.isIndeterminate();

            progressStatus.setMax(objectNodes.size() * 3);
            progressStatus.setCurrent(0);
        }

        project.getModels().forEach(EMFBulkImporter::preloadRecursively);

        try {
            objectNodes = new ArrayList<>(objectNodes);
            failedElementMap = new LinkedHashMap<>(objectNodes.size());
            nonEquivalentElements = new LinkedHashMap<>();
            Map<Element, Changelog.ChangeType> changeTypeMap = new HashMap<>(objectNodes.size());

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
            while (/*failedElementMap.isEmpty() && */!objectNodes.isEmpty()) {
                changelog = new Changelog<>();
                elementCache = new HashMap<>();

                List<ObjectNode> retryObjectNodes = new ArrayList<>();

                if (SessionManager.getInstance().isSessionCreated(project)) {
                    SessionManager.getInstance().cancelSession(project);
                }
                SessionManager.getInstance().createSession(project, sessionName + " x" + objectNodes.size() + " #" + ++sessionCount);
                if (progressStatus != null) {
                    progressStatus.setDescription(sessionName + " - " + NumberFormat.getInstance().format(objectNodes.size()) + " elements" + (!failedElementMap.isEmpty() ? " - " + NumberFormat.getInstance().format(failedElementMap.size()) + " failed" : ""));
                    progressStatus.setCurrent(progressStatus.getMax() - objectNodes.size() * 3);
                }

                Iterator<ObjectNode> iterator = objectNodes.iterator();
                while (iterator.hasNext()) {
                    ObjectNode objectNode = iterator.next();
                    JsonNode sysmlIdJsonNode = objectNode.get(MDKConstants.ID_KEY);
                    String sysmlId = sysmlIdJsonNode != null && sysmlIdJsonNode.isTextual() ? sysmlIdJsonNode.asText() : null;
                    if (MDUtils.isDeveloperMode()) {
                        System.out.println("[ATTEMPT 1] Attempting " + sysmlId);
                    }
                    Changelog.Change<Element> change = null;
                    try {
                        change = jsonToElementFunction.apply(objectNode, project, false);
                    } catch (ImportException | ReadOnlyElementException ignored) {
                    }
                    if (change == null || change.getChanged() == null) {
                        if (MDUtils.isDeveloperMode()) {
                            System.err.println("[FAILED 1] Could not create " + sysmlId);
                        }
                        // Element may fail to create on first pass, ex: Diagram (because owner doesn't exist yet + custom creation), so we need to retry after everything else.
                        retryObjectNodes.add(objectNode);
                        //failedElementMap.put(new Pair<>(Converters.getIdToElementConverter().apply(objectNode.get(MDKConstants.ID_KEY).asText(), project), objectNode), importException);
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
                        changeTypeMap.put(change.getChanged(), change.getType());
                    }

                    //changelog.addChange(Converters.getElementToIdConverter().apply(change.getChanged()), new Pair<>(change.getChanged(), objectNode), change.getType());
                    if (progressStatus != null) {
                        progressStatus.increase();
                    }
                }

                for (ObjectNode objectNode : retryObjectNodes) {
                    JsonNode sysmlIdJsonNode = objectNode.get(MDKConstants.ID_KEY);
                    String sysmlId = sysmlIdJsonNode != null && sysmlIdJsonNode.isTextual() ? sysmlIdJsonNode.asText() : null;
                    if (MDUtils.isDeveloperMode()) {
                        System.out.println("[ATTEMPT 1.5] Attempting " + sysmlId);
                    }
                    Changelog.Change<Element> change = null;
                    Exception exception = new ImportException(null, objectNode, "Failed to create/update element.");
                    try {
                        change = jsonToElementFunction.apply(objectNode, project, false);
                    } catch (ImportException | ReadOnlyElementException e) {
                        exception = e;
                    }
                    if (change == null || change.getChanged() == null) {
                        if (MDUtils.isDeveloperMode()) {
                            System.err.println("[FAILED 1.5] Could not create " + sysmlId);
                        }
                        Element element = Converters.getIdToElementConverter().apply(sysmlId, project);
                        failedElementMap.put(new Pair<>(element != null && !project.isDisposed(element) ? element : null, objectNode), exception);
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
                        changeTypeMap.put(change.getChanged(), change.getType());
                    }
                }

                iterator = objectNodes.iterator();
                while (iterator.hasNext()) {
                    ObjectNode objectNode = iterator.next();
                    JsonNode sysmlIdJsonNode = objectNode.get(MDKConstants.ID_KEY);
                    String sysmlId = sysmlIdJsonNode != null && sysmlIdJsonNode.isTextual() ? sysmlIdJsonNode.asText() : "<>";
                    if (MDUtils.isDeveloperMode()) {
                        System.out.println("[ATTEMPT 2] Attempting " + sysmlId);
                    }
                    Changelog.Change<Element> change = null;
                    Exception exception = new ImportException(null, objectNode, "Failed to create/update element with relationships.");
                    try {
                        change = jsonToElementFunction.apply(objectNode, project, true);
                    } catch (ImportException | ReadOnlyElementException e) {
                        exception = e;
                    }
                    if (change == null || change.getChanged() == null) {
                        if (MDUtils.isDeveloperMode()) {
                            System.err.println("[FAILED 2] Could not import " + sysmlId);
                        }
                        Element element = Converters.getIdToElementConverter().apply(sysmlId, project);
                        failedElementMap.put(new Pair<>(element != null && !project.isDisposed(element) ? element : null, objectNode), exception);
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
                        Changelog.ChangeType changeType = changeTypeMap.get(change.getChanged());
                        changelog.addChange(Converters.getElementToIdConverter().apply(change.getChanged()), new Pair<>(change.getChanged(), objectNode), changeType != null ? changeType : change.getType());
                    }

                    if (progressStatus != null) {
                        progressStatus.increase();
                    }
                }

                for (Changelog.ChangeType changeType : Changelog.ChangeType.values()) {
                    for (Map.Entry<String, Pair<Element, ObjectNode>> entry : changelog.get(changeType).entrySet()) {
                        Element element = entry.getValue().getKey();
                        ObjectNode objectNode = entry.getValue().getValue();

                        Collection<ModelValidationResult> results = validator.validateChanges(Collections.singleton(element));

                        if (results != null && !results.isEmpty()) {
                            ModelValidationResult result = results.iterator().next();
                            if (MDUtils.isDeveloperMode()) {
                                System.err.println("[FAILED 3] " + result.toString());
                            }
                            failedElementMap.put(new Pair<>(element != null && !project.isDisposed(element) ? element : null, objectNode), new ImportException(element, objectNode, "Element failed validation after importing. Reason: " + result.getReason()));
                            objectNodes.remove(objectNode);
                            continue bulkImport;
                        }
                        
                        // TODO: Need to see if we should/can reimplement this
                        // if (isElementInvalid(element)) {
                        //     if (MDUtils.isDeveloperMode()) {
                        //         JsonNode sysmlIdJsonNode = objectNode.get(MDKConstants.ID_KEY);
                        //         String sysmlId = sysmlIdJsonNode != null && sysmlIdJsonNode.isTextual() ? sysmlIdJsonNode.asText() : "<>";
                        //         System.err.println("[FAILED 4] Could not create " + sysmlId);
                        //     }
                        //     failedElementMap.put(new Pair<>(element != null && !project.isDisposed(element) ? element : null, objectNode), new ImportException(element, objectNode, "Element was found to be invalid after importing."));
                        //     objectNodes.remove(objectNode);
                        //     continue bulkImport;
                        // }
                        ObjectNode sourceObjectNode = Converters.getElementToJsonConverter().apply(element, project);
                        if (!JsonEquivalencePredicate.getInstance().test(sourceObjectNode, objectNode)) {
                            // currently handled as a warning instead of an error
                            nonEquivalentElements.put(element, objectNode);
                        }

                        if (progressStatus != null) {
                            progressStatus.increase();
                        }
                    }
                }
                break;
            }
        } finally {
            try {
                if (failedElementMap.isEmpty()) {
                    onSuccess();
                }
                else {
                    onFailure();
                }
                if (SessionManager.getInstance().isSessionCreated(project)) {
                    SessionManager.getInstance().closeSession(project);
                }

                if (progressStatus != null) {
                    progressStatus.setDescription(initialProgressStatusDescription);
                    progressStatus.setCurrent(initialProgressStatusCurrent);
                    progressStatus.setIndeterminate(initialProgressStatusIndeterminate);
                }
            } finally {
                if (SessionManager.getInstance().isSessionCreated(project)) {
                    SessionManager.getInstance().cancelSession(project);
                }
            }
        }
        return (changelog == null ? new Changelog<>() : changelog);
    }

    private static void preloadRecursively(EObject eObject) {
        for (final TreeIterator<Object> allProperContents = EcoreUtil.getAllProperContents(eObject, true); allProperContents.hasNext(); allProperContents.next()) {
            // just iterate to load contents
        }
    }

    public void onSuccess() {
    }

    public void onFailure() {
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

    public Map<Pair<Element, ObjectNode>, Exception> getFailedElementMap() {
        return failedElementMap;
    }

    public Map<Element, ObjectNode> getNonEquivalentElements() {
        return nonEquivalentElements;
    }
    
    private static RepositoryModelValidator getValidator(Project project) {
        String className = "RepositoryModelValidator";
        String packageName = "com.nomagic.magicdraw.uml.transaction";
        Class<?>[] formalParameters = { Project.class };
        Object[] effectiveParameters = new Object[] { project };
        try {
            Class<?> clazz = Class.forName(packageName + "." + className);
            try {
                Constructor<?> method = clazz.getConstructor(formalParameters);
                return (RepositoryModelValidator) method.newInstance(effectiveParameters);
            } catch (NoSuchMethodException e) {
                
                try {
                    Constructor<?>[] method = clazz.getConstructors();
                    effectiveParameters = new Object[] {};
                    return (RepositoryModelValidator) method[0].newInstance(effectiveParameters);
                }
                catch (Exception ex) {
                    Application.getInstance().getGUILog().log("Error accessing Cameo API (EMFBulkImporter.Shims)");
                    return null;
                }
            } catch (Exception e) {
                Application.getInstance().getGUILog().log("Error accessing Cameo API (EMFBulkImporter.Shims)");
                return null;
        }
        } catch (Exception e) {
            Application.getInstance().getGUILog().log("Error accessing Cameo API (EMFBulkImporter.Shims)");
            return null;
        }
    }
}

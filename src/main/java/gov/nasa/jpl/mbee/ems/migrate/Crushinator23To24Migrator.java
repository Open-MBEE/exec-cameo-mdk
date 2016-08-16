package gov.nasa.jpl.mbee.ems.migrate;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.task.ProgressStatus;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.DocGenPlugin;
import gov.nasa.jpl.mbee.api.docgen.PresentationElementType;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.sync.delta.SyncElements;
import gov.nasa.jpl.mbee.ems.sync.local.LocalSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.ems.sync.local.LocalSyncTransactionCommitListener;
import gov.nasa.jpl.mbee.ems.sync.queue.OutputQueue;
import gov.nasa.jpl.mbee.ems.sync.queue.Request;
import gov.nasa.jpl.mbee.ems.validation.ModelValidator;
import gov.nasa.jpl.mbee.generator.PresentationElementUtils;
import gov.nasa.jpl.mbee.lib.Pair;
import gov.nasa.jpl.mbee.lib.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

/**
 * This class migrates a MagicDraw project from EMS 2.3 to EMS 2.4
 */

public class Crushinator23To24Migrator extends Migrator {
    // https://cae-jira.jpl.nasa.gov/browse/MAGICDRAW-233
    // https://cae-jira.jpl.nasa.gov/browse/MAGICDRAW-277

    private static final List<String[]> VIEW_JSON_KEYS = Arrays.asList(
            new String[]{"sysmlid"},
            new String[]{"owner"},
            new String[]{"ownedAttribute"},
            /*new String[]{"specialization", "view2view"},*/
            new String[]{"specialization", "type"}
    ), DOCUMENT_JSON_KEYS = Arrays.asList(
            new String[]{"sysmlid"},
            new String[]{"owner"},
            new String[]{"ownedAttribute"},
            new String[]{"specialization", "view2view"},
            new String[]{"specialization", "type"}
    ), PROPERTY_JSON_KEYS = Arrays.asList(
            new String[]{"sysmlid"},
            new String[]{"owner"},
            new String[]{"specialization", "propertyType"},
            new String[]{"specialization", "aggregation"},
            new String[]{"specialization", "type"}
    ), ASSOCIATION_JSON_KEYS = Arrays.asList(
            new String[]{"sysmlid"},
            new String[]{"owner"},
            new String[]{"specialization", "ownedEnd"},
            new String[]{"specialization", "type"},
            new String[]{"specialization", "source"},
            new String[]{"specialization", "target"}
    ), PRESENTATION_ELEMENT_KEYS = Arrays.asList(
            new String[]{"sysmlid"},
            new String[]{"owner"},
            new String[]{"specialization"}
    );

    private Project project = Application.getInstance().getProject();
    private boolean failed;

    public Crushinator23To24Migrator() {
        // so it doesn't do the recursive collect in the Migrator constructor
    }

    public void migrate(ProgressStatus ps) {
        LocalSyncTransactionCommitListener listener = LocalSyncProjectEventListenerAdapter.getProjectMapping(project).getLocalSyncTransactionCommitListener();
        if (listener == null) {
            Application.getInstance().getGUILog().log("[ERROR] Local sync transaction commit listener not found. Aborting.");
            return;
        }

        List<InstanceSpecification> presentationElements = new ArrayList<>();
        for (PresentationElementType presentationElementType : PresentationElementType.values()) {
            Classifier presentationElementTypeClassifier = presentationElementType.getClassifier(project);
            if (presentationElementTypeClassifier == null) {
                Application.getInstance().getGUILog().log("[ERROR] " + presentationElementType.name() + " presentation element type classifier not found.");
                failed = true;
                continue;
            }
            presentationElements.addAll(presentationElementTypeClassifier.get_instanceSpecificationOfClassifier());
        }

        Map<String, JSONObject> elementJsonMap = new LinkedHashMap<>();
        Stereotype viewStereotype = Utils.getViewStereotype();
        Set<Element> views = new HashSet<>(),
                documents = new HashSet<>();
        Set<Property> properties = new HashSet<>();
        Set<Association> associations = new HashSet<>();
        Set<Element> elementsToDeleteLocally = new HashSet<>(), elementsToDeleteRemotely = new HashSet<>();
        if (viewStereotype == null) {
            Application.getInstance().getGUILog().log("[ERROR] Failed to find view stereotype.");
            failed = true;
        }
        else {
            views.addAll(StereotypesHelper.getExtendedElementsIncludingDerived(viewStereotype));
        }

        Stereotype documentStereotype = Utils.getDocumentStereotype();
        if (documentStereotype == null) {
            Application.getInstance().getGUILog().log("[ERROR] Failed to find document stereotype.");
            failed = true;
        }
        else {
            Collection<Element> documentElements = StereotypesHelper.getExtendedElementsIncludingDerived(documentStereotype);
            documents.addAll(documentElements);
            views.removeAll(documentElements);
        }

        Stereotype productStereotype = Utils.getProductStereotype();
        if (productStereotype == null) {
            Application.getInstance().getGUILog().log("[ERROR] Failed to find product stereotype.");
            failed = true;
        }
        else {
            Collection<Element> productElements = StereotypesHelper.getExtendedElementsIncludingDerived(productStereotype);
            documents.addAll(productElements);
            views.removeAll(productElements);
        }

        List<Element> viewsAndDocuments = new ArrayList<>(views.size() + documents.size());
        PresentationElementUtils presentationElementUtils = new PresentationElementUtils();
        viewsAndDocuments.addAll(views);
        viewsAndDocuments.addAll(documents);
        List<Package> viewInstancePackages = new ArrayList<>(viewsAndDocuments.size());
        for (Element element : viewsAndDocuments) {
            Constraint constraint = Utils.getViewConstraint(element);
            if (constraint != null) {
                elementsToDeleteLocally.add(constraint);
            }
            Package viewInstancePackage = presentationElementUtils.findViewInstancePackage(element);
            if (viewInstancePackage != null) {
                viewInstancePackages.add(viewInstancePackage);
                elementsToDeleteLocally.add(viewInstancePackage);
                elementsToDeleteRemotely.add(viewInstancePackage);
                for (Dependency dependency : viewInstancePackage.getSupplierDependency()) {
                    if (StereotypesHelper.hasStereotype(dependency, presentationElementUtils.getPresentsStereotype())) {
                        elementsToDeleteLocally.add(dependency);
                        elementsToDeleteRemotely.add(dependency);
                    }
                }
            }
            if (element instanceof Class) {
                properties.addAll(((Class) element).getOwnedAttribute());
            }
        }
        BaseElement viewInstancesPackage = project.getElementByID(project.getPrimaryProject().getProjectID().replace("PROJECT", "View_Instances"));
        if (viewInstancesPackage instanceof Element) {
            elementsToDeleteLocally.add((Element) viewInstancesPackage);
            elementsToDeleteRemotely.add((Element) viewInstancesPackage);
        }
        BaseElement unusedViewInstancePackage = project.getElementByID(project.getPrimaryProject().getProjectID().replace("PROJECT", "Unused_View_Instances"));
        if (unusedViewInstancePackage instanceof Element) {
            elementsToDeleteLocally.add((Element) unusedViewInstancePackage);
            elementsToDeleteRemotely.add((Element) unusedViewInstancePackage);
        }
        // cannot confirm validity of 2.3- changelogs and lots of unneeded blocks
        Package syncPackage = SyncElements.getSyncPackage(project);
        if (syncPackage != null) {
            elementsToDeleteLocally.add(syncPackage);
        }

        for (Property property : properties) {
            associations.add(property.getAssociation());
        }

        Map<String, Pair<List<String[]>, Set<? extends Element>>> keyPathMapping = new HashMap<>();
        keyPathMapping.put("view", new Pair<List<String[]>, Set<? extends Element>>(VIEW_JSON_KEYS, views));
        keyPathMapping.put("document", new Pair<List<String[]>, Set<? extends Element>>(DOCUMENT_JSON_KEYS, documents));
        keyPathMapping.put("property", new Pair<List<String[]>, Set<? extends Element>>(PROPERTY_JSON_KEYS, properties));
        keyPathMapping.put("association", new Pair<List<String[]>, Set<? extends Element>>(ASSOCIATION_JSON_KEYS, associations));

        for (Map.Entry<String, Pair<List<String[]>, Set<? extends Element>>> entry : keyPathMapping.entrySet()) {
            for (Element element : entry.getValue().getSecond()) {
                if (!ExportUtility.shouldAdd(element)) {
                    continue;
                }
                if (ProjectUtilities.isElementInAttachedProject(element)) {
                    continue;
                }
                JSONObject jsonObject = ExportUtility.fillElement(element, null);
                Object o;
                if (entry.getKey().equals("document") && (o = jsonObject.get("specialization")) instanceof JSONObject) {
                    ((JSONObject) o).put("view2view", null);
                }
                if (jsonObject == null) {
                    Application.getInstance().getGUILog().log("[ERROR] Failed to serialize " + entry.getKey() + " " + element.getID() + ".");
                    failed = true;
                    continue;
                }
                jsonObject = transform(jsonObject, entry.getValue().getFirst());
                if (jsonObject == null) {
                    Application.getInstance().getGUILog().log("[ERROR] Failed to transform " + entry.getKey() + " " + element.getID() + ".");
                    failed = true;
                    continue;
                }
                elementJsonMap.put(ExportUtility.getElementID(element), jsonObject);
            }
        }

        for (Package viewInstancePackage : viewInstancePackages) {
            JSONObject jsonObject = ExportUtility.fillElement(viewInstancePackage, null);
            if (jsonObject == null) {
                Application.getInstance().getGUILog().log("[ERROR] Failed to serialize view instance package " + viewInstancePackage.getID() + ".");
                failed = true;
                continue;
            }
            Object o = jsonObject.get("sysmlid");
            if (o instanceof String) {
                String newSysmlid = ModelValidator.HIDDEN_ID_PREFIX + o;
                jsonObject.put("sysmlid", newSysmlid);
                jsonObject.put("owner", null);
                elementJsonMap.put(newSysmlid, jsonObject);
            }
        }

        for (InstanceSpecification presentationElement : presentationElements) {
            JSONObject jsonObject = ExportUtility.fillElement(presentationElement, null);
            if (jsonObject == null) {
                Application.getInstance().getGUILog().log("[ERROR] Failed to serialize presentation element " + presentationElement.getID() + ".");
                failed = true;
                continue;
            }
            Object o = jsonObject.get("owner");
            if (o instanceof String) {
                String newOwnerId = ModelValidator.HIDDEN_ID_PREFIX + o;
                jsonObject.put("owner", elementJsonMap.containsKey(newOwnerId) ? newOwnerId : null);
                jsonObject = transform(jsonObject, PRESENTATION_ELEMENT_KEYS);
                if (jsonObject == null) {
                    Application.getInstance().getGUILog().log("[ERROR] Failed to transform presentation element " + presentationElement.getID() + ".");
                    failed = true;
                    continue;
                }
                elementJsonMap.put(ExportUtility.getElementID(presentationElement), jsonObject);
            }
        }

        if (!elementJsonMap.isEmpty()) {
            JSONArray jsonArray = new JSONArray();
            jsonArray.addAll(elementJsonMap.values());

            JSONObject body = new JSONObject();
            body.put("elements", jsonArray);
            // Intentionally not sending source so that it is not skipped on Coordinated Sync. Otherwise holding_bin intermediate packages won't be created, which could cause errors if non-hidden elements are created there.
            //send.put("source", "magicdraw");
            body.put("mmsVersion", DocGenPlugin.VERSION);


            String url = ExportUtility.getPostElementsUrl();
            Application.getInstance().getGUILog().log("[INFO] Queueing request to update " + elementJsonMap.size() + " element" + (elementJsonMap.size() != 1 ? "s" : "") + " on the MMS.");
            //System.out.println(body);
            OutputQueue.getInstance().offer(new Request(url, body.toJSONString(), jsonArray.size(), "Migration Creations/Updates", false));
        }

        if (!elementsToDeleteRemotely.isEmpty()) {
            JSONArray jsonArray = new JSONArray();
            for (Element element : elementsToDeleteRemotely) {
                JSONObject elementJsonObject = new JSONObject();
                elementJsonObject.put("sysmlid", ExportUtility.getElementID(element));
                jsonArray.add(elementJsonObject);
            }
            JSONObject body = new JSONObject();
            body.put("elements", jsonArray);
            body.put("source", "magicdraw");
            body.put("mmsVersion", DocGenPlugin.VERSION);
            Application.getInstance().getGUILog().log("[INFO] Queuing request to delete " + jsonArray.size() + " element" + (jsonArray.size() != 1 ? "s" : "") + " on the MMS.");
            //System.out.println(body);
            OutputQueue.getInstance().offer(new Request(ExportUtility.getUrlWithWorkspace() + "/elements", body.toJSONString(), "DELETEALL", true, jsonArray.size(), "Migration Deletes"));
        }

        listener.setDisabled(true);

        if (!presentationElements.isEmpty()) {
            Application.getInstance().getGUILog().log("[INFO] Deleting " + presentationElements.size() + " client-side presentation element" + (presentationElements.size() != 1 ? "s" : "") + ".");
            if (!SessionManager.getInstance().isSessionCreated(project)) {
                SessionManager.getInstance().createSession(project, "2.3 to 2.4 Migration");
            }
            for (Element presentationElement : presentationElements) {
                try {
                    ModelElementsManager.getInstance().removeElement(presentationElement);
                } catch (ReadOnlyElementException ignored) {
                    failed = true;
                    Application.getInstance().getGUILog().log("[ERROR] Failed to delete read-only element " + presentationElement.getID() + ".");
                }
            }
        }

        if (!elementsToDeleteLocally.isEmpty()) {
            Application.getInstance().getGUILog().log("[INFO] Deleting " + elementsToDeleteLocally.size() + " client-side view related element" + (elementsToDeleteLocally.size() != 1 ? "s" : "") + ".");
            if (!SessionManager.getInstance().isSessionCreated(project)) {
                SessionManager.getInstance().createSession(project, "2.3 to 2.4 Migration");
            }
            for (Element element : elementsToDeleteLocally) {
                try {
                    ModelElementsManager.getInstance().removeElement(element);
                } catch (ReadOnlyElementException ignored) {
                    failed = true;
                    Application.getInstance().getGUILog().log("[ERROR] Failed to delete read-only element " + element.getID() + ".");
                }
            }
        }

        if (SessionManager.getInstance().isSessionCreated()) {
            SessionManager.getInstance().closeSession();
        }
        listener.setDisabled(false);

        if (failed) {
            Application.getInstance().getGUILog().log("[ERROR] Migration failed! Please see log messages above for more details. Please restart the migration once the errors are resolved.");
        }
        else {
            Application.getInstance().getGUILog().log("[INFO] " + ExportUtility.getProjectId(project) + " successfully migrated from 2.3 to 2.4.");
        }
    }

    private JSONObject transform(JSONObject originalJsonObject, List<String[]> keyPaths) {
        JSONObject jsonObject = new JSONObject();
        for (String[] keyPath : keyPaths) {
            JSONObject originalOwnerJsonObject = originalJsonObject,
                    ownerJsonObject = jsonObject;
            for (int i = 0; i < keyPath.length; i++) {
                String key = keyPath[i];
                if (i == keyPath.length - 1) {
                    ownerJsonObject.put(key, originalOwnerJsonObject.get(key));
                }
                else {
                    Object o = originalOwnerJsonObject.get(key);
                    if (!(o instanceof JSONObject)) {
                        return null;
                    }
                    originalOwnerJsonObject = (JSONObject) originalOwnerJsonObject.get(key);
                    JSONObject newOwnerJsonObject = (o = ownerJsonObject.get(key)) instanceof JSONObject ? ((JSONObject) o) : new JSONObject();
                    ownerJsonObject.put(key, newOwnerJsonObject);
                    ownerJsonObject = newOwnerJsonObject;
                }
            }
        }
        return jsonObject;
    }
}

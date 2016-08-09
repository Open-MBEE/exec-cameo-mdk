package gov.nasa.jpl.mbee.ems.migrate;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.task.ProgressStatus;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.api.docgen.PresentationElementType;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.sync.local.LocalSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.ems.sync.local.LocalSyncTransactionCommitListener;
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
        listener.setDisabled(true);

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

        Map<String, JSONObject> elementJsonMap = new HashMap<>();
        Stereotype viewStereotype = Utils.getViewStereotype();
        Set<Element> views = new HashSet<>(),
                documents = new HashSet<>();
        Set<Property> properties = new HashSet<>();
        Set<Association> associations = new HashSet<>();
        Set<Constraint> constraints = new HashSet<>();
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
        viewsAndDocuments.addAll(views);
        viewsAndDocuments.addAll(documents);
        for (Element element : viewsAndDocuments) {
            Constraint constraint = Utils.getViewConstraint(element);
            if (constraint != null) {
                constraints.add(constraint);
            }
            if (element instanceof Class) {
                properties.addAll(((Class) element).getOwnedAttribute());
            }
        }

        for (Property property : properties) {
            associations.add(property.getAssociation());
        }

        if (!constraints.isEmpty()) {
            Application.getInstance().getGUILog().log("[INFO] Deleting " + constraints.size() + " client-side view constraint" + (constraints.size() != 1 ? "s" : "") + ".");
            if (!SessionManager.getInstance().isSessionCreated(project)) {
                SessionManager.getInstance().createSession(project, "2.3 to 2.4 Migration");
            }
            for (Element constraint : constraints) {
                try {
                    ModelElementsManager.getInstance().removeElement(constraint);
                } catch (ReadOnlyElementException ignored) {
                    failed = true;
                    Application.getInstance().getGUILog().log("[ERROR] Failed to delete read-only element " + constraint.getID() + ".");
                }
            }
        }

        if (SessionManager.getInstance().isSessionCreated()) {
            SessionManager.getInstance().closeSession();
        }
        listener.setDisabled(false);

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

        if (!elementJsonMap.isEmpty()) {
            JSONArray jsonArray = new JSONArray();
            jsonArray.addAll(elementJsonMap.values());
            commit(jsonArray);
        }

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

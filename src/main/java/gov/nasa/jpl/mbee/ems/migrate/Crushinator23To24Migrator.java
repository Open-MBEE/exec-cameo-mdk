package gov.nasa.jpl.mbee.ems.migrate;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import com.nomagic.magicdraw.teamwork2.locks.LockService;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.task.ProgressStatus;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.DocGenPlugin;
import gov.nasa.jpl.mbee.actions.systemsreasoner.SRAction;
import gov.nasa.jpl.mbee.api.docgen.presentation_elements.PresentationElementEnum;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.ServerException;
import gov.nasa.jpl.mbee.ems.jms.JMSUtils;
import gov.nasa.jpl.mbee.ems.sync.delta.SyncElements;
import gov.nasa.jpl.mbee.ems.sync.local.LocalSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.ems.sync.local.LocalSyncTransactionCommitListener;
import gov.nasa.jpl.mbee.ems.sync.queue.OutputQueue;
import gov.nasa.jpl.mbee.ems.sync.queue.Request;
import gov.nasa.jpl.mbee.ems.sync.status.SyncStatusConfigurator;
import gov.nasa.jpl.mbee.ems.validation.ModelValidator;
import gov.nasa.jpl.mbee.generator.PresentationElementUtils;
import gov.nasa.jpl.mbee.lib.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.annotation.CheckForNull;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

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
    private static final int MAX_ELEMENTS_PER_REQUEST = 99;

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

        if (project.isTeamworkServerProject()) {
            Collection<String> lockedElementUsers = LockService.getLockService(project).getLockedElementUsers();
            lockedElementUsers.remove(TeamworkUtils.getLoggedUserName());
            if (!lockedElementUsers.isEmpty()) {
                Application.getInstance().getGUILog().log("[ERROR] Model has locks from " + NumberFormat.getInstance().format(lockedElementUsers.size()) + " other users. Aborting.");
                return;
            }
            if (!TeamworkUtils.lockElement(project, project.getModel(), true)) {
                Application.getInstance().getGUILog().log("[ERROR] Failed to lock model recursively. Aborting.");
                return;
            }
        }

        String postUrl = ExportUtility.getPostElementsUrl();
        String deleteUrl = ExportUtility.getUrlWithWorkspace() + "/elements";
        if (postUrl == null) {
            Application.getInstance().getGUILog().log("[ERROR] Could not build post URL. Aborting.");
            return;
        }
        try {
            String originUrl = JMSUtils.getJMSInfo(project).getUrl();
            String site = ExportUtility.getSite();
            if (originUrl != null && site != null) {
                originUrl = originUrl.replace("tcp://", site.startsWith("http://") ? "http://" : "https://");
                originUrl = originUrl.replaceFirst(":\\d+", "");
                Application.getInstance().getGUILog().log("[INFO] Found origin domain: " + originUrl);
                String pattern = "http(s?):\\/\\/.+?(?=\\/)";
                postUrl = postUrl.replaceFirst(pattern, originUrl);
                deleteUrl = deleteUrl.replaceFirst(pattern, originUrl);
                Application.getInstance().getGUILog().log("[INFO] New post URL: " + postUrl);
                Application.getInstance().getGUILog().log("[INFO] New delete URL: " + deleteUrl);
            }
        } catch (ServerException e) {
            e.printStackTrace();
        }

        boolean segmented = JOptionPane.showConfirmDialog(null,
                "Would you like the migration to pause between each stage? This will allow \n" +
                        "the opportunity to review the results of each stage independently, but will \n" +
                        "require manual intervention to proceed.", "Segmented Migration", JOptionPane.YES_NO_OPTION, JOptionPane.NO_OPTION) == JOptionPane.YES_OPTION;

        List<InstanceSpecification> presentationElements = new ArrayList<>();
        for (PresentationElementEnum presentationElementEnum : PresentationElementEnum.values()) {
            Classifier presentationElementTypeClassifier = presentationElementEnum.get().apply(project);
            if (presentationElementTypeClassifier == null) {
                Application.getInstance().getGUILog().log("[ERROR] " + presentationElementEnum.name() + " presentation element type classifier not found.");
                failed = true;
                continue;
            }
            presentationElements.addAll(presentationElementTypeClassifier.get_instanceSpecificationOfClassifier());
        }

        //Map<String, JSONObject> elementJsonMap = new LinkedHashMap<>();
        Stereotype viewStereotype = Utils.getViewStereotype();
        Set<Element> views = new LinkedHashSet<>(),
                documents = new LinkedHashSet<>();
        Set<Property> properties = new LinkedHashSet<>();
        Set<Association> associations = new LinkedHashSet<>();
        Set<Element> elementsToDeleteLocally = new LinkedHashSet<>(), elementsToDeleteRemotely = new LinkedHashSet<>();
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

        String holdingBinId = "holding_bin_" + project.getPrimaryProject().getProjectID();

        List<Element> viewsAndDocuments = new ArrayList<>(views.size() + documents.size());
        PresentationElementUtils presentationElementUtils = new PresentationElementUtils();
        viewsAndDocuments.addAll(views);
        viewsAndDocuments.addAll(documents);
        List<Package> viewInstancePackages = new ArrayList<>(viewsAndDocuments.size() + 2);
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
        if (viewInstancesPackage instanceof Package) {
            viewInstancePackages.add((Package) viewInstancesPackage);
            elementsToDeleteLocally.add((Package) viewInstancesPackage);
            elementsToDeleteRemotely.add((Package) viewInstancesPackage);
        }
        BaseElement unusedViewInstancePackage = project.getElementByID(project.getPrimaryProject().getProjectID().replace("PROJECT", "Unused_View_Instances"));
        if (unusedViewInstancePackage instanceof Package) {
            viewInstancePackages.add((Package) unusedViewInstancePackage);
            elementsToDeleteLocally.add((Package) unusedViewInstancePackage);
            elementsToDeleteRemotely.add((Package) unusedViewInstancePackage);
        }
        // cannot confirm validity of 2.3- changelogs and lots of unneeded blocks
        Package syncPackage = SyncElements.getSyncPackage(project);
        if (syncPackage != null) {
            elementsToDeleteLocally.add(syncPackage);
        }

        for (Property property : properties) {
            associations.add(property.getAssociation());
        }
        for (InstanceSpecification presentationElement : presentationElements) {
            if (!presentationElement.getID().startsWith("MMS") && !presentationElement.has_instanceValueOfInstance()) {
                elementsToDeleteRemotely.add(presentationElement);
            }
        }

        List<JSONObject> elementJsonObjects = new ArrayList<>(documents.size() + views.size() + properties.size() + associations.size());
        for (Element document : documents) {
            JSONObject jsonObject = convertElementToPartialJson(document, DOCUMENT_JSON_KEYS);
            Object o;
            if (jsonObject != null && (o = jsonObject.get("specialization")) instanceof JSONObject) {
                ((JSONObject) o).put("view2view", null);
                elementJsonObjects.add(jsonObject);
            }
        }
        for (Element view : views) {
            JSONObject jsonObject = convertElementToPartialJson(view, VIEW_JSON_KEYS);
            if (jsonObject != null) {
                elementJsonObjects.add(jsonObject);
            }
        }
        for (Property property : properties) {
            JSONObject jsonObject = convertElementToPartialJson(property, PROPERTY_JSON_KEYS);
            if (jsonObject != null) {
                elementJsonObjects.add(jsonObject);
            }
        }
        for (Association association : associations) {
            JSONObject jsonObject = convertElementToPartialJson(association, ASSOCIATION_JSON_KEYS);
            if (jsonObject != null) {
                elementJsonObjects.add(jsonObject);
            }
        }
        if (!elementJsonObjects.isEmpty()) {
            ps.setDescription("Updating " + elementJsonObjects.size() + " element" + (elementJsonObjects.size() != 1 ? "s" : "") + " on the MMS");

            JSONArray jsonArray = new JSONArray();
            jsonArray.addAll(elementJsonObjects);
            JSONObject body = new JSONObject();
            body.put("elements", jsonArray);
            body.put("source", "magicdraw");
            body.put("mmsVersion", DocGenPlugin.VERSION);
            ExportUtility.send(postUrl, body.toJSONString(), false, false);
            if (ps.isCancel()) {
                handleCancel();
                return;
            }
            Application.getInstance().getGUILog().log("[INFO] Updated " + elementJsonObjects.size() + " element" + (elementJsonObjects.size() != 1 ? "s" : "") + " on the MMS.");
        }

        if (segmented) {
            pause();
        }

        Map<String, JSONObject> hiddenViewInstancePackageJsonObjects = new LinkedHashMap<>(viewInstancePackages.size());
        for (Package viewInstancePackage : viewInstancePackages) {
            if (ProjectUtilities.isElementInAttachedProject(viewInstancePackage)) {
                continue;
            }
            JSONObject jsonObject = ExportUtility.fillElement(viewInstancePackage, null);
            if (jsonObject == null) {
                Application.getInstance().getGUILog().log("[ERROR] Failed to serialize Presentation Element Package " + viewInstancePackage.getID() + ".");
                failed = true;
                continue;
            }
            Object o = jsonObject.get("sysmlid");
            if (o instanceof String) {
                String newSysmlid = ModelValidator.HIDDEN_ID_PREFIX + o;
                jsonObject.put("sysmlid", newSysmlid);
                jsonObject.put("owner", holdingBinId);
                hiddenViewInstancePackageJsonObjects.put(newSysmlid, jsonObject);
            }
        }
        if (!hiddenViewInstancePackageJsonObjects.isEmpty()) {
            sendStaggered(postUrl, hiddenViewInstancePackageJsonObjects.values(), "Presentation Element Package", "Presentation Element Packages", ps, true);
            if (ps.isCancel()) {
                return;
            }
        }

        if (segmented) {
            pause();
        }

        List<JSONObject> presentationElementJsonObjects = new ArrayList<>(presentationElements.size());
        for (InstanceSpecification presentationElement : presentationElements) {
            if (ProjectUtilities.isElementInAttachedProject(presentationElement)) {
                continue;
            }
            JSONObject jsonObject = ExportUtility.fillElement(presentationElement, null);
            if (jsonObject == null) {
                Application.getInstance().getGUILog().log("[ERROR] Failed to serialize Presentation Element " + presentationElement.getID() + ".");
                failed = true;
                continue;
            }
            Object o = jsonObject.get("owner");
            if (o instanceof String) {
                String newOwnerId = ModelValidator.HIDDEN_ID_PREFIX + o;
                jsonObject.put("owner", hiddenViewInstancePackageJsonObjects.containsKey(newOwnerId) ? newOwnerId : holdingBinId);
                jsonObject = transform(jsonObject, PRESENTATION_ELEMENT_KEYS);
                if (jsonObject == null) {
                    Application.getInstance().getGUILog().log("[ERROR] Failed to transform Presentation Element " + presentationElement.getID() + ".");
                    failed = true;
                    continue;
                }
                presentationElementJsonObjects.add(jsonObject);
            }
        }
        if (!presentationElementJsonObjects.isEmpty()) {
            // Intentionally not sending source so that it is not skipped on Coordinated Sync. Otherwise holding_bin intermediate packages won't be created, which could cause errors if non-hidden elements are created there.
            sendStaggered(postUrl, presentationElementJsonObjects, "Presentation Element", "Presentation Elements", ps, true);
            if (ps.isCancel()) {
                return;
            }
        }

        if (segmented) {
            pause();
        }

        Iterator<Element> elementsToDeleteRemotelyIterator = elementsToDeleteRemotely.iterator();
        while (elementsToDeleteRemotelyIterator.hasNext()) {
            if (ProjectUtilities.isElementInAttachedProject(elementsToDeleteRemotelyIterator.next())) {
                elementsToDeleteRemotelyIterator.remove();
            }
        }

        if (!elementsToDeleteRemotely.isEmpty()) {
            int total = 0;
            String status = "Deleting " + elementsToDeleteRemotely.size() + " legacy Presentation Element Package" + (elementsToDeleteRemotely.size() != 1 ? "s" : "") + "/<<Presents>> Dependenc" + (elementsToDeleteRemotely.size() != 1 ? "ies" : "y") + "/Unused Presentation Element" + (elementsToDeleteRemotely.size() != 1 ? "s" : "") + " on the MMS";
            Queue<Element> elementJsonQueue = new LinkedBlockingQueue<>(elementsToDeleteRemotely);
            ps.setIndeterminate(false);
            ps.setCurrent(0);
            ps.setMax(elementsToDeleteRemotely.size() / MAX_ELEMENTS_PER_REQUEST + 1);
            while (ps.getCurrent() < ps.getMax()) {
                if (ps.isCancel()) {
                    handleCancel();
                    return;
                }
                ps.setDescription(status + " (" + total + "/" + elementsToDeleteRemotely.size() + ")");
                JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < MAX_ELEMENTS_PER_REQUEST && !elementJsonQueue.isEmpty(); i++) {
                    JSONObject elementJsonObject = new JSONObject();
                    elementJsonObject.put("sysmlid", ExportUtility.getElementID(elementJsonQueue.poll()));
                    jsonArray.add(elementJsonObject);
                }
                JSONObject body = new JSONObject();
                body.put("elements", jsonArray);
                body.put("source", "magicdraw");
                body.put("mmsVersion", DocGenPlugin.VERSION);
                //Application.getInstance().getGUILog().log("[INFO] Queuing request to delete " + jsonArray.size() + " element" + (jsonArray.size() != 1 ? "s" : "") + " on the MMS.");
                //System.out.println(body);
                //OutputQueue.getInstance().offer(new Request(ExportUtility.getUrlWithWorkspace() + "/elements", body.toJSONString(), "DELETEALL", true, jsonArray.size(), "Migration Deletes"));
                ExportUtility.deleteWithBody(deleteUrl, body.toJSONString(), true);

                ps.increase();
                total += jsonArray.size();
            }
            ps.setDescription(null);
            ps.setIndeterminate(true);
            Application.getInstance().getGUILog().log("[INFO] Deleted " + elementsToDeleteRemotely.size() + " legacy Presentation Element Package" + (elementsToDeleteRemotely.size() != 1 ? "s" : "") + "/Presents Dependenc" + (elementsToDeleteRemotely.size() != 1 ? "ies" : "y") + "/Unused Presentation Element" + (elementsToDeleteRemotely.size() != 1 ? "s" : "") + " on the MMS.");
        }

        // POINT OF NO RETURN; NO MORE CANCELLING

        listener.setDisabled(true);

        Iterator<InstanceSpecification> presentationElementsIterator = presentationElements.iterator();
        while (presentationElementsIterator.hasNext()) {
            if (ProjectUtilities.isElementInAttachedProject(presentationElementsIterator.next())) {
                presentationElementsIterator.remove();
            }
        }

        if (!presentationElements.isEmpty()) {
            Application.getInstance().getGUILog().log("[INFO] Deleting " + presentationElements.size() + " client-side presentation element" + (presentationElements.size() != 1 ? "s" : "") + ".");
            if (!SessionManager.getInstance().isSessionCreated(project)) {
                SessionManager.getInstance().createSession(project, "C-3 to C-4 Migration");
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

        Iterator<Element> elementsToDeleteLocallyIterator = elementsToDeleteLocally.iterator();
        while (elementsToDeleteLocallyIterator.hasNext()) {
            if (ProjectUtilities.isElementInAttachedProject(elementsToDeleteLocallyIterator.next())) {
                elementsToDeleteLocallyIterator.remove();
            }
        }

        if (!elementsToDeleteLocally.isEmpty()) {
            Application.getInstance().getGUILog().log("[INFO] Deleting " + elementsToDeleteLocally.size() + " client-side View related element" + (elementsToDeleteLocally.size() != 1 ? "s" : "") + ".");
            if (!SessionManager.getInstance().isSessionCreated(project)) {
                SessionManager.getInstance().createSession(project, "C-3 to C-4 Migration");
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
        SyncStatusConfigurator.getSyncStatusAction().update();

        if (failed) {
            Application.getInstance().getGUILog().log("[ERROR] Migration failed! Please see log messages above for more details. Please restart the migration once the errors are resolved.");
        }
        else {
            Application.getInstance().getGUILog().log("[INFO] Project " + project.getName() + " successfully migrated from C-3 to C-4.");
            Application.getInstance().getGUILog().log("[INFO] Please review the contents of the notification window to verify and then " + (project.isTeamworkServerProject() ? "commit to Teamwork" : "save") + " without making further modification.");
            Application.getInstance().getGUILog().log("[INFO] In the case that an error occurred, close the project abandoning all changes. Then re-open the project and restart the migration.");
        }

        saveLoggedMessages();
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

    private JSONObject convertElementToPartialJson(Element element, List<String[]> keyPaths) {
        if (!ExportUtility.shouldAdd(element)) {
            return null;
        }
        if (ProjectUtilities.isElementInAttachedProject(element)) {
            return null;
        }
        JSONObject jsonObject = ExportUtility.fillElement(element, null);
        Object o;
        /*if (entry.getKey().equals("document") && (o = jsonObject.get("specialization")) instanceof JSONObject) {
            ((JSONObject) o).put("view2view", null);
        }*/
        if (jsonObject == null) {
            Application.getInstance().getGUILog().log("[ERROR] Failed to serialize " + element.getID() + ".");
            failed = true;
            return null;
        }
        jsonObject = transform(jsonObject, keyPaths);
        if (jsonObject == null) {
            Application.getInstance().getGUILog().log("[ERROR] Failed to transform " + element.getID() + ".");
            failed = true;
            return null;
        }
        return jsonObject;
    }

    private void handleCancel() {
        Application.getInstance().getGUILog().log("[WARNING] Migration manually cancelled by user. Please note that the migration has not completed and will need to be ran again.");
        failed = true;
    }

    private void sendStaggered(String url, Collection<JSONObject> jsonObjects, String elementType, String elementTypePlural, ProgressStatus progressStatus, boolean suppressSource) {
        if (!jsonObjects.isEmpty()) {
            int total = 0;
            String status = "Updating " + (jsonObjects.size() != 1 ? elementTypePlural : elementType) + " on the MMS";
            Queue<JSONObject> elementJsonQueue = new LinkedBlockingQueue<>(jsonObjects);
            progressStatus.setIndeterminate(false);
            progressStatus.setCurrent(0);
            progressStatus.setMax(jsonObjects.size() / MAX_ELEMENTS_PER_REQUEST + 1);
            while (progressStatus.getCurrent() < progressStatus.getMax()) {
                progressStatus.setDescription(status + " (" + total + "/" + jsonObjects.size() + ")");
                JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < MAX_ELEMENTS_PER_REQUEST && !elementJsonQueue.isEmpty(); i++) {
                    jsonArray.add(elementJsonQueue.poll());
                }

                JSONObject body = new JSONObject();
                body.put("elements", jsonArray);
                if (!suppressSource) {
                    body.put("source", "magicdraw");
                }
                body.put("mmsVersion", DocGenPlugin.VERSION);
                //Application.getInstance().getGUILog().log("[INFO] Queueing request to update " + elementJsonMap.size() + " element" + (elementJsonMap.size() != 1 ? "s" : "") + " on the MMS.");
                //System.out.println(body);
                //OutputQueue.getInstance().offer(new Request(url, body.toJSONString(), jsonArray.size(), "Migration Creations/Updates", false));
                ExportUtility.send(url, body.toJSONString(), false, false);
                if (progressStatus.isCancel()) {
                    handleCancel();
                    return;
                }

                progressStatus.increase();
                total += jsonArray.size();
            }

            progressStatus.setDescription(null);
            progressStatus.setIndeterminate(true);
            Application.getInstance().getGUILog().log("[INFO] Updated " + jsonObjects.size() + " " + (jsonObjects.size() != 1 ? elementTypePlural : elementType) + " on the MMS.");
        }
    }

    private File saveLoggedMessages() {
        try {
            Path path = Paths.get(System.getProperty("user.home"), "md_notification_window_" + System.currentTimeMillis() + ".html");
            Files.write(path, Application.getInstance().getGUILog().getLoggedMessages().getBytes());
            return path.toFile();
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void pause() {
        File log = saveLoggedMessages();
        JOptionPane.showConfirmDialog(null, "Migration paused. Press OK or close window to continue. " +
                (log != null ? "\nLogged messages: " + log.getAbsolutePath() : ""), "Migration", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
    }

    public Crushinator23to24DocumentMigratorAction createAction(Element element) {
        return new Crushinator23to24DocumentMigratorAction(element);
    }

    public class Crushinator23to24DocumentMigratorAction extends SRAction {

        public static final String NAME = "Migrate Document (C-3 to C-4)";

        public Crushinator23to24DocumentMigratorAction(Element element) {
            super(NAME, element);
        }

        @Override
        public void actionPerformed(@CheckForNull ActionEvent actionEvent) {
            Stereotype productStereotype = Utils.getProductStereotype();
            if (productStereotype == null) {
                Application.getInstance().getGUILog().log("[ERROR] Product stereotype not found. Cancelling document migraiton.");
                return;
            }
            Stereotype viewStereotype = Utils.getViewStereotype();
            if (viewStereotype == null) {
                Application.getInstance().getGUILog().log("[ERROR] View stereotype not found. Cancelling document migration.");
                return;
            }

            Collection<Property> properties = new LinkedHashSet<>();
            Collection<Association> associations = new LinkedHashSet<>();
            Collection<Classifier> views = new LinkedHashSet<>();
            collectRelatedElementsRecursively(element, viewStereotype, properties, associations, views);

            List<JSONObject> elementJsonObjects = new ArrayList<>(views.size() + properties.size() + associations.size());
            for (Classifier view : views) {
                boolean isDocument = StereotypesHelper.checkForDerivedStereotype(StereotypesHelper.getStereotypes(view), productStereotype) != null;
                JSONObject jsonObject = convertElementToPartialJson(view, isDocument ? DOCUMENT_JSON_KEYS : VIEW_JSON_KEYS);
                Object o;
                if (isDocument) {
                    if (jsonObject != null && (o = jsonObject.get("specialization")) instanceof JSONObject) {
                        ((JSONObject) o).put("view2view", null);
                    }
                }
                if (jsonObject != null) {
                    elementJsonObjects.add(jsonObject);
                }
            }
            for (Property property : properties) {
                JSONObject jsonObject = convertElementToPartialJson(property, PROPERTY_JSON_KEYS);
                if (jsonObject != null) {
                    elementJsonObjects.add(jsonObject);
                }
            }
            for (Association association : associations) {
                JSONObject jsonObject = convertElementToPartialJson(association, ASSOCIATION_JSON_KEYS);
                if (jsonObject != null) {
                    elementJsonObjects.add(jsonObject);
                }
            }

            if (!elementJsonObjects.isEmpty()) {
                JSONArray jsonArray = new JSONArray();
                jsonArray.addAll(elementJsonObjects);
                JSONObject body = new JSONObject();
                body.put("elements", jsonArray);
                body.put("source", "magicdraw");
                body.put("mmsVersion", DocGenPlugin.VERSION);
                //ExportUtility.send(postUrl, body.toJSONString(), false, false);
                OutputQueue.getInstance().offer(new Request(ExportUtility.getPostElementsUrl(), body.toJSONString(), "Migrate Document"));
                Application.getInstance().getGUILog().log("[INFO] Updating " + elementJsonObjects.size() + " element" + (elementJsonObjects.size() != 1 ? "s" : "") + " on the MMS.");
            }
        }

        private void collectRelatedElementsRecursively(Element element, Stereotype viewStereotype, Collection<Property> properties, Collection<Association> associations, Collection<Classifier> views) {
            List<Stereotype> stereotypes = StereotypesHelper.getStereotypes(element);
            if (!(element instanceof Classifier) || StereotypesHelper.checkForDerivedStereotype(stereotypes, viewStereotype) == null) {
                Application.getInstance().getGUILog().log("[INFO] " + element.getHumanName() + " - " + element.getID() + " - is not a view. Skipping view in document migration.");
                return;
            }
            Classifier view = (Classifier) element;
            if (views.contains(view)) {
                Application.getInstance().getGUILog().log("[WARNING] Detected circular reference in view structure to " + view.getHumanName() + " - " + element.getID() + ". Skipping view in document migration.");
                return;
            }
            views.add(view);
            for (Property property : view.getAttribute()) {
                properties.add(property);
                Association association = property.getAssociation();
                if (association != null) {
                    associations.add(association);
                    properties.addAll(association.getMemberEnd());
                }
                Type type = property.getType();
                if (type != null) {
                    collectRelatedElementsRecursively(type, viewStereotype, properties, associations, views);
                }
            }
        }
    }
}

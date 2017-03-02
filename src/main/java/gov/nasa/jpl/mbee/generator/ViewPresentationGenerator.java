package gov.nasa.jpl.mbee.generator;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import gov.nasa.jpl.mbee.DocGenPlugin;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.ImportException;
import gov.nasa.jpl.mbee.ems.ImportUtility;
import gov.nasa.jpl.mbee.ems.ServerException;
import gov.nasa.jpl.mbee.ems.sync.local.LocalSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.ems.sync.local.LocalSyncTransactionCommitListener;
import gov.nasa.jpl.mbee.ems.sync.queue.OutputQueue;
import gov.nasa.jpl.mbee.ems.sync.queue.Request;
import gov.nasa.jpl.mbee.ems.validation.ImageValidator;
import gov.nasa.jpl.mbee.ems.validation.ModelValidator;
import gov.nasa.jpl.mbee.lib.JSONUtils;
import gov.nasa.jpl.mbee.lib.Pair;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.model.DocBookOutputVisitor;
import gov.nasa.jpl.mbee.model.Document;
import gov.nasa.jpl.mbee.viewedit.DBAlfrescoVisitor;
import gov.nasa.jpl.mbee.viewedit.PresentationElementInstance;
import gov.nasa.jpl.mbee.viewedit.ViewHierarchyVisitor;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBBook;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ViolationSeverity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

/**
 * @author dlam
 */
public class ViewPresentationGenerator implements RunnableWithProgress {
    private ValidationSuite suite = new ValidationSuite("View Instance Generation");
    private ValidationRule uneditableContent = new ValidationRule("Uneditable", "uneditable", ViolationSeverity.ERROR);
    private ValidationRule uneditableElements = new ValidationRule("Uneditable elements", "uneditable elements", ViolationSeverity.WARNING);
    private ValidationRule viewInProject = new ValidationRule("viewInProject", "viewInProject", ViolationSeverity.WARNING);
    private ValidationRule updateFailed = new ValidationRule("updateFailed", "updateFailed", ViolationSeverity.ERROR);
    private ValidationRule viewDoesNotExist = new ValidationRule("viewDoesNotExist", "viewDoesNotExist", ViolationSeverity.ERROR);

    private PresentationElementUtils instanceUtils;

    private boolean recurse;
    private Element start;
    private boolean isFromTeamwork = false;
    private boolean failure = false;

    private Project project = Application.getInstance().getProject();
    private boolean showValidation;

    private final List<ValidationSuite> vss = new ArrayList<>();
    private final Map<String, JSONObject> images;
    private final Set<Element> processedElements;

    public ViewPresentationGenerator(Element start, boolean recurse, boolean showValidation, PresentationElementUtils viu, Map<String, JSONObject> images, Set<Element> processedElements) {
        this.start = start;
        this.images = images != null ? images : new HashMap<String, JSONObject>();
        this.processedElements = processedElements != null ? processedElements : new HashSet<Element>();
        this.recurse = recurse;
        // cannotChange is obsoleted by server-side only instance specifications
        //this.cannotChange = cannotChange; //from one click doc gen, if update has unchangeable elements, check if those are things the view generation touches
        this.showValidation = showValidation;
        if (ProjectUtilities.isFromTeamworkServer(project.getPrimaryProject())) {
            isFromTeamwork = true;
        }
        this.instanceUtils = viu;
        if (this.instanceUtils == null) {
            this.instanceUtils = new PresentationElementUtils();
        }
        suite.addValidationRule(uneditableContent);
        suite.addValidationRule(viewInProject);
        suite.addValidationRule(updateFailed);
        suite.addValidationRule(uneditableElements);
        suite.addValidationRule(viewDoesNotExist);
        vss.add(suite);
    }

    @Override
    public void run(ProgressStatus progressStatus) {
        progressStatus.init("Initializing", 6);
        // Ensure no existing session so we have full control of whether to close/cancel further sessions.
        // no wild sessions spotted as of 06/05/16
        if (SessionManager.getInstance().isSessionCreated()) {
            SessionManager.getInstance().closeSession();
        }

        // STAGE 1: Calculating view structure
        progressStatus.setDescription("Calculating view structure");
        progressStatus.setCurrent(1);

        DocumentValidator dv = new DocumentValidator(start);
        dv.validateDocument();
        if (dv.isFatal()) {
            dv.printErrors(false);
            return;
        }
        // first run a local generation of the view model to get the current model view structure
        DocumentGenerator dg = new DocumentGenerator(start, dv, null, false);
        Document dge = dg.parseDocument(true, recurse, false);
        new PostProcessor().process(dge);

        DocBookOutputVisitor docBookOutputVisitor = new DocBookOutputVisitor(true);
        dge.accept(docBookOutputVisitor);
        DBBook book = docBookOutputVisitor.getBook();
        // TODO ??
        if (book == null) {
            return;
        }
        // Use HierarchyVisitor to find all views to download related elements (instances, constraint, etc.)
        ViewHierarchyVisitor viewHierarchyVisitor = new ViewHierarchyVisitor();
        dge.accept(viewHierarchyVisitor);

        Map<String, Pair<JSONObject, InstanceSpecification>> instanceSpecificationMap = new LinkedHashMap<>();
        Map<String, Pair<JSONObject, Slot>> slotMap = new LinkedHashMap<>();
        Map<String, ViewMapping> viewMap = new LinkedHashMap<>(viewHierarchyVisitor.getView2ViewElements().size());

        for (Element view : viewHierarchyVisitor.getView2ViewElements().keySet()) {
            if (processedElements.contains(view)) {
                Application.getInstance().getGUILog().log("Detected duplicate view reference. Skipping generation for " + view.getID() + ".");
                continue;
            }
            ViewMapping viewMapping = viewMap.containsKey(view.getID()) ? viewMap.get(view.getID()) : new ViewMapping();
            viewMapping.setElement(view);
            viewMap.put(view.getID(), viewMapping);
        }

        // Find and delete existing view constraints to prevent ID conflict when importing. Migration should handle this,
        // but best to not let the user corrupt their model. Have also noticed an MD bug where the constraint just sticks around
        // after a session cancellation.
        List<Constraint> constraintsToBeDeleted = new ArrayList<>(viewMap.size());
        for (ViewMapping viewMapping : viewMap.values()) {
            Element view = viewMapping.getElement();
            if (view == null) {
                continue;
            }
            Constraint constraint = Utils.getViewConstraint(view);
            if (constraint != null) {
                constraintsToBeDeleted.add(constraint);
            }
        }

        if (!constraintsToBeDeleted.isEmpty()) {
            SessionManager.getInstance().createSession("Legacy View Constraint Purge");
            for (Constraint constraint : constraintsToBeDeleted) {
                if (constraint.isEditable()) {
                    Application.getInstance().getGUILog().log("Deleting legacy view constraint: " + constraint.getID());
                    try {
                        ModelElementsManager.getInstance().removeElement(constraint);
                    } catch (ReadOnlyElementException e) {
                        updateFailed.addViolation(new ValidationRuleViolation(constraint, "[UPDATE FAILED] This view constraint could not be deleted automatically and needs to be deleted to prevent ID conflicts."));
                        failure = true;
                    }
                }
                else {
                    updateFailed.addViolation(new ValidationRuleViolation(constraint, "[UPDATE FAILED] This view constraint could not be deleted automatically and needs to be deleted to prevent ID conflicts."));
                    failure = true;
                }
            }
            SessionManager.getInstance().closeSession();
        }

        if (failure) {
            if (showValidation) {
                Utils.displayValidationWindow(vss, "View Generation Validation");
            }
            return;
        }

        // Allowing cancellation right before potentially long server queries
        if (handleCancel(progressStatus)) {
            return;
        }

        LocalSyncTransactionCommitListener localSyncTransactionCommitListener = LocalSyncProjectEventListenerAdapter.getProjectMapping(project).getLocalSyncTransactionCommitListener();

        // Query existing server-side JSONs for views
        List<String> viewIDs = new ArrayList<String>();
        if (!viewMap.isEmpty()) {
            // STAGE 2: Downloading existing view instances
            progressStatus.setDescription("Downloading existing view instances");
            progressStatus.setCurrent(2);

            JSONObject viewResponse;
            try {
                viewResponse = ModelValidator.getManyAlfrescoElementsByID(viewMap.keySet(), progressStatus);
            } catch (ServerException e) {
                failure = true;
                Application.getInstance().getGUILog().log("Server error occurred. Please check your network connection or view logs for more information.");
                e.printStackTrace();
                return;
            }
            if (viewResponse != null && viewResponse.containsKey("elements") && viewResponse.get("elements") instanceof JSONArray) {
                Queue<String> instanceIDs = new LinkedList<>();
                Queue<String> slotIDs = new LinkedList<>();
                Property generatedFromViewProperty = Utils.getGeneratedFromViewProperty(), generatedFromElementProperty = Utils.getGeneratedFromElementProperty();
                for (Object viewObject : (JSONArray) viewResponse.get("elements")) {
                    JSONObject viewJSONObject, viewSpecializationJSONObject, viewContentsJSONObject;
                    // store returned ids so we can exclude view generation for elements that aren't on the mms yet
                    if (viewObject instanceof JSONObject && (viewJSONObject = (JSONObject) viewObject).containsKey("specialization") && viewJSONObject.get("sysmlid") instanceof String) {
                        viewIDs.add((String) viewJSONObject.get("sysmlid"));
                    }
                    // Resolve current instances in the view constraint expression
                    if (viewObject instanceof JSONObject && (viewJSONObject = (JSONObject) viewObject).containsKey("specialization") && viewJSONObject.get("specialization") instanceof JSONObject
                            && (viewSpecializationJSONObject = (JSONObject) viewJSONObject.get("specialization")).containsKey("contents")
                            && viewSpecializationJSONObject.get("contents") instanceof JSONObject && (viewContentsJSONObject = (JSONObject) viewSpecializationJSONObject.get("contents")).containsKey("operand")
                            && viewContentsJSONObject.get("operand") instanceof JSONArray && viewJSONObject.containsKey("sysmlid") && viewJSONObject.get("sysmlid") instanceof String) {
                        JSONArray viewOperandJSONArray = (JSONArray) viewContentsJSONObject.get("operand");
                        List<String> viewInstanceIDs = new ArrayList<>(viewOperandJSONArray.size());
                        for (Object viewOperandObject : viewOperandJSONArray) {
                            if (viewOperandObject instanceof JSONObject) {
                                JSONObject viewOperandJSONObject = (JSONObject) viewOperandObject;
                                if (viewOperandJSONObject.containsKey("instance") && viewOperandJSONObject.get("instance") instanceof String) {
                                    String instanceID = (String) viewOperandJSONObject.get("instance");
                                    /*if (!instanceID.endsWith(PresentationElementUtils.ID_SUFFIX)) {
                                        continue;
                                    }*/
                                    if (generatedFromViewProperty != null) {
                                        slotIDs.add(instanceID + "-slot-" + generatedFromViewProperty.getID());
                                    }
                                    if (generatedFromElementProperty != null) {
                                        slotIDs.add(instanceID + "-slot-" + generatedFromElementProperty.getID());
                                    }
                                    instanceIDs.add(instanceID);
                                    viewInstanceIDs.add(instanceID);
                                }
                            }
                        }
                        String sysmlid = (String) viewJSONObject.get("sysmlid");
                        ViewMapping viewMapping = viewMap.containsKey(sysmlid) ? viewMap.get(sysmlid) : new ViewMapping();
                        viewMapping.setJson(viewJSONObject);
                        viewMapping.setInstanceIDs(viewInstanceIDs);
                        viewMap.put(sysmlid, viewMapping);
                    }
                }

                // Create the session you intend to cancel to revert all temporary elements.
                if (SessionManager.getInstance().isSessionCreated()) {
                    SessionManager.getInstance().closeSession();
                }
                SessionManager.getInstance().createSession("View Presentation Generation - Cancelled");
                if (localSyncTransactionCommitListener != null) {
                    localSyncTransactionCommitListener.setDisabled(true);
                }

                // Now that all first-level instances are resolved, query for them and import client-side (in reverse order) as model elements
                // Add any sections that are found along the way and loop until no more are found
                List<JSONObject> instanceJSONObjects = new ArrayList<>();
                List<JSONObject> slotJSONObjects = new ArrayList<>();
                while (!instanceIDs.isEmpty() && !slotIDs.isEmpty()) {
                    // Allow cancellation between every depths' server query.
                    if (handleCancel(progressStatus)) {
                        return;
                    }

                    JSONObject response;
                    try {
                        List<String> elementIDs = new ArrayList<>(instanceIDs);
                        elementIDs.addAll(slotIDs);
                        response = ModelValidator.getManyAlfrescoElementsByID(elementIDs, progressStatus);
                    } catch (ServerException e) {
                        failure = true;
                        Application.getInstance().getGUILog().log("Server error occurred. Please check your network connection or view logs for more information.");
                        e.printStackTrace();
                        SessionManager.getInstance().cancelSession();
                        return;
                    }
                    instanceIDs.clear();
                    slotIDs.clear();
                    if (response != null && response.containsKey("elements") && response.get("elements") instanceof JSONArray) {
                        for (Object instanceObject : (JSONArray) response.get("elements")) {
                            JSONObject elementJSONObject = null, specializationJSONObject = null, instanceSpecificationJSONObject;
                            if (instanceObject instanceof JSONObject && (elementJSONObject = (JSONObject) instanceObject).containsKey("specialization")
                                    && elementJSONObject.get("specialization") instanceof JSONObject
                                    && (specializationJSONObject = (JSONObject) elementJSONObject.get("specialization")).containsKey("instanceSpecificationSpecification")
                                    && specializationJSONObject.get("instanceSpecificationSpecification") instanceof JSONObject
                                    && (instanceSpecificationJSONObject = (JSONObject) specializationJSONObject.get("instanceSpecificationSpecification")).containsKey("operand")
                                    && instanceSpecificationJSONObject.get("operand") instanceof JSONArray) {
                                JSONArray instanceOperandJSONArray = (JSONArray) instanceSpecificationJSONObject.get("operand");
                                for (Object instanceOperandObject : instanceOperandJSONArray) {
                                    if (instanceOperandObject instanceof JSONObject) {
                                        JSONObject instanceOperandJSONObject = (JSONObject) instanceOperandObject;
                                        if (instanceOperandJSONObject.containsKey("instance") && instanceOperandJSONObject.get("instance") instanceof String) {
                                            String instanceID = (String) instanceOperandJSONObject.get("instance");
                                            /*if (!instanceID.endsWith(PresentationElementUtils.ID_SUFFIX)) {
                                                continue;
                                            }*/
                                            if (generatedFromViewProperty != null) {
                                                slotIDs.add(instanceID + "-slot-" + generatedFromViewProperty.getID());
                                            }
                                            if (generatedFromElementProperty != null) {
                                                slotIDs.add(instanceID + "-slot-" + generatedFromElementProperty.getID());
                                            }
                                            instanceIDs.add(instanceID);
                                        }
                                    }
                                }
                            }
                            Object o;
                            Boolean isSlot = specializationJSONObject != null && (o = specializationJSONObject.get("isSlot")) instanceof Boolean && ((Boolean) o);
                            (isSlot ? slotJSONObjects : instanceJSONObjects).add(elementJSONObject);
                        }
                    }
                }

                // STAGE 3: Importing existing view instances
                progressStatus.setDescription("Importing existing view instances");
                progressStatus.setCurrent(3);

                // importing instances in reverse order so that deepest level instances (sections and such) are loaded first
                ListIterator<JSONObject> instanceJSONsIterator = new ArrayList<>(instanceJSONObjects).listIterator(instanceJSONObjects.size());
                while (instanceJSONsIterator.hasPrevious()) {
                    if (handleCancel(progressStatus)) {
                        return;
                    }

                    JSONObject elementJSONObject = instanceJSONsIterator.previous();
                    try {
                        // Slots will break if imported with owner (instance) ignored, but we need to ignore InstanceSpecification owners
                        Element element = ImportUtility.createElement(elementJSONObject, false, true);
                        if (element instanceof InstanceSpecification) {
                            instanceSpecificationMap.put(element.getID(), new Pair<>(elementJSONObject, (InstanceSpecification) element));
                        }
                    } catch (ImportException e) {
                        /*failure = true;
                        Utils.printException(e);
                        SessionManager.getInstance().cancelSession();
                        return;*/
                        Application.getInstance().getGUILog().log("[WARNING] Failed to import instance specification " + elementJSONObject.get("sysmlid") + ": " + e.getMessage());
                    }
                }

                // The Alfresco service is a nice guy and returns Slots at the end so that when it's loaded in order the slots don't throw errors for missing their instances.
                // However we're being fancy and loading them backwards so we had to separate them ahead of time and then load the slots after.
                for (JSONObject slotJSONObject : slotJSONObjects) {
                    if (handleCancel(progressStatus)) {
                        return;
                    }

                    try {
                        // Slots will break if imported with owner (instance) ignored, but we need to ignore InstanceSpecification owners
                        Element element = ImportUtility.createElement(slotJSONObject, false, false);
                        if (element instanceof Slot) {
                            slotMap.put(element.getID(), new Pair<>(slotJSONObject, (Slot) element));
                        }
                    } catch (ImportException e) {
                        /*failure = true;
                        Utils.printException(e);
                        SessionManager.getInstance().cancelSession();
                        return;*/
                        Application.getInstance().getGUILog().log("[WARNING] Failed to import slot " + slotJSONObject.get("sysmlid") + ": " + e.getMessage());
                    }
                }

                // Build view constraints client-side as actual Constraint, Expression, InstanceValue(s), etc.
                // Note: Doing this one first since what it does is smaller in scope than ImportUtility. Potential order-dependent edge cases require further evaluation.
                for (ViewMapping viewMapping : viewMap.values()) {
                    Element view = viewMapping.getElement();
                    if (handleCancel(progressStatus)) {
                        return;
                    }

                    List<String> instanceSpecificationIDs;
                    if (viewMap.containsKey(view.getID()) && (instanceSpecificationIDs = viewMap.get(view.getID()).getInstanceIDs()) != null) {
                        final List<InstanceSpecification> instanceSpecifications = new ArrayList<>(instanceSpecificationIDs.size());
                        for (String instanceSpecificationID : instanceSpecificationIDs) {
                            Pair<JSONObject, InstanceSpecification> pair = instanceSpecificationMap.get(instanceSpecificationID);
                            if (pair != null && pair.getSecond() != null) {
                                instanceSpecifications.add(pair.getSecond());
                            }
                        }
                        instanceUtils.updateOrCreateConstraintFromInstanceSpecifications(view, instanceSpecifications);
                    }
                }

                // Update relations for all InstanceSpecifications and Slots
                // Instances need to be done in reverse order to load the lowest level instances first (sections)
                ListIterator<Pair<JSONObject, InstanceSpecification>> instanceSpecificationMapIterator = new ArrayList<>(instanceSpecificationMap.values()).listIterator(instanceSpecificationMap.size());
                while (instanceSpecificationMapIterator.hasPrevious()) {
                    if (handleCancel(progressStatus)) {
                        return;
                    }

                    Pair<JSONObject, InstanceSpecification> pair = instanceSpecificationMapIterator.previous();
                    try {
                        ImportUtility.createElement(pair.getFirst(), true, true);
                    } catch (Exception e) {
                        /*failure = true;
                        Utils.printException(e);
                        SessionManager.getInstance().cancelSession();
                        return;*/
                        Application.getInstance().getGUILog().log("Failed to update relations for instance specification " + pair.getFirst().get("sysmlid") + ": " + e.getMessage());
                    }
                }
                for (Pair<JSONObject, Slot> pair : slotMap.values()) {
                    if (handleCancel(progressStatus)) {
                        return;
                    }

                    try {
                        ImportUtility.createElement(pair.getFirst(), true, false);
                    } catch (Exception e) {
                        /*failure = true;
                        Utils.printException(e);
                        SessionManager.getInstance().cancelSession();
                        return;*/
                        Application.getInstance().getGUILog().log("Failed to update relations for slot " + pair.getFirst().get("sysmlid") + ": " + e.getMessage());
                    }
                }
            }
        }

        // STAGE 4: Generating new view instances
        progressStatus.setDescription("Generating new view instances");
        progressStatus.setCurrent(4);

        DBAlfrescoVisitor dbAlfrescoVisitor = new DBAlfrescoVisitor(recurse, true);
        try {
            book.accept(dbAlfrescoVisitor);
        } catch (Exception e) {
            Utils.printException(e);
            e.printStackTrace();
        }
        Map<Element, List<PresentationElementInstance>> view2pe = dbAlfrescoVisitor.getView2Pe();
        Map<Element, List<PresentationElementInstance>> view2unused = dbAlfrescoVisitor.getView2Unused();
        Map<Element, JSONArray> view2elements = dbAlfrescoVisitor.getView2Elements();
        List<Element> views = instanceUtils.getViewProcessOrder(start, dbAlfrescoVisitor.getHierarchyElements());
        views.removeAll(processedElements);
        Set<Element> skippedViews = new HashSet<>();

        for (Element view : views) {
            if (ProjectUtilities.isElementInAttachedProject(view)) {
                ValidationRuleViolation violation = new ValidationRuleViolation(view, "[IN MODULE] This view is in a module and was not processed.");
                viewInProject.addViolation(violation);
                skippedViews.add(view);
            }
            if (!viewIDs.contains(view.getID())) {
                ValidationRuleViolation violation = new ValidationRuleViolation(view, "View does not exist on MMS. Generation skipped.");
                viewDoesNotExist.addViolation(violation);
                skippedViews.add(view);
            }

        }

        if (failure) {
            if (showValidation) {
                Utils.displayValidationWindow(vss, "View Generation Validation");
            }
            SessionManager.getInstance().cancelSession();
            return;
        }

        try {
            Package unused = instanceUtils.getOrCreateUnusedInstancePackage();
            for (Element view : views) {
                if (skippedViews.contains(view)) {
                    continue;
                }
                // Using null package with intention to cancel session and delete instances to prevent model validation error.
                handlePes(view2pe.get(view), null);
                instanceUtils.updateOrCreateConstraintFromPresentationElements(view, view2pe.get(view));
            }

            if (handleCancel(progressStatus)) {
                return;
            }

            // commit to MMS
            JSONArray elementsJsonArray = new JSONArray();
            Queue<Pair<InstanceSpecification, Element>> instanceToView = new LinkedList<>();
            for (Element view : views) {
                if (skippedViews.contains(view)) {
                    continue;
                }
                

                // Sends the full view JSON if it doesn't exist on the server yet. If it does exist, it sends just the
                // portion of the JSON required to update the view contents.
                Object o;
                JSONObject oldViewJson = (o = viewMap.get(view.getID())) != null ? ((ViewMapping) o).getJson() : null,
                        oldSpecializationJson = oldViewJson != null && (o = oldViewJson.get("specialization")) instanceof JSONObject ? (JSONObject) o : null,
                        fullViewJson = ExportUtility.fillElement(view, null),
                        specializationJson = fullViewJson != null && (o = fullViewJson.get("specialization")) instanceof JSONObject ? (JSONObject) o : null;
                if (oldSpecializationJson == null || specializationJson == null) {
                    elementsJsonArray.add(fullViewJson);
                }
                else {
                    specializationJson.put("displayedElements", view2elements.get(view));
                    if (ModelValidator.isViewSpecializationDiff(oldSpecializationJson, specializationJson)) {
                        JSONObject subViewJson = new JSONObject();
                        subViewJson.put("sysmlid", fullViewJson.get("sysmlid"));
                        subViewJson.put("specialization", specializationJson);
                        elementsJsonArray.add(subViewJson);
                    }
                }

                for (PresentationElementInstance presentationElementInstance : view2pe.get(view)) {
                    if (presentationElementInstance.getInstance() != null) {
                        instanceToView.add(new Pair<>(presentationElementInstance.getInstance(), view));
                    }
                }

                // No need to commit constraint as it's wrapped up into the view
                /*
                Constraint constraint = Utils.getViewConstraint(view);
                if (constraint != null) {
                    elementsJSONArray.add(ExportUtility.fillElement(constraint, null));
                }
                */

            }

            while (!instanceToView.isEmpty()) {
                Pair<InstanceSpecification, Element> pair = instanceToView.remove();
                InstanceSpecification instance = pair.getFirst();

                List<InstanceSpecification> subInstances = instanceUtils.getCurrentInstances(instance, pair.getSecond()).getAll();
                for (InstanceSpecification subInstance : subInstances) {
                    instanceToView.add(new Pair<>(subInstance, pair.getSecond()));
                }

                JSONObject instanceSpecificationJson = ExportUtility.fillElement(instance, null);
                if (instanceSpecificationJson == null) {
                    continue;
                }
                JSONObject oldInstanceSpecificationJson = instanceSpecificationMap.containsKey(instance.getID()) ? instanceSpecificationMap.get(instance.getID()).getFirst() : null;
                JSONObject instanceSpecificationToCommit = null;
                if (oldInstanceSpecificationJson == null) {
                    instanceSpecificationToCommit = instanceSpecificationJson;
                }
                else {
                    // We only want to compare documentation and specialization to see if we need to update the instance
                    JSONObject subInstanceSpecificationJson = new JSONObject(), oldSubInstanceSpecificationJson = new JSONObject();
                    subInstanceSpecificationJson.put("documentation", instanceSpecificationJson.get("documentation"));
                    oldSubInstanceSpecificationJson.put("documentation", oldInstanceSpecificationJson.get("documentation"));
                    subInstanceSpecificationJson.put("specialization", instanceSpecificationJson.get("specialization"));
                    oldSubInstanceSpecificationJson.put("specialization", oldInstanceSpecificationJson.get("specialization"));
                    subInstanceSpecificationJson.put("name", instanceSpecificationJson.get("name"));
                    oldSubInstanceSpecificationJson.put("name", oldInstanceSpecificationJson.get("name"));
                    if (!JSONUtils.compare(subInstanceSpecificationJson, oldSubInstanceSpecificationJson)) {
                        instanceSpecificationToCommit = instanceSpecificationJson;
                    }
                }
                if (instanceSpecificationToCommit != null) {
                    elementsJsonArray.add(instanceSpecificationToCommit);
                }

                for (Slot slot : instance.getSlot()) {
                    JSONObject slotJson = ExportUtility.fillElement(slot, null);
                    if (slotJson == null) {
                        continue;
                    }
                    JSONObject oldSlotJson = slotMap.containsKey(slot.getID()) ? slotMap.get(slot.getID()).getFirst() : null;
                    if (oldSlotJson == null) {
                        elementsJsonArray.add(slotJson);
                        continue;
                    }
                    // We only want to compare owner and specialization to see if we need to update the slot
                    JSONObject subSlotJson = new JSONObject(), oldSubSlotJson = new JSONObject();
                    subSlotJson.put("owner", slotJson.get("owner"));
                    oldSubSlotJson.put("owner", oldSlotJson.get("owner"));
                    subSlotJson.put("specialization", slotJson.get("specialization"));
                    oldSubSlotJson.put("specialization", oldSlotJson.get("specialization"));
                    if (!JSONUtils.compare(subSlotJson, oldSubSlotJson)) {
                        elementsJsonArray.add(slotJson);
                    }
                }
            }

            // Last chance to cancel before sending generated views to the server. Point of no return.
            if (handleCancel(progressStatus)) {
                return;
            }

            boolean changed = false;

            if (!elementsJsonArray.isEmpty()) {
                // STAGE 5: Queueing upload of generated view instances
                progressStatus.setDescription("Queueing upload of generated view instances");
                progressStatus.setCurrent(5);

                JSONObject body = new JSONObject();
                body.put("elements", elementsJsonArray);
                body.put("source", "magicdraw");
                body.put("mmsVersion", DocGenPlugin.VERSION);
                Application.getInstance().getGUILog().log("Updating/creating " + elementsJsonArray.size() + " element" + (elementsJsonArray.size() != 1 ? "s" : "") + " to generate views.");

                OutputQueue.getInstance().offer(new Request(ExportUtility.getPostElementsUrl(), body.toJSONString(), "POST", true, elementsJsonArray.size(), "Sync Changes"));
                changed = true;
            }

            // Delete unused presentation elements

//            elementsJsonArray = new JSONArray();
//            for (List<PresentationElementInstance> presentationElementInstances : view2unused.values()) {
//                for (PresentationElementInstance presentationElementInstance : presentationElementInstances) {
//                    if (presentationElementInstance.getInstance() == null) {
//                        continue;
//                    }
//                    String id = ExportUtility.getElementID(presentationElementInstance.getInstance());
//                    if (id == null) {
//                        continue;
//                    }
//                    JSONObject elementJsonObject = new JSONObject();
//                    elementJsonObject.put("sysmlid", id);
//                    elementsJsonArray.add(elementJsonObject);
//                }
//            }
//            if (!elementsJsonArray.isEmpty()) {
//                JSONObject body = new JSONObject();
//                body.put("elements", elementsJsonArray);
//                body.put("source", "magicdraw");
//                body.put("mmsVersion", DocGenPlugin.VERSION);
//                Application.getInstance().getGUILog().log("Deleting " + elementsJsonArray.size() + " unused presentation element" + (elementsJsonArray.size() != 1 ? "s" : "") + ".");
//
//                OutputQueue.getInstance().offer(new Request(ExportUtility.getUrlWithWorkspace() + "/elements", body.toJSONString(), "DELETEALL", true, elementsJsonArray.size(), "View Generation"));
//                changed = true;
//            }

            if (!changed) {
                Application.getInstance().getGUILog().log("No changes required to generate views.");
            }

            // STAGE 6: Finishing up

            progressStatus.setDescription("Finishing up");
            progressStatus.setCurrent(6);

            // Cleaning up after myself. While cancelSession *should* undo all elements created, there are certain edge
            // cases like the underlying constraint not existing in the containment tree, but leaving a stale constraint
            // on the view block.
            Set<Element> elementsToDelete = new HashSet<>();
            for (Pair<JSONObject, Slot> pair : slotMap.values()) {
                if (pair.getSecond() != null) {
                    elementsToDelete.add(pair.getSecond());
                }
            }
            for (Pair<JSONObject, InstanceSpecification> pair : instanceSpecificationMap.values()) {
                if (pair.getSecond() != null) {
                    elementsToDelete.add(pair.getSecond());
                }
            }
            for (Element element : views) {
                Constraint constraint = Utils.getViewConstraint(element);
                if (constraint == null) {
                    continue;
                }
                elementsToDelete.add(constraint);
                ValueSpecification valueSpecification = constraint.getSpecification();
                if (valueSpecification == null) {
                    continue;
                }
                elementsToDelete.add(valueSpecification);
                List<ValueSpecification> operands;
                if (!(valueSpecification instanceof Expression) || (operands = ((Expression) valueSpecification).getOperand()) == null) {
                    continue;
                }
                for (ValueSpecification operand : operands) {
                    elementsToDelete.add(operand);
                    InstanceSpecification instanceSpecification;
                    if (!(operand instanceof InstanceValue) || (instanceSpecification = ((InstanceValue) operand).getInstance()) == null) {
                        continue;
                    }
                    elementsToDelete.add(instanceSpecification);
                    for (Slot slot : instanceSpecification.getSlot()) {
                        elementsToDelete.add(slot);
                        elementsToDelete.addAll(slot.getValue());
                    }
                }
            }
            for (Element element : elementsToDelete) {
                try {
                    ModelElementsManager.getInstance().removeElement(element);
                } catch (ReadOnlyElementException ignored) {
                }
            }
            // used to skip redundant view generation attempts when using multi-select or ElementGroups; see GenerateViewPresentationAction
            processedElements.addAll(views);
        } catch (Exception e) {
            failure = true;
            Utils.printException(e);
        } finally {
            // cancel session so all elements created get deleted automatically
            if (SessionManager.getInstance().isSessionCreated()) {
                SessionManager.getInstance().cancelSession();
            }
            if (localSyncTransactionCommitListener != null) {
                localSyncTransactionCommitListener.setDisabled(false);
            }
        }
        ImageValidator iv = new ImageValidator(dbAlfrescoVisitor.getImages(), images);
        // this checks images generated from the local generation against what's on the web based on checksum
        iv.validate();
        // Auto-validate - https://cae-jira.jpl.nasa.gov/browse/MAGICDRAW-45
        for (ValidationRule validationRule : iv.getSuite().getValidationRules()) {
            for (ValidationRuleViolation validationRuleViolation : validationRule.getViolations()) {
                if (!validationRuleViolation.getActions().isEmpty()) {
                    validationRuleViolation.getActions().get(0).actionPerformed(null);
                }
            }
        }
        /*vss.add(iv.getSuite());
        if (showValidation) {
            if (suite.hasErrors() || iv.getSuite().hasErrors()) {
                Utils.displayValidationWindow(vss, "View Generation Validation");
            }
        }*/
        if (showValidation) {
            if (suite.hasErrors()) {
                Utils.displayValidationWindow(vss, "View Generation Validation");
            }
        }
    }

    private void handlePes(List<PresentationElementInstance> pes, Package p) {
        for (PresentationElementInstance pe : pes) {
            if (pe.getChildren() != null && !pe.getChildren().isEmpty()) {
                handlePes(pe.getChildren(), p);
            }
            instanceUtils.updateOrCreateInstance(pe, p);
        }
    }

    private boolean handleCancel(ProgressStatus progressStatus) {
        if (progressStatus.isCancel()) {
            failure = true;
            if (SessionManager.getInstance().isSessionCreated()) {
                SessionManager.getInstance().cancelSession();
            }
            Application.getInstance().getGUILog().log("View generation cancelled.");
            return true;
        }
        return false;
    }

    public List<ValidationSuite> getValidations() {
        return vss;
    }

    public boolean isFailure() {
        return failure;
    }

    private class ViewMapping {
        private Element element;
        private JSONObject json;
        private List<String> instanceIDs;

        public Element getElement() {
            return element;
        }

        public void setElement(Element element) {
            this.element = element;
        }

        public JSONObject getJson() {
            return json;
        }

        public void setJson(JSONObject json) {
            this.json = json;
        }

        public List<String> getInstanceIDs() {
            return instanceIDs;
        }

        public void setInstanceIDs(List<String> instanceIDs) {
            this.instanceIDs = instanceIDs;
        }
    }

}

package gov.nasa.jpl.mbee.mdk.generator;

import com.fasterxml.jackson.databind.JsonNode;
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
import gov.nasa.jpl.mbee.mdk.MDKPlugin;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DBBook;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationRule;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ViolationSeverity;
import gov.nasa.jpl.mbee.mdk.ems.ExportUtility;
import gov.nasa.jpl.mbee.mdk.ems.ImportException;
import gov.nasa.jpl.mbee.mdk.ems.ImportUtility;
import gov.nasa.jpl.mbee.mdk.ems.ServerException;
import gov.nasa.jpl.mbee.mdk.ems.json.JsonEquivalencePredicate;
import gov.nasa.jpl.mbee.mdk.ems.sync.local.LocalSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.mdk.ems.sync.local.LocalSyncTransactionCommitListener;
import gov.nasa.jpl.mbee.mdk.ems.sync.queue.OutputQueue;
import gov.nasa.jpl.mbee.mdk.ems.sync.queue.Request;
import gov.nasa.jpl.mbee.mdk.ems.validation.ImageValidator;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.lib.Pair;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import gov.nasa.jpl.mbee.mdk.ems.MMSUtils;
import gov.nasa.jpl.mbee.mdk.model.DocBookOutputVisitor;
import gov.nasa.jpl.mbee.mdk.model.Document;
import gov.nasa.jpl.mbee.mdk.viewedit.DBAlfrescoVisitor;
import gov.nasa.jpl.mbee.mdk.viewedit.PresentationElementInstance;
import gov.nasa.jpl.mbee.mdk.viewedit.ViewHierarchyVisitor;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.*;

/**
 * @author dlam
 */

@Deprecated
//TODO update stuff in here for @donbot
public class ViewPresentationGenerator implements RunnableWithProgress {
    private ValidationSuite suite = new ValidationSuite("View Instance Generation");
    private ValidationRule uneditableContent = new ValidationRule("Uneditable", "uneditable", ViolationSeverity.ERROR);
    private ValidationRule uneditableElements = new ValidationRule("Uneditable elements", "uneditable elements", ViolationSeverity.WARNING);
    private ValidationRule viewInProject = new ValidationRule("viewInProject", "viewInProject", ViolationSeverity.WARNING);
    private ValidationRule updateFailed = new ValidationRule("updateFailed", "updateFailed", ViolationSeverity.ERROR);

    private PresentationElementUtils instanceUtils;

    private boolean recurse;
    private Element start;
    private boolean isFromTeamwork = false;
    private boolean failure = false;

    private Project project = Application.getInstance().getProject();
    private boolean showValidation;

    private final List<ValidationSuite> vss = new ArrayList<>();
    private final Map<String, JsonNode> images;
    private final Set<Element> processedElements;

    public ViewPresentationGenerator(Element start, boolean recurse, boolean showValidation, PresentationElementUtils viu, Map<String, JsonNode> images, Set<Element> processedElements) {
        this.start = start;
        this.images = images != null ? images : new HashMap<>();
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

        Map<String, Pair<JsonNode, InstanceSpecification>> instanceSpecificationMap = new LinkedHashMap<>();
        Map<String, Pair<JsonNode, Slot>> slotMap = new LinkedHashMap<>();
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
                        updateFailed.addViolation(new ValidationRuleViolation(constraint, "[LOCAL FAILED] This view constraint could not be deleted automatically and needs to be deleted to prevent ID conflicts."));
                        failure = true;
                    }
                }
                else {
                    updateFailed.addViolation(new ValidationRuleViolation(constraint, "[LOCAL FAILED] This view constraint could not be deleted automatically and needs to be deleted to prevent ID conflicts."));
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
        if (!viewMap.isEmpty()) {
            // STAGE 2: Downloading existing view instances
            progressStatus.setDescription("Downloading existing view instances");
            progressStatus.setCurrent(2);

            JsonNode viewResponse;
            try {
                JSONObject jsonObject = MMSUtils.getElementsById(viewMap.keySet(), progressStatus);
                viewResponse = jsonObject != null ? JacksonUtils.getObjectMapper().readTree(jsonObject.toJSONString()) : null;
            } catch (ServerException | IOException e) {
                failure = true;
                Application.getInstance().getGUILog().log("Server error occurred. Please check your network connection or view logs for more information.");
                e.printStackTrace();
                return;
            }
            JsonNode viewElementsJsonArray;
            if (viewResponse != null && (viewElementsJsonArray = viewResponse.get("elements")) != null && viewElementsJsonArray.isArray()) {
                Queue<String> instanceIDs = new LinkedList<>();
                Queue<String> slotIDs = new LinkedList<>();
                Property generatedFromViewProperty = Utils.getGeneratedFromViewProperty(), generatedFromElementProperty = Utils.getGeneratedFromElementProperty();
                for (JsonNode elementJson : viewElementsJsonArray) {
                    // Resolve current instances in the view constraint expression
                    JsonNode viewOperandJsonArray = JacksonUtils.getAtPath(elementJson, "/contents/operand"),
                            sysmlIdJson = elementJson.get(MDKConstants.SYSML_ID_KEY);
                    String sysmlId;
                    if (viewOperandJsonArray != null && viewOperandJsonArray.isArray()
                            && sysmlIdJson != null && sysmlIdJson.isTextual() && (sysmlId = sysmlIdJson.asText()).isEmpty()) {
                        List<String> viewInstanceIDs = new ArrayList<>(viewOperandJsonArray.size());
                        for (JsonNode viewOperandJson : viewOperandJsonArray) {
                            JsonNode instanceIdJson = viewOperandJson.get(MDKConstants.INSTANCE_ID_KEY);
                            String instanceId;
                            if (instanceIdJson != null && instanceIdJson.isTextual() && !(instanceId = instanceIdJson.asText()).isEmpty()) {
                                /*if (!instanceID.endsWith(PresentationElementUtils.ID_SUFFIX)) {
                                    continue;
                                }*/
                                if (generatedFromViewProperty != null) {
                                    slotIDs.add(instanceId + "-slot-" + generatedFromViewProperty.getID());
                                }
                                if (generatedFromElementProperty != null) {
                                    slotIDs.add(instanceId + "-slot-" + generatedFromElementProperty.getID());
                                }
                                instanceIDs.add(instanceId);
                                viewInstanceIDs.add(instanceId);
                            }
                        }
                        ViewMapping viewMapping = viewMap.containsKey(sysmlId) ? viewMap.get(sysmlId) : new ViewMapping();
                        viewMapping.setJson(elementJson);
                        viewMapping.setInstanceIDs(viewInstanceIDs);
                        viewMap.put(sysmlId, viewMapping);
                    }
                }

                // Create the session you intend to cancel to revert all temporary elements.
                SessionManager.getInstance().createSession("View Presentation Generation - Cancelled");
                if (localSyncTransactionCommitListener != null) {
                    localSyncTransactionCommitListener.setDisabled(true);
                }

                // Now that all first-level instances are resolved, query for them and import client-side (in reverse order) as model elements
                // Add any sections that are found along the way and loop until no more are found
                List<JsonNode> instanceJsonNodes = new ArrayList<>();
                List<JsonNode> slotJsonNodes = new ArrayList<>();
                while (!instanceIDs.isEmpty() && !slotIDs.isEmpty()) {
                    // Allow cancellation between every depths' server query.
                    if (handleCancel(progressStatus)) {
                        return;
                    }

                    JsonNode instanceAndSlotResponse;
                    try {
                        List<String> elementIDs = new ArrayList<>(instanceIDs);
                        elementIDs.addAll(slotIDs);
                        JSONObject jsonObject = MMSUtils.getElementsById(elementIDs, progressStatus);
                        instanceAndSlotResponse = jsonObject != null ? JacksonUtils.getObjectMapper().readTree(jsonObject.toJSONString()) : null;
                    } catch (ServerException | IOException e) {
                        failure = true;
                        Application.getInstance().getGUILog().log("Server error occurred. Please check your network connection or view logs for more information.");
                        e.printStackTrace();
                        SessionManager.getInstance().cancelSession();
                        return;
                    }
                    instanceIDs.clear();
                    slotIDs.clear();
                    JsonNode instanceAndSlotElementsJsonArray;
                    if (instanceAndSlotResponse != null && (instanceAndSlotElementsJsonArray = instanceAndSlotResponse.get("elements")) != null && instanceAndSlotElementsJsonArray.isArray()) {
                        for (JsonNode elementJson : instanceAndSlotElementsJsonArray) {
                            JsonNode instanceOperandJsonArray = JacksonUtils.getAtPath(elementJson, "/specification/operand");
                            if (instanceOperandJsonArray != null && instanceOperandJsonArray.isArray()) {
                                for (JsonNode instanceOperandJson : instanceOperandJsonArray) {
                                    JsonNode instanceIdJson = instanceOperandJson.get(MDKConstants.INSTANCE_ID_KEY);
                                    String instanceId;
                                    if (instanceIdJson != null && instanceIdJson.isTextual() && !(instanceId = instanceIdJson.asText()).isEmpty()) {
                                        /*if (!instanceID.endsWith(PresentationElementUtils.ID_SUFFIX)) {
                                            continue;
                                        }*/
                                        if (generatedFromViewProperty != null) {
                                            slotIDs.add(instanceId + "-slot-" + generatedFromViewProperty.getID());
                                        }
                                        if (generatedFromElementProperty != null) {
                                            slotIDs.add(instanceId + "-slot-" + generatedFromElementProperty.getID());
                                        }
                                        instanceIDs.add(instanceId);
                                    }
                                }
                            }
                            JsonNode typeJson = elementJson.get(MDKConstants.TYPE_KEY);
                            if (typeJson.isTextual()) {
                                (typeJson.asText().equalsIgnoreCase("slot") ? slotJsonNodes : instanceJsonNodes).add(elementJson);
                            }
                        }
                    }
                }

                // STAGE 3: Importing existing view instances
                progressStatus.setDescription("Importing existing view instances");
                progressStatus.setCurrent(3);

                // importing instances in reverse order so that deepest level instances (sections and such) are loaded first
                ListIterator<JsonNode> instanceJSONsIterator = new ArrayList<>(instanceJsonNodes).listIterator(instanceJsonNodes.size());
                while (instanceJSONsIterator.hasPrevious()) {
                    if (handleCancel(progressStatus)) {
                        return;
                    }

                    JsonNode elementJsonNode = instanceJSONsIterator.previous();
                    try {
                        // Slots will break if imported with owner (instance) ignored, but we need to ignore InstanceSpecification owners
                        Element element = ImportUtility.createElement(elementJsonNode, false, true);
                        if (element instanceof InstanceSpecification) {
                            instanceSpecificationMap.put(element.getID(), new Pair<>(elementJsonNode, (InstanceSpecification) element));
                        }
                    } catch (ImportException e) {
                        /*failure = true;
                        Utils.printException(e);
                        SessionManager.getInstance().cancelSession();
                        return;*/
                        Application.getInstance().getGUILog().log("[WARNING] Failed to import instance specification " + elementJsonNode.get(MDKConstants.SYSML_ID_KEY) + ": " + e.getMessage());
                    }
                }

                // The Alfresco service is a nice guy and returns Slots at the end so that when it's loaded in order the slots don't throw errors for missing their instances.
                // However we're being fancy and loading them backwards so we had to separate them ahead of time and then load the slots after.
                for (JsonNode slotJsonNode : slotJsonNodes) {
                    if (handleCancel(progressStatus)) {
                        return;
                    }

                    try {
                        // Slots will break if imported with owner (instance) ignored, but we need to ignore InstanceSpecification owners
                        Element element = ImportUtility.createElement(slotJsonNode, false, false);
                        if (element instanceof Slot) {
                            slotMap.put(element.getID(), new Pair<>(slotJsonNode, (Slot) element));
                        }
                    } catch (ImportException e) {
                        /*failure = true;
                        Utils.printException(e);
                        SessionManager.getInstance().cancelSession();
                        return;*/
                        Application.getInstance().getGUILog().log("[WARNING] Failed to import slot " + slotJsonNode.get(MDKConstants.SYSML_ID_KEY) + ": " + e.getMessage());
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
                            Pair<JsonNode, InstanceSpecification> pair = instanceSpecificationMap.get(instanceSpecificationID);
                            if (pair != null && pair.getSecond() != null) {
                                instanceSpecifications.add(pair.getSecond());
                            }
                        }
                        instanceUtils.updateOrCreateConstraintFromInstanceSpecifications(view, instanceSpecifications);
                    }
                }

                // Update relations for all InstanceSpecifications and Slots
                // Instances need to be done in reverse order to load the lowest level instances first (sections)
                ListIterator<Pair<JsonNode, InstanceSpecification>> instanceSpecificationMapIterator = new ArrayList<>(instanceSpecificationMap.values()).listIterator(instanceSpecificationMap.size());
                while (instanceSpecificationMapIterator.hasPrevious()) {
                    if (handleCancel(progressStatus)) {
                        return;
                    }

                    Pair<JsonNode, InstanceSpecification> pair = instanceSpecificationMapIterator.previous();
                    try {
                        ImportUtility.createElement(pair.getFirst(), true, true);
                    } catch (Exception e) {
                        /*failure = true;
                        Utils.printException(e);
                        SessionManager.getInstance().cancelSession();
                        return;*/
                        Application.getInstance().getGUILog().log("Failed to update relations for instance specification " + pair.getFirst().get(MDKConstants.SYSML_ID_KEY) + ": " + e.getMessage());
                    }
                }
                for (Pair<JsonNode, Slot> pair : slotMap.values()) {
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
                        Application.getInstance().getGUILog().log("Failed to update relations for slot " + pair.getFirst().get(MDKConstants.SYSML_ID_KEY) + ": " + e.getMessage());
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
                for (PresentationElementInstance presentationElementInstance : view2pe.get(view)) {
                    if (presentationElementInstance.getInstance() != null) {
                        instanceToView.add(new Pair<>(presentationElementInstance.getInstance(), view));
                    }
                }

                // No need to commit constraint as it's wrapped up into the view
                /*
                Constraint constraint = Utils.getViewConstraint(view);
                if (constraint != null) {
                    elementsJSONArray.add(Converters.getElementToJsonConverter().apply(constraint, project));
                }
                */

                // Sends the full view JSON if it doesn't exist on the server yet. If it does exist, it sends just the
                // portion of the JSON required to update the view contents.
                // TODO there's a lot of redundancy here, fix in @donbot
                Object o;
                JsonNode newViewJson = Converters.getElementToJsonConverter().apply(view, project);
                if (newViewJson == null) {
                    skippedViews.add(view);
                    continue;
                }
                JsonNode oldViewJson = (o = viewMap.get(view.getID())) != null ? ((ViewMapping) o).getJson() : null;
                try {
                    JsonNode source = oldViewJson != null ? JacksonUtils.getObjectMapper().readTree(oldViewJson.toJSONString()) : null;
                    JsonNode target = JacksonUtils.getObjectMapper().readTree(newViewJson.toJSONString());
                    if (!JsonEquivalencePredicate.getInstance().test(source, target)) {
                        elementsJsonArray.add(newViewJson);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    skippedViews.add(view);
                }
            }

            while (!instanceToView.isEmpty()) {
                Pair<InstanceSpecification, Element> pair = instanceToView.remove();
                InstanceSpecification instance = pair.getFirst();

                List<InstanceSpecification> subInstances = instanceUtils.getCurrentInstances(instance, pair.getSecond()).getAll();
                for (InstanceSpecification subInstance : subInstances) {
                    instanceToView.add(new Pair<>(subInstance, pair.getSecond()));
                }

                JsonNode newInstanceSpecificationJson = Converters.getElementToJsonConverter().apply(instance, project);
                if (newInstanceSpecificationJson == null) {
                    continue;
                }
                JsonNode oldInstanceSpecificationJson = instanceSpecificationMap.containsKey(instance.getID()) ? instanceSpecificationMap.get(instance.getID()).getFirst() : null;
                try {
                    JsonNode source = oldInstanceSpecificationJson != null ? JacksonUtils.getObjectMapper().readTree(oldInstanceSpecificationJson.toJSONString()) : null;
                    JsonNode target = JacksonUtils.getObjectMapper().readTree(newInstanceSpecificationJson.toJSONString());
                    if (!JsonEquivalencePredicate.getInstance().test(source, target)) {
                        elementsJsonArray.add(newInstanceSpecificationJson);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
                for (Slot slot : instance.getSlot()) {
                    JsonNode newSlotJson = Converters.getElementToJsonConverter().apply(slot, project);
                    if (newSlotJson == null) {
                        continue;
                    }
                    JsonNode oldSlotJson = slotMap.containsKey(slot.getID()) ? slotMap.get(slot.getID()).getFirst() : null;
                    try {
                        JsonNode source = oldSlotJson != null ? JacksonUtils.getObjectMapper().readTree(oldSlotJson.toJSONString()) : null;
                        JsonNode target = JacksonUtils.getObjectMapper().readTree(newSlotJson.toJSONString());
                        if (!JsonEquivalencePredicate.getInstance().test(source, target)) {
                            elementsJsonArray.add(newSlotJson);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
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

                JsonNode body = new JsonNode();
                body.put("elements", elementsJsonArray);
                body.put("source", "magicdraw");
                body.put("mmsVersion", MDKPlugin.VERSION);
                Application.getInstance().getGUILog().log("Updating/creating " + elementsJsonArray.size() + " element" + (elementsJsonArray.size() != 1 ? "s" : "") + " to generate views.");

                OutputQueue.getInstance().offer(new Request(ExportUtility.getPostElementsUrl(), body.toJSONString(), "POST", true, elementsJsonArray.size(), "Sync Changes"));
                changed = true;
            }

            // Delete unused presentation elements

            elementsJsonArray = new JSONArray();
            for (List<PresentationElementInstance> presentationElementInstances : view2unused.values()) {
                for (PresentationElementInstance presentationElementInstance : presentationElementInstances) {
                    if (presentationElementInstance.getInstance() == null) {
                        continue;
                    }
                    String id = Converters.getElementToIdConverter().apply(presentationElementInstance.getInstance());
                    if (id == null) {
                        continue;
                    }
                    JsonNode elementJsonNode = new JsonNode();
                    elementJsonNode.put(MDKConstants.SYSML_ID_KEY, id);
                    elementsJsonArray.add(elementJsonNode);
                }
            }
            if (!elementsJsonArray.isEmpty()) {
                JsonNode body = new JsonNode();
                body.put("elements", elementsJsonArray);
                body.put("source", "magicdraw");
                body.put("mmsVersion", MDKPlugin.VERSION);
                Application.getInstance().getGUILog().log("Deleting " + elementsJsonArray.size() + " unused presentation element" + (elementsJsonArray.size() != 1 ? "s" : "") + ".");

                OutputQueue.getInstance().offer(new Request(ExportUtility.getUrlWithWorkspace() + "/elements", body.toJSONString(), "DELETEALL", true, elementsJsonArray.size(), "View Generation"));
                changed = true;
            }

            if (!changed) {
                Application.getInstance().getGUILog().log("No changes required to generate views.");
            }

            // STAGE 6: Finishing up

            progressStatus.setDescription("Finishing up");
            progressStatus.setCurrent(6);

            // Cleaning up after myself. While cancelSession *should* undo all elements created, there are certain edge
            // cases like the underlying constraint not existing in the containment tree, but leaving a stale constraint
            // on the view block.
            List<Element> elementsToDelete = new ArrayList<>(slotMap.size() + instanceSpecificationMap.size() + views.size());
            for (Pair<JsonNode, Slot> pair : slotMap.values()) {
                if (pair.getSecond() != null) {
                    elementsToDelete.add(pair.getSecond());
                }
            }
            for (Pair<JsonNode, InstanceSpecification> pair : instanceSpecificationMap.values()) {
                if (pair.getSecond() != null) {
                    elementsToDelete.add(pair.getSecond());
                }
            }
            for (Element element : views) {
                Constraint constraint = Utils.getViewConstraint(element);
                if (constraint != null) {
                    elementsToDelete.add(constraint);
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
        private JsonNode json;
        private List<String> instanceIDs;

        public Element getElement() {
            return element;
        }

        public void setElement(Element element) {
            this.element = element;
        }

        public JsonNode getJson() {
            return json;
        }

        public void setJson(JsonNode json) {
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

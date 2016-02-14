package gov.nasa.jpl.mbee.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import gov.nasa.jpl.mbee.ems.validation.ImageValidator;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.model.DocBookOutputVisitor;
import gov.nasa.jpl.mbee.model.Document;
import gov.nasa.jpl.mbee.viewedit.DBAlfrescoVisitor;
import gov.nasa.jpl.mbee.viewedit.PresentationElement;
import gov.nasa.jpl.mbee.viewedit.PresentationElement.PEType;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBBook;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ViolationSeverity;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.AggregationKind;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.AggregationKindEnum;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Association;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ElementValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Expression;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Relationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.TypedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;

public class ViewPresentationGenerator implements RunnableWithProgress {
    private ValidationSuite suite = new ValidationSuite("View Instance Generation");
    private ValidationRule uneditableContent = new ValidationRule("Uneditable", "uneditable", ViolationSeverity.ERROR);
    private ValidationRule uneditableElements = new ValidationRule("Uneditable elements", "uneditable elements", ViolationSeverity.WARNING);
    private ValidationRule viewInProject = new ValidationRule("viewInProject", "viewInProject", ViolationSeverity.WARNING);
    private ValidationRule updateFailed = new ValidationRule("updateFailed", "updateFailed", ViolationSeverity.ERROR);
    
    private ElementsFactory ef = Application.getInstance().getProject().getElementsFactory();
    private ViewInstanceUtils instanceUtils;
    private OrganizeViewInstances organizer;

    private Stereotype viewClassStereotype = Utils.getViewClassStereotype();
    
    private boolean recurse;
    private Element start;
    private boolean isFromTeamwork = false;
    private boolean failure = false;

    // use these prefixes then add project_id to form the view instances id and unused view id respectively
    private String viewInstPrefix = "View_Instances";
    private String unusedInstPrefix = "Unused_View_Instances";

    private Project project = Application.getInstance().getProject();;
    private boolean showValidation;
    private Set<String> cannotChange;
    
    private List<ValidationSuite> vss = new ArrayList<ValidationSuite>();
    private Set<Element> needEdit = new HashSet<Element>();
    private Set<Element> shouldMove = new HashSet<Element>();

    public ViewPresentationGenerator(Element start, boolean recurse, Set<String> cannotChange, boolean showValidation, ViewInstanceUtils viu) {
        this.start = start;
        this.recurse = recurse;
        this.cannotChange = cannotChange; //from one click doc gen, if update has unchangeable elements, check if those are things the view generation touches
        this.showValidation = showValidation;
        if (ProjectUtilities.isFromTeamworkServer(project.getPrimaryProject())) {
            isFromTeamwork = true;
        }
        this.instanceUtils = viu;
        if (this.instanceUtils == null) {
            this.instanceUtils = new ViewInstanceUtils();
        }
        this.organizer = new OrganizeViewInstances(start, recurse, false, this.instanceUtils);
        suite.addValidationRule(uneditableContent);
        suite.addValidationRule(viewInProject);
        suite.addValidationRule(updateFailed);
        suite.addValidationRule(uneditableElements);
    }
    
    @Override
    public void run(ProgressStatus ps) {
        
        DocumentValidator dv = new DocumentValidator(start);
        dv.validateDocument();
        if (dv.isFatal()) {
            dv.printErrors(false);
            return;
        }
        // first run a local generation of the view model to get the current model view structure
        DocumentGenerator dg = new DocumentGenerator(start, dv, null, false);
        Document dge = dg.parseDocument(true, recurse, false);
        (new PostProcessor()).process(dge);

        DocBookOutputVisitor visitor = new DocBookOutputVisitor(true);
        DBAlfrescoVisitor visitor2 = new DBAlfrescoVisitor(recurse, true);
        dge.accept(visitor);
        DBBook book = visitor.getBook();
        if (book == null) //TODO ??
            return;
        book.accept(visitor2);
        Map<Element, List<PresentationElement>> view2pe = visitor2.getView2Pe();
        Map<Element, List<PresentationElement>> view2unused = visitor2.getView2Unused();
        Map<Element, JSONArray> view2elements = visitor2.getView2Elements();
        
        List<Element> views = instanceUtils.getViewProcessOrder(start, visitor2.getHierarchyElements());
        Map<Element, ViewInstanceInfo> infos = new HashMap<Element, ViewInstanceInfo>();
        
        Set<Element> skippedViews = new HashSet<Element>();
        
        //lock elements first
        for (Element view: views) {
            if (ProjectUtilities.isElementInAttachedProject(view)) {
                ValidationRuleViolation violation = new ValidationRuleViolation(view, "[IN MODULE] This view is in a module and was not processed.");
                viewInProject.addViolation(violation);
                skippedViews.add(view);
                continue;
            }
            if (cannotChange != null && cannotChange.contains(view.getID())) {
                updateFailed.addViolation(new ValidationRuleViolation(view, "[UPDATE FAILED] This view failed to update from MMS."));
            }
            JSONArray es = view2elements.get(view);
            Object eles = StereotypesHelper.getStereotypePropertyFirst(view, viewClassStereotype, "elements");
            boolean needChange = true;
            if (eles instanceof String) {
                try {
                    JSONArray elements = (JSONArray)JSONValue.parse((String)eles);
                    if (Utils.jsonArraySetDiff(elements, es)) {
                        needChange = false;
                    }
                } catch (Exception e) {}
            }
            if (needChange) {
                if (!Utils.tryToLock(project, view, isFromTeamwork)) {
                    ValidationRuleViolation violation = new ValidationRuleViolation(view, "[NOT EDITABLE (view displayed elements)] The list of view displayed elements cannot be updated.");
                    uneditableElements.addViolation(violation);
                }
                needEdit.add(view);
            }
            if (instanceUtils.needLockForEditConstraint(view, view2pe.get(view))) {
                Constraint c = Utils.getViewConstraint(view);
                if (c != null) {
                    if (!Utils.tryToLock(project, c, isFromTeamwork)) {
                        ValidationRuleViolation vrv = new ValidationRuleViolation(c, "[NOT EDITABLE (VIEW CONTENT)] This view constraint can't be updated.");
                        uneditableContent.addViolation(vrv);
                    }
                    needEdit.add(c);
                }
            }
            lockInstances(view2pe.get(view));
        }
        
        //create session
        //from view hierarchy top down: create view instance package first
        //then for view instances bottom up: create or update instances
        for (Element view: views) {
            if (skippedViews.contains(view))
                continue;
            Package p = organizer.createOrMoveViewInstancePackage(view);
            handlePes(view2pe.get(view), p);
            Constraint c = Utils.getViewConstraint(view);
            if (c == null || (needEdit.contains(c) && c.isEditable())) {
                instanceUtils.updateOrCreateConstraint(view, view2pe.get(view));
            }
            if (needEdit.contains(view) && view.isEditable())
                StereotypesHelper.setStereotypePropertyValue(view, viewClassStereotype, "elements", view2elements.get(view).toJSONString());
        }
        //end session

        ImageValidator iv = new ImageValidator(visitor2.getImages());
        // this checks images generated from the local generation against what's on the web based on checksum
        iv.validate();
        if (!iv.getRule().getViolations().isEmpty() || suite.hasErrors()) {
            ValidationSuite imageSuite = iv.getSuite();
            
            vss.add(imageSuite);
            vss.add(suite);
            if (showValidation)
                Utils.displayValidationWindow(vss, "View Generation and Images Validation");
        }
    }

    private void lockInstances(List<PresentationElement> pes) {
        for (PresentationElement pe: pes) {
            if (pe.getChildren() != null && !pe.getChildren().isEmpty()) {
                lockInstances(pe.getChildren());
            }
            if (instanceUtils.needLockForEdit(pe)) {
                if (!Utils.tryToLock(project, pe.getInstance(), isFromTeamwork)) {
                    ValidationRuleViolation vrv = new ValidationRuleViolation(pe.getInstance(), "[NOT EDITABLE (CONTENT)] This presentation element instance can't be updated.");
                    uneditableContent.addViolation(vrv);
                }
                needEdit.add(pe.getInstance());
            }
            if (pe.getInstance() != null) {
                if ((pe.isManual() && !instanceUtils.isInSomeViewPackage(pe.getInstance())
                        || !pe.isManual())) {
                    shouldMove.add(pe.getInstance());
                    Utils.tryToLock(project, pe.getInstance(), isFromTeamwork);
                }
            }
        }
    }
    
    private void handlePes(List<PresentationElement> pes, Package p) {
        for (PresentationElement pe: pes) {
            if (pe.getChildren() != null && !pe.getChildren().isEmpty()) {
                handlePes(pe.getChildren(), p);
            }
            if (pe.getInstance() == null || 
                    (pe.getInstance().isEditable() && needEdit.contains(pe.getInstance())))
                instanceUtils.updateOrCreateInstance(pe, p);
            if (shouldMove.contains(pe.getInstance()))
                organizer.moveViewInstance(pe.getInstance(), p);
        }
    }
    
    public List<ValidationSuite> getValidations() {
        return vss;
    }

    private Package createViewInstancesPackage() {
        // fix root element, set it to project instead
        return createParticularPackage(Utils.getRootElement(), viewInstPrefix, "View Instances");
    }

    private Package createUnusedInstancesPackage() {
        Package rootPackage = createParticularPackage(Utils.getRootElement(), viewInstPrefix, "View Instances");
        return createParticularPackage(rootPackage, unusedInstPrefix, "Unused View Instances");
    }

    //get or create package with id 
    private Package createParticularPackage(Package owner, String packIDPrefix, String name) {
        // fix root element, set it to project replace PROJECT with the packIDPrefix
        String viewInstID = Utils.getProject().getPrimaryProject() .getProjectID().replace("PROJECT", packIDPrefix);
        Package viewInst = (Package)Application.getInstance().getProject().getElementByID(viewInstID);
        if (viewInst == null) {
            Application.getInstance().getProject().getCounter().setCanResetIDForObject(true);
            viewInst = ef.createPackageInstance();
            viewInst.setID(viewInstID);
            viewInst.setName(name);
        }
        // either way, set the owner to the owner we passed in
        if (viewInst.isEditable() && viewInst.getOwner() != owner)
            viewInst.setOwner(owner);
        
        return viewInst;
    }
    
    public boolean getFailure() {
        return failure;
    }

}

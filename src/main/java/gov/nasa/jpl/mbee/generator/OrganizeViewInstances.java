package gov.nasa.jpl.mbee.generator;

import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.model.Document;
import gov.nasa.jpl.mbee.viewedit.ViewHierarchyVisitor;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ViolationSeverity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.impl.ElementsFactory;


/**
 * Tries to fix the following
 * <ul>
 * <li>Move view instance packages into the right hierarchy</li>
 * <li>Move view instances into the right package</li>
 * <li>Check unused view instances and move into unused package</li>
 * <li>Check if opaque instances are referenced by more than one view</li>
 * <li>Check if view have more than one canonical parent</li>
 * </ul>
 * <p></p>
 * @author dlam
 *
 */
public class OrganizeViewInstances implements RunnableWithProgress {
    private ValidationSuite suite = new ValidationSuite("View Instance Organization");
    //unable to move instances or packages to their right place
    private ValidationRule uneditableOwner = new ValidationRule("Uneditable owner", "uneditable owner", ViolationSeverity.WARNING);
    //view have no or more than one canonical parent
    private ValidationRule viewParent = new ValidationRule("viewParent", "viewParent", ViolationSeverity.WARNING);
    //an opaque instance is referenced by more than one view
    private ValidationRule instanceRef = new ValidationRule("instanceRef", "instranceRef", ViolationSeverity.WARNING);
    
    private boolean recurse;
    private Element start;
    private boolean isFromTeamwork;
    private boolean showValidation;
    private ViewInstanceUtils instanceUtils;
    private ElementsFactory ef = Application.getInstance().getProject().getElementsFactory();
    private Project project = Application.getInstance().getProject();
    private Set<Element> shouldMove = new HashSet<Element>();
    
    public OrganizeViewInstances(Element start, boolean recurse, boolean showValidation, ViewInstanceUtils viu) {
        this.start = start;
        this.recurse = recurse;
        this.showValidation = showValidation;
        this.instanceUtils = viu;
        if (this.instanceUtils == null)
            this.instanceUtils = new ViewInstanceUtils();
        suite.addValidationRule(uneditableOwner);
        suite.addValidationRule(viewParent);
        suite.addValidationRule(instanceRef);
        if (ProjectUtilities.isFromTeamworkServer(project.getPrimaryProject())) {
            isFromTeamwork = true;
        }
    }
    
    @Override
    public void run(ProgressStatus ps) { //if running by itself
        DocumentGenerator dg = new DocumentGenerator(start, null, null);
        Document dge = dg.parseDocument(true, recurse, true);
        (new PostProcessor()).process(dge);
        
        ViewHierarchyVisitor vhv = new ViewHierarchyVisitor();
        dge.accept(vhv);
        Map<Element, List<Element>> view2view = vhv.getView2ViewElements();
        List<Element> views = instanceUtils.getViewProcessOrder(start, view2view);
        
        Set<Element> skippedViews = new HashSet<Element>();
        Map<Element, ViewInstanceInfo> infos = new HashMap<Element, ViewInstanceInfo>();
        for (Element view: views) {
            if (ProjectUtilities.isElementInAttachedProject(view)) {
                skippedViews.add(view);
                continue;
            }
            infos.put(view, lockElements(view, null));
        }
        //start session
        for (Element view: views) {
            if (skippedViews.contains(view))
                continue;
            Package p = createOrMoveViewInstancePackage(view);
            ViewInstanceInfo info = infos.get(view);
            for (InstanceSpecification is: info.getOpaque())
                moveViewInstance(is, p);
            for (InstanceSpecification is: info.getManuals()) {
                if (shouldMove.contains(is))
                    moveViewInstance(is, p);
            }
        }
        //end session and show validation
    }
    
    public ViewInstanceInfo lockElements(Element view, ViewInstanceInfo infoo) { //try to lock things, doesn't matter if fails
        ViewInstanceInfo info = infoo;
        if (info == null)    
            info = instanceUtils.getCurrentInstances(view, view);
        Package viewPackage = instanceUtils.findViewInstancePackage(view);
        List<Package> parents = instanceUtils.findCorrectViewInstancePackageOwners(view);

        for (InstanceSpecification is: info.getOpaque()) {
            if (is.getOwner() != viewPackage) { //check sections
                Utils.tryToLock(project, is, isFromTeamwork); //instance needs moving
                shouldMove.add(is);
            }
        }
        for (InstanceSpecification is: info.getManuals()) {
            if (!instanceUtils.isInSomeViewPackage(is)) {
                Utils.tryToLock(project, is, isFromTeamwork); //manual instance needs moving
                shouldMove.add(is);
            }
        }
        if (viewPackage != null && !parents.contains(viewPackage.getOwner())) {
            Utils.tryToLock(project, viewPackage, isFromTeamwork); //package needs moving
            shouldMove.add(viewPackage);
        }
        return info;
    }

    public boolean moveViewInstance(InstanceSpecification is, Package owner) {
        if (is.getOwner() == owner) {
            return true;
        }
        if (!is.isEditable()) {
            ValidationRuleViolation vrv = new ValidationRuleViolation(is, "[NOT EDITABLE (OWNER)] This presentation element instance can't be moved to the right view instance package.");
            uneditableOwner.addViolation(vrv);
            return false;
        }
        is.setOwner(owner);
        return true;
    }
    
    public Package createOrMoveViewInstancePackage(Element view) {
        Package viewPackage = instanceUtils.findViewInstancePackage(view);
        List<Package> parentPackages = instanceUtils.findCorrectViewInstancePackageOwners(view);
        if (viewPackage == null) {
            viewPackage = instanceUtils.createViewInstancePackage(view, parentPackages.get(0));
        }
        if (!parentPackages.contains(viewPackage.getOwner())) {
            if (!viewPackage.isEditable()) {
                ValidationRuleViolation vrv = new ValidationRuleViolation(viewPackage, "[NOT EDITABLE (OWNER)] View instance package cannot be moved to correct owner");
                uneditableOwner.addViolation(vrv);
            } else {
                viewPackage.setOwner(parentPackages.get(0));
            }
        }
        if (parentPackages.size() > 1) {
            ValidationRuleViolation vrv = new ValidationRuleViolation(view, "[CANONICAL PARENT PACKAGE] This view has multiple parent views that uses composition and view instance package is randomly placed under one of them");
            viewParent.addViolation(vrv);
        }
        return viewPackage;
    }
}

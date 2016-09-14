package gov.nasa.jpl.mbee.generator;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.impl.ElementsFactory;
import gov.nasa.jpl.mbee.generator.validation.actions.ClearAllReferencesAction;
import gov.nasa.jpl.mbee.generator.validation.actions.FixReferenceAction;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.model.Document;
import gov.nasa.jpl.mbee.viewedit.ViewHierarchyVisitor;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ViolationSeverity;

import java.util.*;


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
 *
 * @author dlam
 */
@Deprecated
public class ViewInstancesOrganizer implements RunnableWithProgress {
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
    private PresentationElementUtils peUtils;
    private ElementsFactory ef = Application.getInstance().getProject().getElementsFactory();
    private Project project = Application.getInstance().getProject();
    private Set<Element> shouldMove = new HashSet<Element>();
    private Map<Element, PresentationElementInfo> infos = new HashMap<Element, PresentationElementInfo>();
    private List<ValidationSuite> vss = new ArrayList<ValidationSuite>();
    private Package unused = null;

    private Map<Element, List<InstanceSpecification>> all = new HashMap<Element, List<InstanceSpecification>>();
    private Map<Element, List<InstanceSpecification>> allManual = new HashMap<Element, List<InstanceSpecification>>();

    public ViewInstancesOrganizer(Element start, boolean recurse, boolean showValidation, PresentationElementUtils viu) {
        this.start = start;
        this.recurse = recurse;
        this.showValidation = showValidation;
        this.peUtils = viu;
        if (this.peUtils == null) {
            this.peUtils = new PresentationElementUtils();
        }
        suite.addValidationRule(uneditableOwner);
        suite.addValidationRule(viewParent);
        suite.addValidationRule(instanceRef);
        if (ProjectUtilities.isFromTeamworkServer(project.getPrimaryProject())) {
            isFromTeamwork = true;
        }
        vss.add(suite);
    }

    @Override
    public void run(ProgressStatus ps) { //if running by itself
        DocumentGenerator dg = new DocumentGenerator(start, null, null);
        Document dge = dg.parseDocument(true, recurse, true);
        (new PostProcessor()).process(dge);

        ViewHierarchyVisitor vhv = new ViewHierarchyVisitor();
        dge.accept(vhv);
        Map<Element, List<Element>> view2view = vhv.getView2ViewElements();
        List<Element> views = peUtils.getViewProcessOrder(start, view2view);

        Set<Element> skippedViews = new HashSet<Element>();
        for (Element view : views) {
            if (ProjectUtilities.isElementInAttachedProject(view)) {
                skippedViews.add(view);
                continue;
            }
            Package viewPackage = peUtils.findViewInstancePackage(view);
            List<Package> parents = peUtils.findCorrectViewInstancePackageOwners(view);
            if (viewPackage != null && !parents.contains(viewPackage.getOwner())) {
                Utils.tryToLock(project, viewPackage, isFromTeamwork); //package needs moving
                shouldMove.add(viewPackage);
            }
            lockElements(view, view, viewPackage);
        }
        boolean sessionCreated = false;
        try {
            if (!SessionManager.getInstance().isSessionCreated()) {
                SessionManager.getInstance().createSession("view instance organize");
                sessionCreated = true;
            }
            unused = peUtils.getOrCreateUnusedInstancePackage();
            for (Element view : views) {
                if (skippedViews.contains(view)) {
                    continue;
                }
                Package p = createOrMoveViewInstancePackage(view);
                handle(view, p);
            }
            if (sessionCreated) {
                SessionManager.getInstance().closeSession();
            }
        } catch (Exception ex) {
            if (sessionCreated) {
                SessionManager.getInstance().cancelSession();
            }
            Utils.printException(ex);
        }
        //end session and show validation
        if (showValidation) {
            if (suite.hasErrors()) {
                Utils.displayValidationWindow(vss, "Organize View Validation");
            }
            else {
                Utils.guilog("[INFO] View Organize finished.");
            }
        }
    }

    public void lockElements(Element viewOrSection, Element view, Package viewPackage) { //try to lock things, doesn't matter if fails
        PresentationElementInfo info = peUtils.getCurrentInstances(viewOrSection, view);

        for (InstanceSpecification is : info.getSections()) {
            lockElements(is, view, viewPackage);
        }
        for (InstanceSpecification is : info.getOpaque()) {
            if (is.getOwner() != viewPackage) { //check sections
                Utils.tryToLock(project, is, isFromTeamwork); //instance needs moving
                shouldMove.add(is);
            }
        }
        for (InstanceSpecification is : info.getManuals()) {
            if (peUtils.isSection(is)) {
                lockElements(is, view, viewPackage);
            }
            if (!peUtils.isInSomeViewPackage(is)) {
                Utils.tryToLock(project, is, isFromTeamwork); //manual instance needs moving
                shouldMove.add(is);
            }
        }
        if (!info.getExtraRef().isEmpty() || !info.getExtraManualRef().isEmpty()) {
            ValidationRuleViolation vrv = new ValidationRuleViolation(viewOrSection, "[REFERENCE] This view or section is referencing presentation elements from other views.");
            if (!info.getExtraRef().isEmpty()) {
                vrv.addAction(new FixReferenceAction(false, viewOrSection, view, all, allManual));
            }
            if (!info.getExtraManualRef().isEmpty()) {
                vrv.addAction(new FixReferenceAction(true, viewOrSection, view, all, allManual));
            }
            vrv.addAction(new ClearAllReferencesAction(viewOrSection, view));
            all.put(viewOrSection, info.getExtraRef());
            allManual.put(viewOrSection, info.getExtraManualRef());
            instanceRef.addViolation(vrv);
        }
        for (InstanceSpecification is : info.getUnused()) {
            Utils.tryToLock(project, is, isFromTeamwork);
        }
        infos.put(viewOrSection, info);
    }

    private void handle(Element viewOrSection, Package p) {
        PresentationElementInfo info = infos.get(viewOrSection);
        for (InstanceSpecification is : info.getSections()) {
            handle(is, p);
        }
        for (InstanceSpecification is : info.getOpaque()) {
            moveViewInstance(is, p);
        }
        for (InstanceSpecification is : info.getManuals()) {
            if (peUtils.isSection(is)) {
                handle(is, p);
            }
            if (shouldMove.contains(is)) {
                moveViewInstance(is, p);
            }
        }
        for (InstanceSpecification is : info.getUnused()) {
            moveViewInstance(is, unused);
        }
    }

    public boolean moveViewInstance(InstanceSpecification is, Package owner) {
        if (is.getOwner() == owner) {
            return true;
        }
        if (!is.isEditable()) {
            if (owner == unused) {
                return false;
            }
            ValidationRuleViolation vrv = new ValidationRuleViolation(is, "[NOT EDITABLE (OWNER)] This presentation element instance can't be moved to the right view instance package.");
            uneditableOwner.addViolation(vrv);
            return false;
        }
        is.setOwner(owner);
        return true;
    }

    public Package createOrMoveViewInstancePackage(Element view) {
        Package viewPackage = peUtils.findViewInstancePackage(view);
        List<Package> parentPackages = peUtils.findCorrectViewInstancePackageOwners(view);
        if (viewPackage == null) {
            viewPackage = peUtils.createViewInstancePackage(view, parentPackages.get(0));
        }
        if (!parentPackages.contains(viewPackage.getOwner())) {
            if (!viewPackage.isEditable()) {
                ValidationRuleViolation vrv = new ValidationRuleViolation(viewPackage, "[NOT EDITABLE (OWNER)] View instance package cannot be moved to correct owner");
                uneditableOwner.addViolation(vrv);
            }
            else {
                viewPackage.setOwner(parentPackages.get(0));
            }
        }
        if (parentPackages.size() > 1) {
            ValidationRuleViolation vrv = new ValidationRuleViolation(view, "[CANONICAL PARENT PACKAGE] This view has multiple parent views that uses composition and view instance package is randomly placed under one of them");
            viewParent.addViolation(vrv);
        }
        return viewPackage;
    }

    public void setUnused(Package p) {
        unused = p;
    }

    public ValidationSuite getSuite() {
        return suite;
    }
}

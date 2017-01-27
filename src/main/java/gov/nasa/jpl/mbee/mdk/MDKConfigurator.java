package gov.nasa.jpl.mbee.mdk;

import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.ActionsManager;
import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.actions.*;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.ui.browser.Tree;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.Activity;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import gov.nasa.jpl.mbee.mdk.actions.*;
import gov.nasa.jpl.mbee.mdk.docgen.actions.*;
import gov.nasa.jpl.mbee.mdk.ems.actions.*;
import gov.nasa.jpl.mbee.mdk.generator.DocumentGenerator;
import gov.nasa.jpl.mbee.mdk.lib.MDUtils;
import gov.nasa.jpl.mbee.mdk.lib.TicketUtils;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import gov.nasa.jpl.mbee.mdk.lib.Utils2;
import gov.nasa.jpl.mbee.mdk.model.CollectActionsVisitor;
import gov.nasa.jpl.mbee.mdk.model.Document;
import gov.nasa.jpl.mbee.mdk.model.UserScript;
import gov.nasa.jpl.mbee.mdk.model.actions.RunUserScriptAction;
import gov.nasa.jpl.mbee.mdk.model.actions.RunUserValidationScriptAction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public class MDKConfigurator implements BrowserContextAMConfigurator, DiagramContextAMConfigurator {

    private Set<ActionsManager> viewQueryCalled = new HashSet<>();

    @Override
    public int getPriority() {
        return ConfiguratorWithPriority.MEDIUM_PRIORITY;
    }

    @Override
    public void configure(ActionsManager manager, Tree browser) {
        Node no = browser.getSelectedNode();
        if (no == null) {
            return;
        }
        Object o = no.getUserObject();
        if (!(o instanceof Element)) {
            return;
        }
        List<Element> elements = new ArrayList<>();
        for (Node node : browser.getSelectedNodes()) {
            if (node == null) {
                continue;
            }
            Object ob = node.getUserObject();
            if (!(ob instanceof Element)) {
                continue;
            }
            elements.add((Element) ob);
        }
        addElementActions(manager, (Element) o, elements);
    }

    @Override
    public void configure(ActionsManager manager, DiagramPresentationElement diagram,
                          PresentationElement[] selected, PresentationElement requestor) {
        if (repainting()) {
            return;
        }

        List<Element> es = new ArrayList<>();
        for (PresentationElement pe : selected) {
            if (pe.getElement() != null) {
                es.add(pe.getElement());
            }
        }
        if (!es.isEmpty()) {
            addElementActions(manager, es.get(0), es);
        }
        addDiagramActions(manager, diagram);
    }

    public static boolean repainting() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        String lastMethod = "";
        for (StackTraceElement traceElem : trace) {
            if (traceElem.getClassName().contains("MainFrame") && (traceElem.getMethodName().equals("paint") || lastMethod.equals("paintImmediately"))) {
                //Debug.outln( "@@@ repainting() = true" );
                return true;
            }
            if (traceElem.getClassName().endsWith("RepaintManager") && traceElem.getMethodName().equals("paint")) {
                //Debug.outln( "@@@ repainting() = true" );
                return true;
            }
            //Debug.outln( "class name = " + traceElem.getClassName() + ", method name = " + traceElem.getMethodName() + ", last method name = " + lastMethod );
            lastMethod = traceElem.getMethodName();
        }
        //Debug.outln( "@@@ repainting() = false:" );
        //Debug.outln( MoreToString.Helper.toString( trace ) );
        return false;
    }

    private void dumpCategory(ActionsCategory category, int i) {
        IntStream.range(0, i++).forEach(ignored -> System.out.print("-"));
        System.out.println("[C] " + category.getID() + " : " + category.getName());
        for (ActionsCategory c : category.getCategories()) {
                dumpCategory(c, i);
        }
        for (NMAction action : category.getActions()) {
                IntStream.range(0, i).forEach(ignored -> System.out.print("-"));
                System.out.println("[A] " + action.getID() + " : " + action.getName());
        }
    }

    private void addElementActions(ActionsManager manager, Element e, List<Element> es) {
        //manager.getCategories().forEach(category -> dumpCategory(category, 0));
        Project prj = Project.getProject(e);
        if (prj == null) {
            return;
        }
        Stereotype sysmlview = Utils.getViewStereotype();
        Stereotype sysmlviewpoint = Utils.getViewpointStereotype();
        Stereotype documentView = Utils.getProductStereotype();
        Stereotype classview = Utils.getViewClassStereotype();
        Stereotype elementGroupStereotype = Utils.getElementGroupStereotype();

        // top-level context menu: Refactor With ID
        ActionsCategory refactorWithIDActionCat = myCategory(manager, "Refactor With ID", "Refactor With ID");
        if (e instanceof com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class & !(e instanceof com.nomagic.uml2.ext.magicdraw.components.mdbasiccomponents.Component)) {
            if (manager.getActionFor(ClassToComponentRefactorWithIDAction.DEFAULT_ID) == null) {
                refactorWithIDActionCat.addAction(new ClassToComponentRefactorWithIDAction(es));
            }
        }
        if (e instanceof com.nomagic.uml2.ext.magicdraw.components.mdbasiccomponents.Component) {
            if (manager.getActionFor(ComponentToClassRefactorWithIDAction.DEFAULT_ID) == null) {
                refactorWithIDActionCat.addAction(new ComponentToClassRefactorWithIDAction(es));
            }
        }
        //manager.addCategory(refactorWithIDActionCat);

        ActionsCategory modelLoad = myCategory(manager, "AlfrescoModel", "MMS");
        if (TicketUtils.isTicketSet() && !MMSAction.isDisabled()) {
            ActionsCategory models = getCategory(manager, "MMSModel", "MMSModel", modelLoad);
            if (MDUtils.isDeveloperMode()) {
                if (e instanceof Model && manager.getActionFor(CommitProjectAction.DEFAULT_ID) == null) {
                    models.addAction(new CommitProjectAction(Application.getInstance().getProject(), false, true));
                    models.addAction(new CommitProjectAction(Application.getInstance().getProject(), true, true));
                }
            }
            if (manager.getActionFor(ValidateModelAction.DEFAULT_ID) == null) {
                models.addAction(new ValidateModelAction(es, (Application.getInstance().getProject().getModel() == e) ? "Validate Models" : "Validate Models"));
            }
            if (manager.getActionFor(ValidateElementAction.DEFAULT_ID) == null) {
                models.addAction(new ValidateElementAction(es, (Application.getInstance().getProject().getModel() == e) ? "Validate Element" : "Validate Element"));
            }
            if (manager.getActionFor(ValidateElementDepthAction.DEFAULT_ID) == null) {
                models.addAction(new ValidateElementDepthAction(es, (Application.getInstance().getProject().getModel() == e) ? "Validate Models (specified depth)" : "Validate Models (specified depth)"));
            }

            /*if (e instanceof Package) {
                if (manager.getActionFor(ExportAllDocuments.DEFAULT_ID) == null)
                    models.addAction(new ExportAllDocuments(e));
            }*/
        }
        else {
            ActionsCategory login = getCategory(manager, "Login to MMS", "Login to MMS", modelLoad);
            if (manager.getActionFor(EMSLoginAction.DEFAULT_ID) == null) {
                login.addAction(new EMSLoginAction());
            }
            // Ivan: Little hack to disable category by adding a disabled child action and deriving category state using useActionForDisable
            //final MDAction mda = new MDAction(null, null, null, "null");
            //mda.updateState();
            //mda.setEnabled(false);
            //modelLoad.addAction(mda);
        }
        ActionsStateUpdater.updateActionsState();

        // add menus in reverse order since they are inserted at top
        // View Interaction menu
        if (StereotypesHelper.hasStereotypeOrDerived(e, DocGen3Profile.validationScriptStereotype)) {
            ActionsCategory c = myCategory(manager, "ViewInteraction", "View Interaction");
            UserScript us = new UserScript();
            us.setDgElement(e);
            List<Element> targets = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(e,
                    DocGen3Profile.queriesStereotype, 1, false, 1);
            targets.addAll(Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(e,
                    DocGen3Profile.oldQueriesStereotype, 1, false, 1));
            us.setTargets(Utils2.asList(targets, Object.class));
            if (manager.getActionFor(RunUserValidationScriptAction.DEFAULT_ID) == null) {
                c.addAction(new RunUserValidationScriptAction(us, true));
            }
        }
        else if (StereotypesHelper.hasStereotypeOrDerived(e, DocGen3Profile.userScriptStereotype)) {
            ActionsCategory c = myCategory(manager, "ViewInteraction", "View Interaction");
            UserScript us = new UserScript();
            us.setDgElement(e);
            List<Element> targets = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(e,
                    DocGen3Profile.queriesStereotype, 1, false, 1);
            targets.addAll(Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(e,
                    DocGen3Profile.oldQueriesStereotype, 1, false, 1));
            us.setTargets(Utils2.asList(targets, Object.class));
            if (manager.getActionFor(RunUserScriptAction.DEFAULT_ID) == null) {
                c.addAction(new RunUserScriptAction(us, true));
            }
        }
        boolean canShowGeneration = true;
        for (Element element : es) {
            if (!StereotypesHelper.hasStereotypeOrDerived(element, sysmlview) && !StereotypesHelper.hasStereotypeOrDerived(element, elementGroupStereotype)) {
                canShowGeneration = false;
                break;
            }
        }
        if (canShowGeneration) {
            // There may be no view query actions to add, in which case we need
            // to avoid adding an empty menu category, so the category is
            // removed in this case.
            // Not worth implementing multi-selection for this legacy feature. Would require refactoring and testing.
            if (classview != null && es.size() == 1 && StereotypesHelper.hasStereotypeOrDerived(e, sysmlview)) {
                Boolean collectActions = (Boolean) StereotypesHelper.getStereotypePropertyFirst(e, classview, "collectViewActions");
                if (collectActions != null && collectActions) {
                    ActionsCategory category = (ActionsCategory) manager.getActionFor("ViewInteraction");
                    if (category == null) {
                        category = new MDActionsCategory("ViewInteraction", "View Interaction");
                        category.setNested(true);
                        boolean added = addViewQueryActions(manager, category, (NamedElement) e);
                        if (added) {
                            manager.addCategory(0, category);
                        }
                    }
                }
            }

            ActionsCategory modelLoad2 = myCategory(manager, "AlfrescoModel", "MMS");
            if (TicketUtils.isTicketSet() && !MMSAction.isDisabled()) {

                ActionsCategory viewInstances = getCategory(manager, "MMSViewInstance", "MMSViewInstance", modelLoad2);
                NMAction action = manager.getActionFor(GenerateViewPresentationAction.DEFAULT_ID);
                if (action == null) {
                    viewInstances.addAction(new GenerateViewPresentationAction(es, false));
                }
                action = manager.getActionFor(GenerateViewPresentationAction.RECURSE_DEFAULT_ID);
                if (action == null) {
                    viewInstances.addAction(new GenerateViewPresentationAction(es, true));
                }
                /*action = manager.getActionFor(OrganizeViewInstancesAction.DEFAULT_ID);
                if (action == null) {
                    viewInstances.addAction(new OrganizeViewInstancesAction(es, false));
                }
                action = manager.getActionFor(OrganizeViewInstancesAction.RECURSE_DEFAULT_ID);
                if (action == null) {
                    viewInstances.addAction(new OrganizeViewInstancesAction(es, true));
                }
                action = manager.getActionFor(OneClickUpdateDoc.DEFAULT_ID);
                if (action == null) {
                    viewInstances.addAction(new OneClickUpdateDoc(es));
                }*/
            }
            else {
                ActionsCategory login = getCategory(manager, "Login to MMS", "Login to MMS", modelLoad);
                if (manager.getActionFor(EMSLoginAction.DEFAULT_ID) == null) {
                    login.addAction(new EMSLoginAction());
                }
                // Ivan: Little hack to disable category by adding a disabled child action and deriving category state using useActionForDisable
                //final MDAction mda = new MDAction(null, null, null, "null");
                //mda.updateState();
                //mda.setEnabled(false);
                //modelLoad2.addAction(mda);
            }

            if (StereotypesHelper.hasStereotype(Project.getProject(e).getPrimaryModel(), "ModelManagementSystem")) {
                ActionsCategory tracingCategory = manager.getCategory("TRACING_CATEGORY");
                if (tracingCategory != null) {
                    NMAction action = manager.getActionFor(MMSViewLinkAction.DEFAULT_ID);
                    if (action == null) {
                        tracingCategory.addAction(new MMSViewLinkAction(es));
                    }
                }
            }

            ActionsStateUpdater.updateActionsState();

            //ActionsCategory c = myCategory(manager, "ViewEditor", "View Editor");
            //action = manager.getActionFor(ExportViewAction.DEFAULT_ID);
            //if (action == null)
            //addEditableViewActions(c, (NamedElement)e);
        }
        /*if (StereotypesHelper.hasStereotype(e, ViewEditorProfile.project)) { // REVIEW
                                                                         // --
                                                                         // hasStereotypeOrDerived()?
            ActionsCategory c = myCategory(manager, "ViewEditor", "View Editor");
            NMAction act = manager.getActionFor(OrganizeViewEditorAction.DEFAULT_ID);
            if (act == null)
                c.addAction(new OrganizeViewEditorAction(e));
            act = manager.getActionFor(DeleteProjectAction.DEFAULT_ID);
            if (act == null)
                c.addAction(new DeleteProjectAction(e));
            act = manager.getActionFor(EMSLogoutAction.DEFAULT_ID);
            if (act == null)
                c.addAction(new EMSLogoutAction());
        }
        if (StereotypesHelper.hasStereotype(e, ViewEditorProfile.volume)) { // REVIEW
                                                                        // --
                                                                        // hasStereotypeOrDerived()?
            ActionsCategory c = myCategory(manager, "ViewEditor", "View Editor");
            NMAction act = manager.getActionFor(DeleteVolumeAction.DEFAULT_ID);
            if (act == null)
                c.addAction(new DeleteVolumeAction(e));
            act = manager.getActionFor(EMSLogoutAction.DEFAULT_ID);
            if (act == null)
                c.addAction(new EMSLogoutAction());
        }
        if (StereotypesHelper.hasStereotype(e, ViewEditorProfile.document)
                || StereotypesHelper.hasStereotypeOrDerived(e, documentView)) {
            ActionsCategory c = myCategory(manager, "ViewEditor", "View Editor");
            NMAction act = manager.getActionFor(DeleteDocumentAction.DEFAULT_ID);
            if (act == null)
                c.addAction(new DeleteDocumentAction(e));
            if (StereotypesHelper.hasStereotypeOrDerived(e, documentView)) {
                act = manager.getActionFor(OrganizeDocumentAction.DEFAULT_ID);
                if (act == null)
                    c.addAction(new OrganizeDocumentAction(e));
            }
        }*/

        if (e == Application.getInstance().getProject().getModel()) {
            NMAction act = null;
            ActionsCategory c = myCategory(manager, "DocGen", "DocGen");
            // DefaultPropertyResourceProvider pp = new
            // DefaultPropertyResourceProvider();
            act = manager.getActionFor(ValidateOldDocgen.DEFAULT_ID);
            if (act == null) {
                c.addAction(new ValidateOldDocgen());
            }
        }

        // DocGen menu
        if ((e instanceof Activity && StereotypesHelper.hasStereotypeOrDerived(e,
                DocGen3Profile.documentStereotype)) || StereotypesHelper.hasStereotypeOrDerived(e, sysmlview)) {
            NMAction act = null;
            ActionsCategory c = myCategory(manager, "DocGen", "DocGen");
            // DefaultPropertyResourceProvider pp = new
            // DefaultPropertyResourceProvider();
            act = manager.getActionFor(ValidateDocument3Action.DEFAULT_ID);
            if (act == null) {
                c.addAction(new ValidateDocument3Action(e));
            }

            act = manager.getActionFor(ValidateViewStructureAction.DEFAULT_ID);
            if (act == null && e instanceof Classifier) {
                c.addAction(new ValidateViewStructureAction(e));
            }

            act = manager.getActionFor(ViewDocument3Action.DEFAULT_ID);
            if (act == null) {
                c.addAction(new ViewDocument3Action(e));
            }

            act = manager.getActionFor(GenerateDocumentAction.DEFAULT_ID);
            if (act == null) {
                c.addAction(new GenerateDocumentAction(e));
            }

            if (StereotypesHelper.hasStereotypeOrDerived(e, documentView)) {
                if (e instanceof Package) {
                    act = manager.getActionFor(NumberDependencyAction.DEFAULT_ID);
                    if (act == null) {
                        c.addAction(new NumberDependencyAction(e));
                    }
                    act = manager.getActionFor(MigrateToClassViewAction.DEFAULT_ID);
                    if (act == null) {
                        c.addAction(new MigrateToClassViewAction(e));
                    }
                }
                if (e instanceof Class) {
                    act = manager.getActionFor(NumberAssociationAction.DEFAULT_ID);
                    if (act == null) {
                        c.addAction(new NumberAssociationAction((Class) e));
                    }
                }
                //act = manager.getActionFor(PublishDocWebAction.DEFAULT_ID);
                //if (act == null) 
                //   c.addAction(new PublishDocWebAction((NamedElement)e));
            }
            /*
             * if (e instanceof Activity &&
             * StereotypesHelper.hasStereotypeOrDerived(e,
             * DocGen3Profile.documentStereotype)) { act =
             * manager.getActionFor(PublishDocWebAction.DEFAULT_ID); if (act ==
             * null) c.addAction(new PublishDocWebAction((NamedElement)e)); }
             */
        }

        if (StereotypesHelper.hasStereotypeOrDerived(e, sysmlviewpoint)) {
            ActionsCategory c = myCategory(manager, "DocGen", "DocGen");
            NMAction act = manager.getActionFor(InstanceViewpointAction.DEFAULT_ID);
            if (act == null) {
                c.addAction(new InstanceViewpointAction(e));
            }
        }
        // if ( ( e instanceof Activity &&
        // StereotypesHelper.hasStereotypeOrDerived( e,
        // DocGen3Profile.documentStereotype ) ) ||
        // StereotypesHelper.hasStereotypeOrDerived( e, sysmlview ) ) {
        // ActionsCategory c = myCategory( manager, "DocGen", "DocGen" );
        // NMAction act = manager.getActionFor( "DocGenComments" );
        // if ( act == null ) addCommentActions( c, (NamedElement)e );
        // }

//        if (e instanceof Property) {
//        	ArrayList<Property> els = new ArrayList<Property>();
//        	for (Element el: es) {
//        		if (el instanceof Property)
//        			els.add((Property)el);
//        	}
//        	ActionsCategory c = myCategory(manager, "DocGen", "DocGen");
//        	NMAction act = manager.getActionFor(CreateRestrictedValueAction.DEFAULT_ID);
//        	if (act == null)
//        		c.addAction(new CreateRestrictedValueAction((Property) e, els));
//        }
        ArrayList<Property> selectedProperties = new ArrayList<Property>();
        for (Element el : es) {
            if (el instanceof Property) {
                selectedProperties.add((Property) el);
            }
        }
        if (!(selectedProperties.isEmpty())) {
            ActionsCategory c = myCategory(manager, "DocGen", "DocGen");
            NMAction act = manager.getActionFor(CreateRestrictedValueAction.DEFAULT_ID);
            if (act == null) {
                c.addAction(new CreateRestrictedValueAction(selectedProperties));
            }
        }
    }

    private void addDiagramActions(ActionsManager manager, DiagramPresentationElement diagram) {
        if (diagram == null) {
            return;
        }
        Element element = diagram.getActualElement();
        if (element == null) {
            return;
        }
        Element owner = element.getOwner();
        if (owner == null || !(owner instanceof NamedElement)) {
            return;
        }
        // //this add actions for syncing to docweb comments
        // if (StereotypesHelper.hasStereotypeOrDerived(owner,
        // DocGen3Profile.documentViewStereotype)) {
        // ActionsCategory category = myCategory(manager, "DocGen", "DocGen");
        // NMAction action = manager.getActionFor("DocGenComments");
        // if (action == null)
        // addCommentActions(category, (NamedElement) owner);
        // }
    }

    /**
     * add actions related to view editor (this includes view comments)
     *
     * @param parent
     * @param e
     */
 /*   private void addEditableViewActions(ActionsCategory parent, NamedElement e) {
        ActionsCategory c = parent; // new ActionsCategory("EditableView",
                                    // "Editable View");
        c.addAction(new ImportViewDryAction(e));
        c.addAction(new ExportViewAction(e));
        c.addAction(new ExportViewHierarchyAction(e));
        c.addAction(new ImportViewAction(e));
        c.addAction(new SynchronizeViewAction(e));
        c.addAction(new ExportViewCommentsAction(e));
        c.addAction(new ImportViewCommentsAction(e));
        c.addAction(new ViewViewCommentsAction(e));
        ActionsCategory a = new MDActionsCategory("AdvanceEditor", "ModelLoad");
        a.setNested(true);
        a.addAction(new ImportViewRecursiveAction(e));
        a.addAction(new ExportViewRecursiveAction(e));
        a.addAction(new SynchronizeViewRecursiveAction(e));
        c.addAction(a);
        // c.setNested(true);
        // synchronized (this) { // saw a concurrency error at some point
        // parent.addAction(c);
        // parent.getCategories().add(c);
        // }
    }
*/

    /**
     * Gets the specified category, creates it if necessary.
     *
     * @param manager
     * @param id
     * @param name
     * @return category with given id/name
     */
    private ActionsCategory myCategory(ActionsManager manager, String id, String name) {
        ActionsCategory category = (ActionsCategory) manager.getActionFor(id); // getCategory(id);
        if (category == null) {
            category = new MDActionsCategory(id, name);
            category.setNested(true);
            category.setUseActionForDisable(true);
            manager.addCategory(0, category);
        }
        return category;
    }

    private ActionsCategory getCategory(ActionsManager manager, String id, String name, ActionsCategory parent) {
        ActionsCategory category = (ActionsCategory) manager.getActionFor(id);
        if (category == null) {
            //category = myCategory(manager, id, name); 
            category = new MDActionsCategory(id, name);
            category.setNested(false); //this is to just get separators, not actual nested category
            parent.addAction(category);
        }
        return category;
    }

    /**
     * this should be used to add actions that're possible when user right
     * clicks on a view<br/>
     * it parses the single view, gets any document model that'll result in
     * running script, editable table, validation rule
     *
     * @param parent
     * @param e
     */
    private boolean addViewQueryActions(ActionsManager manager, ActionsCategory parent, NamedElement e) {
        if (viewQueryCalled.contains(manager)) {
            return false;
        }
        DocumentGenerator dg = new DocumentGenerator(e, null, null);
        Document dge = dg.parseDocument(true, false, false);
        CollectActionsVisitor cav = new CollectActionsVisitor();
        dge.accept(cav);

        boolean added = false;
        if (cav.getActions().size() > 0) {
            for (MDAction a : cav.getActions()) {
                parent.addAction(a);
            }
            added = true;
        }
        parent.setNested(true);
        viewQueryCalled.clear();
        viewQueryCalled.add(manager);
        return added;
    }

}

package org.openmbee.mdk;

import com.nomagic.actions.AMConfigurator;
import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.ActionsManager;
import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.actions.*;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.sysml.util.SysMLProfile;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.ui.browser.Tree;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import org.openmbee.mdk.actions.ExportToJsonRecursivelyAction;
import org.openmbee.mdk.docgen.actions.ValidateAllViewsAction;
import org.openmbee.mdk.docgen.actions.ValidateViewAction;
import org.openmbee.mdk.docgen.actions.PreviewDocumentAction;
import org.openmbee.mdk.generator.DocumentGenerator;
import org.openmbee.mdk.migrate.actions.GroupsMigrationAction;
import org.openmbee.mdk.model.CollectActionsVisitor;
import org.openmbee.mdk.model.Document;
import org.openmbee.mdk.model.UserScript;
import org.openmbee.mdk.model.actions.RunUserScriptAction;
import org.openmbee.mdk.model.actions.RunUserValidationScriptAction;
import org.openmbee.mdk.ocl.actions.OclQueryAction;
import org.openmbee.mdk.options.MDKProjectOptions;
import org.openmbee.mdk.util.MDUtils;
import org.openmbee.mdk.util.TicketUtils;
import org.openmbee.mdk.util.Utils;
import org.openmbee.mdk.util.Utils2;
import org.openmbee.mdk.actions.InstanceViewpointAction;
import org.openmbee.mdk.actions.MMSViewLinkAction;
import org.openmbee.mdk.mms.actions.*;

import java.util.*;

public class MDKConfigurator implements BrowserContextAMConfigurator, DiagramContextAMConfigurator, AMConfigurator {

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
                return true;
            }
            if (traceElem.getClassName().endsWith("RepaintManager") && traceElem.getMethodName().equals("paint")) {
                return true;
            }
            lastMethod = traceElem.getMethodName();
        }
        return false;
    }

    private void addElementActions(ActionsManager manager, Element e, List<Element> es) {
        Project project = Project.getProject(e);
        if (project == null && !es.isEmpty()) {
            project = Project.getProject(es.iterator().next());
        }
        if (project == null) {
            return;
        }
        SysMLProfile sysml = SysMLProfile.getInstance(e);
        SysMLExtensions profile = SysMLExtensions.getInstance(e);
        Stereotype viewStereotype = sysml.view().getStereotype();
        Stereotype sysmlviewpoint = sysml.viewpoint().getStereotype();
        Stereotype elementGroupStereotype = sysml.elementGroup().getStereotype();

        {
            ActionsCategory exportCategory = myCategory(manager, "MDK_JSON_Serialization", "MDK JSON Export");

            if (es != null) {
                ExportToJsonRecursivelyAction elementOnly = ExportToJsonRecursivelyAction.exportElementOnly(es);
                if (manager.getActionFor(elementOnly.getID()) == null) {
                    exportCategory.addAction(elementOnly);
                }
                ExportToJsonRecursivelyAction exportElementHierarchy = ExportToJsonRecursivelyAction.exportElementHierarchy(es);
                if (manager.getActionFor(exportElementHierarchy.getID()) == null) {
                    exportCategory.addAction(exportElementHierarchy);
                }
            } else if (e != null) {
                ExportToJsonRecursivelyAction elementOnly = ExportToJsonRecursivelyAction.exportElementOnly(e);
                if (manager.getActionFor(elementOnly.getID()) == null) {
                    exportCategory.addAction(elementOnly);
                }
                ExportToJsonRecursivelyAction exportElementHierarchy = ExportToJsonRecursivelyAction.exportElementHierarchy(e);
                if (manager.getActionFor(exportElementHierarchy.getID()) == null) {
                    exportCategory.addAction(exportElementHierarchy);
                }
            }
        }

        ActionsCategory modelLoad = myCategory(manager, "MMSContext", "MMS");
        if (!TicketUtils.isTicketSet(project)) {
            ActionsCategory login = getCategory(manager, "LoginOption", "LoginOption", modelLoad);
            if (manager.getActionFor(MMSLoginAction.DEFAULT_ID) == null) {
                login.addAction(new MMSLoginAction());
            }
        }
        ActionsCategory models = getCategory(manager, "MMSModel", "MMSModel", modelLoad);
        if (MDUtils.isDeveloperMode()) {
            if (e instanceof Model) {
                if (manager.getActionFor(CommitOrgAction.DEFAULT_ID) == null) {
                    models.addAction(new CommitOrgAction(project, true));
                }
                if (manager.getActionFor(CommitProjectAction.DEFAULT_ID) == null) {
                    models.addAction(new CommitProjectAction(project, false, true));
                    models.addAction(new CommitProjectAction(project, true, true));
                }
            }

        }
        if (manager.getActionFor(ValidateElementRecursivelyAction.DEFAULT_ID) == null) {
            models.addAction(new ValidateElementRecursivelyAction(es, "Validate Models"));
        }
        if (manager.getActionFor(ValidateElementAction.DEFAULT_ID) == null) {
            models.addAction(new ValidateElementAction(es, "Validate Element"));
        }
        ActionsStateUpdater.updateActionsState();

        // add menus in reverse order since they are inserted at top
        // View Interaction menu
        if (StereotypesHelper.hasStereotypeOrDerived(e, profile.validationScript().getStereotype())) {
            ActionsCategory c = myCategory(manager, "ViewInteraction", "View Interaction");
            UserScript us = new UserScript();
            us.setDgElement(e);
            List<Element> targets = Utils.collectDirectedRelatedElementsByRelationshipStereotype(e,
                    SysMLProfile.getInstance(e).expose().getStereotype(), 1, true, 1);
            us.setTargets(Utils2.asList(targets, Object.class));
            if (manager.getActionFor(RunUserValidationScriptAction.DEFAULT_ID) == null) {
                c.addAction(new RunUserValidationScriptAction(us, true));
            }
        }
        else if (StereotypesHelper.hasStereotypeOrDerived(e, profile.userScript().getStereotype())) {
            ActionsCategory c = myCategory(manager, "ViewInteraction", "View Interaction");
            UserScript us = new UserScript();
            us.setDgElement(e);
            List<Element> targets = Utils.collectDirectedRelatedElementsByRelationshipStereotype(e,
                    SysMLProfile.getInstance(e).expose().getStereotype(), 1, true, 1);
            us.setTargets(Utils2.asList(targets, Object.class));
            if (manager.getActionFor(RunUserScriptAction.DEFAULT_ID) == null) {
                c.addAction(new RunUserScriptAction(us, true));
            }
        }
        boolean canShowGeneration = true;
        for (Element element : es) {
            if (!StereotypesHelper.hasStereotypeOrDerived(element, viewStereotype) && !StereotypesHelper.hasStereotypeOrDerived(element, elementGroupStereotype)) {
                canShowGeneration = false;
                break;
            }
        }
        if (canShowGeneration) {
            // There may be no view query actions to add, in which case we need
            // to avoid adding an empty menu category, so the category is
            // removed in this case.
            // Not worth implementing multi-selection for this legacy feature. Would require refactoring and testing.
            if (es.size() == 1 && StereotypesHelper.hasStereotypeOrDerived(e, profile.view().getStereotype())) {
                Boolean collectActions = (Boolean) StereotypesHelper.getStereotypePropertyFirst(e, profile.view().getCollectViewActionsProperty());
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

            ActionsCategory modelLoad2 = myCategory(manager, "MMSContext", "MMS");
            ActionsCategory viewInstances = getCategory(manager, "MMSViewInstance", "MMSViewInstance", modelLoad2);
            NMAction action = manager.getActionFor(GenerateViewPresentationAction.DEFAULT_ID);
            if (action == null) {
                viewInstances.addAction(new GenerateViewPresentationAction(new LinkedHashSet<>(es), false));
            }
            action = manager.getActionFor(GenerateViewPresentationAction.RECURSE_DEFAULT_ID);
            if (action == null) {
                viewInstances.addAction(new GenerateViewPresentationAction(new LinkedHashSet<>(es), true));
            }

            if (MDKProjectOptions.getMbeeEnabled(project)) {
                ActionsCategory tracingCategory = manager.getCategory("TRACING_CATEGORY");
                if (tracingCategory != null) {
                    action = manager.getActionFor(MMSViewLinkAction.DEFAULT_ID);
                    if (action == null) {
                        tracingCategory.addAction(new MMSViewLinkAction(es));
                    }
                }
            }

            ActionsStateUpdater.updateActionsState();
        }

        // DocGen menu
        if (e instanceof Class) {
            Class view = (Class) e;
            if (StereotypesHelper.hasStereotypeOrDerived(view, viewStereotype)) {
                ActionsCategory category = myCategory(manager, "DocGen", "DocGen");
                NMAction act = manager.getActionFor(ValidateViewAction.DEFAULT_ID);
                if (act == null) {
                    category.addAction(new ValidateViewAction(view));
                }
                act = manager.getActionFor(ValidateViewAction.RECURSIVE_DEFAULT_ID);
                if (act == null) {
                    category.addAction(new ValidateViewAction(view, true));
                }
                act = manager.getActionFor(PreviewDocumentAction.DEFAULT_ID);
                if (act == null) {
                    category.addAction(new PreviewDocumentAction(e));
                }

                ActionsCategory generateCategory = (ActionsCategory) manager.getActionFor("Generate");
                if (generateCategory == null) {
                	generateCategory = new ActionsCategory("Generate", "Generate");
                	generateCategory.setNested(true);
                	category.addAction(generateCategory);
                }
                act = manager.getActionFor(GenerateDocBookAction.DEFAULT_ID);
                if (act == null) {
                    generateCategory.addAction(new GenerateDocBookAction(e));
                }
                act = manager.getActionFor(GeneratePdfAction.DEFAULT_ID);
                if (act == null) {
                    generateCategory.addAction(new GeneratePdfAction(e));
                }
            }
        }

        if (StereotypesHelper.hasStereotypeOrDerived(e, sysmlviewpoint)) {
            ActionsCategory c = myCategory(manager, "DocGen", "DocGen");
            NMAction act = manager.getActionFor(InstanceViewpointAction.DEFAULT_ID);
            if (act == null) {
                c.addAction(new InstanceViewpointAction(e));
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
    }

    /**
     * Gets the specified category, creates it if necessary.
     *
     * @param manager
     * @param id
     * @param name
     * @return category with given id/name
     */
    private ActionsCategory myCategory(ActionsManager manager, String id, String name) {
        ActionsCategory category = (ActionsCategory) manager.getActionFor(id);
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

    @Override
    public void configure(ActionsManager manager) {
        ActionsCategory category = manager.getCategory(MDKPlugin.MAIN_TOOLBAR_CATEGORY_NAME);
        if (category == null) {
            category = new MDActionsCategory(MDKPlugin.MAIN_TOOLBAR_CATEGORY_NAME, MDKPlugin.MAIN_TOOLBAR_CATEGORY_NAME, null, ActionsGroups.APPLICATION_RELATED);
            category.setNested(true);
        }
        manager.removeCategory(category);
        manager.addCategory(category);
        category.addAction(new OclQueryAction());

        MDActionsCategory validateCategory = new MDActionsCategory(MDKConfigurator.class.getSimpleName() + "-Validate", "Validate");
        validateCategory.setNested(true);
        category.addAction(validateCategory);
        validateCategory.addAction(new ValidateAllViewsAction());

        MDActionsCategory migrateCategory = new MDActionsCategory(MDKConfigurator.class.getSimpleName() + "-Migrate", "Migrate");
        migrateCategory.setNested(true);
        category.addAction(migrateCategory);
        migrateCategory.addAction(new GroupsMigrationAction());

        {
            MDActionsCategory exportCategory = new MDActionsCategory(MDKConfigurator.class.getSimpleName() + "-Serialization", "MDK JSON");
            exportCategory.setNested(true);
            category.addAction(exportCategory);

            exportCategory.addAction(ExportToJsonRecursivelyAction.exportEntirePrimaryModel());
        }
    }
}

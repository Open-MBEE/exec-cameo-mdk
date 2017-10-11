package gov.nasa.jpl.mbee.mdk.systems_reasoner;

import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.ActionsManager;
import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.actions.*;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.ui.browser.Tree;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors.Behavior;
import gov.nasa.jpl.mbee.mdk.systems_reasoner.actions.*;

import java.util.ArrayList;
import java.util.List;

public class SRConfigurator implements BrowserContextAMConfigurator, DiagramContextAMConfigurator {

    public static final String NAME = "Systems Reasoner";
    public static final String ID = "Specialize Structure";
    public static final String ID_RECURSIVE = "Specialize Structure Recursively";
    public static final String ID_RECURSIVE_INDIVIDUAL = "Specialize Recursively & Individually";
    private SRAction validateAction, importCSVAction, specializeStructureRecursiveAction, specializeStructureAction, createBSTAction, ontoBehaviorAction, instance2BSTAction, createInstanceMenuAction, aspectAction, selectAspectAction;

    @Override
    public int getPriority() {
        return 0; // medium
    }

    @Override
    public void configure(ActionsManager manager, Tree tree) {
        final List<Element> elements = new ArrayList<Element>();
        for (final Node n : tree.getSelectedNodes()) {
            if (n.getUserObject() instanceof Element) {
                elements.add((Element) n.getUserObject());
            }
        }
        configure(manager, elements);
    }

    @Override
    public void configure(ActionsManager manager, DiagramPresentationElement diagram, PresentationElement[] selected, PresentationElement requestor) {
        final List<Element> elements = new ArrayList<Element>();
        for (final PresentationElement pe : selected) {
            if (pe.getElement() != null) {
                elements.add(pe.getElement());
            }
        }
        configure(manager, elements);
    }

    protected void configure(ActionsManager manager, List<Element> elements) {
        // refresh the actions for every new click (or selection)
        validateAction = null;
        ontoBehaviorAction = null;
        specializeStructureRecursiveAction = null;
        specializeStructureAction = null;
        createBSTAction = null;
        createInstanceMenuAction = null;
        instance2BSTAction = null;
        aspectAction = null;
        importCSVAction = null;
        selectAspectAction = null;

        ActionsCategory category = (ActionsCategory) manager.getActionFor("SRMain");
        if (category == null) {
            category = new MDActionsCategory("SRMain", "Systems Reasoner", null, ActionsGroups.APPLICATION_RELATED);
            category.setNested(true);
            // manager.addCategory(0, category);
        }
        manager.removeCategory(category);

        if (elements.size() > 1) {
            category = handleMultipleNodes(category, manager, elements);
        }
        else if (elements.size() == 1) {
            category = handleSingleNode(category, manager, elements.get(0));
        }
        else {
            return;
        }

        if (category == null) {
            return;
        }
        manager.addCategory(0, category);

        category.addAction(validateAction);
        if (elements.size() < 2) {
            if (elements.get(0) instanceof Behavior) {
                category.addAction(ontoBehaviorAction);
            }
        }
        category.addAction(importCSVAction);
        category.addAction(selectAspectAction);
        category.addAction(specializeStructureAction);
        category.addAction(specializeStructureRecursiveAction);
        category.addAction(createBSTAction);
        category.addAction(createInstanceMenuAction);
        category.addAction(instance2BSTAction);
        category.addAction(aspectAction);

        category.getActions().clear();
        category.setUseActionForDisable(true);
        if (category.isEmpty()) {
            final MDAction mda = new MDAction(null, null, null, "null");
            mda.updateState();
            mda.setEnabled(false);
            category.addAction(mda);
        }
    }

    public ActionsCategory handleMultipleNodes(ActionsCategory category, ActionsManager manager, List<Element> elements) {
         final List<InstanceSpecification> instances = new ArrayList<InstanceSpecification>();
        final List<Element> validatableElements = new ArrayList<Element>();
        boolean hasUneditable = false;

        for (Element element : elements) {
            if (element != null) {
                if (element instanceof Classifier) {
                     validatableElements.add(element);
                }
                else if (element instanceof InstanceSpecification) {
                    instances.add((InstanceSpecification) element);
                    validatableElements.add(element);
                }
                if (!hasUneditable && !element.isEditable()) {
                    hasUneditable = true;
                }
            }
        }

        // if nothing in classes, disable category and return it
        if (validatableElements.isEmpty()) {
            // category = disableCategory(category);
            return null;
        }
        // otherwise, add the classes to the ValidateAction action
        validateAction = new ValidateAction(validatableElements);
        category.addAction(validateAction);

        if (!instances.isEmpty()) {
            instance2BSTAction = new Instance2BSTAction(instances);
        }

        return category;
    }

    public ActionsCategory handleSingleNode(ActionsCategory category, ActionsManager manager, Element element) {
        if (element == null) {
            return null;
        }
        if (element instanceof Package) {
            selectAspectAction = new CopyAction(element);
        }
        if (element instanceof Classifier) {
            final Classifier classifier = (Classifier) element;
            validateAction = new ValidateAction(classifier);
            importCSVAction = new ImportCSVAction(classifier);
            ontoBehaviorAction = new CreateOntoBehaviorBlocks(classifier, false);
            specializeStructureAction = new SpecializeStructureAction(classifier, false, ID, false, false);
            specializeStructureRecursiveAction = new SpecializeStructureAction(classifier, false, ID_RECURSIVE, true, false);
            createBSTAction = new SpecializeStructureAction(classifier, false, ID_RECURSIVE_INDIVIDUAL, true, true);
            createInstanceMenuAction = new CreateInstanceMenuAction(classifier);
            selectAspectAction = new AspectSelectionAction(classifier);
        }
        else if (element instanceof InstanceSpecification) {
            final InstanceSpecification instance = (InstanceSpecification) element;
            validateAction = new ValidateAction(instance);
            ArrayList<InstanceSpecification> insts = new ArrayList();
            insts.add(instance);
            instance2BSTAction = new Instance2BSTAction(insts);
        }
        else {
            return null;
        }
        return category;
    }

    public static ActionsCategory disableCategory(ActionsCategory category) {
        // once all the categories are disabled, the action category will be disabled
        // this is defined in the configure method: category.setNested(true);
        for (NMAction s : category.getActions()) {
            if (s instanceof SRAction) {
                SRAction sra = (SRAction) s;
                sra.disable("Not Editable");
            }
        }
        return category;
    }
}

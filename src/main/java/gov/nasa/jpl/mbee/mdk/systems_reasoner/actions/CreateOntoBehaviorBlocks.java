package gov.nasa.jpl.mbee.mdk.systems_reasoner.actions;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.ui.dialogs.MDDialogParentProvider;
import com.nomagic.magicdraw.ui.dialogs.SelectElementInfo;
import com.nomagic.magicdraw.ui.dialogs.SelectElementTypes;
import com.nomagic.magicdraw.ui.dialogs.selection.ElementSelectionDlg;
import com.nomagic.magicdraw.ui.dialogs.selection.ElementSelectionDlgFactory;
import com.nomagic.magicdraw.ui.dialogs.selection.SelectionMode;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.Action;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.Connector;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.ConnectorEnd;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.*;
import gov.nasa.jpl.mbee.mdk.api.ElementFinder;
import gov.nasa.jpl.mbee.mdk.util.Utils;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CreateOntoBehaviorBlocks extends SRAction {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_ID = "Create OntoBehavior Blocks";
    private Classifier classifier;
    private Classifier behaviorOccurence = null;
    private int stateCounter;
    private int regionCounter;
    private HashMap<State, Property> stateProps;
    private boolean isValidationMode;

    public CreateOntoBehaviorBlocks(final Classifier classifier, boolean isValidationMode) {
        super(DEFAULT_ID, classifier);
        this.classifier = classifier;
        this.isValidationMode = isValidationMode;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final List<java.lang.Class<?>> types = new ArrayList<java.lang.Class<?>>();
        types.add(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class);
        types.add(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package.class);
        types.add(Model.class);

        final Frame dialogParent = MDDialogParentProvider.getProvider().getDialogParent();
        final ElementSelectionDlg dlg = ElementSelectionDlgFactory.create(dialogParent);
        dlg.setTitle("Select container for generated elements:");
        final SelectElementTypes set = new SelectElementTypes(null, types, null, null);
        final SelectElementInfo sei = new SelectElementInfo(true, false, Application.getInstance().getProject().getModel().getOwner(), true);
        ElementSelectionDlgFactory.initSingle(dlg, set, sei, classifier.getOwner());

        dlg.setSelectionMode(SelectionMode.SINGLE_MODE);
        if (dlg != null) {
            if (!isValidationMode) {
                dlg.setVisible(true);
            }
            if (isValidationMode || dlg.isOkClicked() && dlg.getSelectedElement() != null && dlg.getSelectedElement() instanceof Namespace) {
                Namespace container = null;
                if (isValidationMode) {
                    container = (Namespace) classifier.getOwner();
                }
                else {
                    container = (Namespace) dlg.getSelectedElement();
                }
                SessionManager.getInstance().createSession("create ontobehavior");
                //TODO @donbot this qualified name path doesn't exist. fix when fixing systems reasoner.
                behaviorOccurence = (Classifier) ElementFinder.getElementByQualifiedName("SysML Extensions::SystemsReasoner::BehaviorOccurence", Application.getInstance().getProject());
                Profile sysml = StereotypesHelper.getProfile(Project.getProject(classifier), "SysML");
                Stereotype block = StereotypesHelper.getStereotype(Project.getProject(classifier), "Block", sysml);


                com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class smc = createBlock(classifier, block, container);
                regionCounter = 0;
                for (Element oe : classifier.getOwnedElement()) {

                    if (oe instanceof Region) {
                        Class reg = createBlock((Region) oe, block, smc);
                        stateCounter = 0;
                        stateProps = new HashMap<State, Property>();

                        for (Transition t : ((Region) oe).getTransition()) {
                            // createStepFromTo(stateProps.get(t.getSource()), stateProps.get(t.getTarget()), statemap.get(t.getSource()));
                        }
                    }
                    else if (oe instanceof StateMachine) {
                        Class reg = createBlock((StateMachine) oe, block, smc);
                    }
                    else if (oe instanceof Action) {
                        Class reg = createBlock((Action) oe, block, smc);

                        // createProperty((Action) oe,  smc);
                    }
                }
                // final Classifier specific = (Classifier) CopyPasting.copyPasteElement(classifier, (Namespace) dlg.getSelectedElement(), true);
                // for (final Generalization generalization : Lists.newArrayList(specific.getGeneralization())) {
                // generalization.dispose();
                // }
                // for (final NamedElement ne : Lists.newArrayList(specific.getOwnedMember())) {
                // ne.dispose();
                // }

                SessionManager.getInstance().closeSession();

                // ValidateAction.validate(specific);
            }
        }
    }

    private Object createProperty(Action oe, Class smc) {
        Property prop = Project.getProject(classifier).getElementsFactory().createPropertyInstance();

        return null;
    }

    private void createStepFromTo(Property from, Property to, Class fromClass) {
        if (from != null && to != null && fromClass != null) {
            if (fromClass.getOwner() instanceof Class) {
                Class containerClass = (Class) fromClass.getOwner();
                Connector c = Project.getProject(classifier).getElementsFactory().createConnectorInstance();
                ConnectorEnd cf = Project.getProject(classifier).getElementsFactory().createConnectorEndInstance();
                ConnectorEnd ct = Project.getProject(classifier).getElementsFactory().createConnectorEndInstance();
                cf.setRole(from);
                ct.setRole(to);
                cf.setOwner(c);
                ct.setOwner(c);

                c.getEnd().add(cf);
                c.getEnd().add(ct);
                c.setOwner(containerClass);
            }

        }
    }

    private com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class createBlock(NamedElement bo, Stereotype block, Namespace container) {
        com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class smc = Project.getProject(classifier).getElementsFactory().createClassInstance();
        StereotypesHelper.addStereotype(smc, block);

        container.getOwnedElement().add(smc);
        String[] end = bo.getClassType().toString().split("\\.");
        String name = end[end.length - 1];
        smc.setName(bo.getName() + "_" + name);


        Utils.createGeneralization(behaviorOccurence, smc);

        if (container instanceof Classifier) {
            Property p = Project.getProject(classifier).getElementsFactory().createPropertyInstance();
            p.setOwner(container);
            p.setType(smc);
            if (bo instanceof State) {
                p.setName("state" + stateCounter++);
                stateProps.put((State) bo, p);
            }
            else if (bo instanceof Region) {
                p.setName("region" + regionCounter++);
            }
            else if (bo instanceof Action) {
                p.setName("action" + regionCounter++);
            }
            ((Classifier) container).getFeature().add(p);
        }
        return smc;
    }

}

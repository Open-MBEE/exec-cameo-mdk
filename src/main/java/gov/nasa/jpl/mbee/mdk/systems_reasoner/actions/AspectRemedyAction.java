package gov.nasa.jpl.mbee.mdk.systems_reasoner.actions;

import com.nomagic.magicdraw.copypaste.CopyPasting;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallBehaviorAction;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.Activity;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors.Behavior;
import gov.nasa.jpl.mbee.mdk.validation.GenericRuleViolationAction;

import java.lang.Class;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AspectRemedyAction extends GenericRuleViolationAction {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_ID = "Add Aspect Realization";
    public static HashMap<Classifier, Classifier> classifiersToAspectsMap = new HashMap<Classifier, Classifier>();

    public AspectRemedyAction(Classifier classifier, Classifier aspect) {
        super(DEFAULT_ID);
        classifiersToAspectsMap.put(classifier, aspect);
    }

    public void run() {
        List<Classifier> specials = new ArrayList<Classifier>();

        for (final Classifier aspected : classifiersToAspectsMap.keySet()) {
            Classifier aspect = classifiersToAspectsMap.get(aspected);
            final Classifier special = createSpecializedClassifier(aspect, aspected, null);
            specials.add(special);
            if (special == null) {
                Application.getInstance().getGUILog().log("Failed to specialize. Skipping aspecting.");
                continue;
            }
            if (aspected instanceof Activity) {
                CallBehaviorAction cba = Application.getInstance().getProject().getElementsFactory().createCallBehaviorActionInstance();
                cba.setName("aspectOf" + aspected.getName());
                cba.setBehavior((Behavior) special);
                cba.setOwner(aspected);
            }
            else {
                final Association association = Application.getInstance().getProject().getElementsFactory().createAssociationInstance();
                ModelHelper.setClientElement(association, aspected);
                ModelHelper.setSupplierElement(association, special);
                ModelHelper.setNavigable(ModelHelper.getFirstMemberEnd(association), true);
                ModelHelper.setNavigable(ModelHelper.getSecondMemberEnd(association), false);
                association.setOwner(aspected);
            }
            for (NamedElement aspectProps : aspect.getOwnedMember()) {
                if (aspectProps instanceof RedefinableElement) {
                    SetOrCreateRedefinableElementAction raa = new SetOrCreateRedefinableElementAction(special, (RedefinableElement) aspectProps, false);
                    raa.run();
                }
            }
        }
        ValidateAction.validate(specials);
    }

    private static Classifier createSpecializedClassifier(final Classifier general, final Classifier parent, final Property property) {
        List<Class<? extends Classifier>> UNSPECIALIZABLE_CLASSIFIERS = new ArrayList<Class<? extends Classifier>>();
        UNSPECIALIZABLE_CLASSIFIERS.add(DataType.class);
        UNSPECIALIZABLE_CLASSIFIERS.add(PrimitiveType.class);

        for (final Class<? extends Classifier> c : UNSPECIALIZABLE_CLASSIFIERS) {
            if (c.isAssignableFrom(general.getClass())) {
                Application.getInstance().getGUILog()
                        .log("Warning: " + (property != null ? property.getQualifiedName() : "< >") + " is a " + c.getSimpleName() + ", which is not specializable.");
                return null;
            }
        }

        final Classifier special = (Classifier) CopyPasting.copyPasteElement(general, parent, true);
        if (special == null) {
            return null;
        }

        special.getOwnedMember().clear();
        special.getGeneralization().clear();
        SpecializeClassifierAction.specialize(special, general);
        return special;
    }

}

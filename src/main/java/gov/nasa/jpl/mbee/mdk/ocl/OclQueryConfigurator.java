package gov.nasa.jpl.mbee.mdk.ocl;

import com.nomagic.actions.AMConfigurator;
import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.ActionsManager;
import com.nomagic.magicdraw.actions.ActionsGroups;
import com.nomagic.magicdraw.actions.BrowserContextAMConfigurator;
import com.nomagic.magicdraw.actions.DiagramContextAMConfigurator;
import com.nomagic.magicdraw.actions.MDActionsCategory;
import com.nomagic.magicdraw.ui.browser.Tree;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import gov.nasa.jpl.mbee.mdk.MDKPlugin;
import gov.nasa.jpl.mbee.mdk.actions.ValidateConstraintsAction;
import gov.nasa.jpl.mbee.mdk.ocl.actions.OclQueryAction;
import org.apache.commons.lang.ArrayUtils;

/**
 * Created by igomes on 4/18/17.
 */
public class OclQueryConfigurator implements BrowserContextAMConfigurator, DiagramContextAMConfigurator, AMConfigurator {
    @Override
    public void configure(ActionsManager manager, Tree tree) {
        configure(manager);
    }


    @Override
    public void configure(ActionsManager manager, DiagramPresentationElement diagram, PresentationElement[] selected, PresentationElement requestor) {
        configure(manager, (PresentationElement[]) ArrayUtils.add(selected, requestor));
    }

    @Override
    public void configure(ActionsManager manager) {
        configure(manager, (PresentationElement[]) null);
    }

    public void configure(ActionsManager manager, PresentationElement... elements) {
        ActionsCategory category = (ActionsCategory) manager.getActionFor(MDKPlugin.MAIN_TOOLBAR_CATEGORY_NAME);
        if (category == null) {
            category = new MDActionsCategory(MDKPlugin.MAIN_TOOLBAR_CATEGORY_NAME, MDKPlugin.MAIN_TOOLBAR_CATEGORY_NAME, null, ActionsGroups.APPLICATION_RELATED);
            category.setNested(true);
        }
        manager.addCategory(category);
        category.addAction(new OclQueryAction());
        category.addAction(new ValidateConstraintsAction());
        //category.addAction(new ValidateConstraintsAction(elements != null ? Arrays.stream(elements).map(PresentationElement::getElement).filter(Objects::nonNull).distinct().toArray(Element[]::new) : null));
    }

    @Override
    public int getPriority() {
        return 0;
    }
}

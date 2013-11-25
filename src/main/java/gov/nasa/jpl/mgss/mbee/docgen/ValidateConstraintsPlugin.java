/**
 * 
 */
package gov.nasa.jpl.mgss.mbee.docgen;

import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mgss.mbee.docgen.actions.ValidateConstraints;

import java.awt.event.ActionEvent;
import java.lang.reflect.Method;

import com.nomagic.actions.NMAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ValidateConstraintsPlugin extends MDPlugin {

    // ValidateConstraints action = null;
    /**
   * 
   */
    public ValidateConstraintsPlugin() {
        this(ValidateConstraints.class);
    }

    public ValidateConstraintsPlugin(Class<? extends NMAction> cls) {
        super(cls);
    }

    // unused -- TODO -- remove after testing
    public static void doIt(ActionEvent event, Element element) {
        ValidateConstraints action = new ValidateConstraints(element);
        action.actionPerformed(event);
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.mgss.mbee.docgen.MDPlugin#initConfigurations()
     */
    @Override
    public void initConfigurations() {
        // //Debug.turnOn();
        // if ( !MDUtils.isDeveloperMode() ) {
        // Debug.outln(
        // "ValidateConstraintsPlugin will be hidden since MD is not in developer mode."
        // );
        // return;
        // }
        Debug.outln("initializing ValidateConstraintsPlugin!");

        // Method method = ClassUtils.getMethodsForName(
        // ValidateConstraintsPlugin.class, "doIt")[ 0 ];
        // TODO -- shouldn't have to look this method up and pass it--just get
        // rid of
        // method argument in addConfiguration calls below.
        Method method = getNmActionMethod();

        String category = "MDK";

        addConfiguration("MainMenu", "", ValidateConstraints.actionText, category, method, this);
        addConfiguration("ContainmentBrowserContext", "", ValidateConstraints.actionText, category, method,
                this);
        addConfiguration("BaseDiagramContext", "Class Diagram", ValidateConstraints.actionText, category,
                method, this);
        addConfiguration("BaseDiagramContext", "Activity Diagram", ValidateConstraints.actionText, category,
                method, this);
        addConfiguration("BaseDiagramContext", "SysML Block Definition Diagram",
                ValidateConstraints.actionText, category, method, this);
        addConfiguration("BaseDiagramContext", "SysML Internal Block Diagram",
                ValidateConstraints.actionText, category, method, this);
        addConfiguration("BaseDiagramContext", "DocGen 3 View Diagram", ValidateConstraints.actionText,
                category, method, this);
        addConfiguration("BaseDiagramContext", "DocGen 3 Diagram", ValidateConstraints.actionText, category,
                method, this);
        addConfiguration("BaseDiagramContext", "View Diagram", ValidateConstraints.actionText, category,
                method, this);
        addConfiguration("BaseDiagramContext", "DocumentView", ValidateConstraints.actionText, category,
                method, this);

        Debug.outln("finished initializing TestPlugin!");
    }

}

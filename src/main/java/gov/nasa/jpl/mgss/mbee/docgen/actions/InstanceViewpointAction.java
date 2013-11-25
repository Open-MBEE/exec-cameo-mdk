package gov.nasa.jpl.mgss.mbee.docgen.actions;

import gov.nasa.jpl.mbee.lib.Utils;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;

/**
 * given a viewpoint composition hierarchy, makes the views, and have them
 * conform to the respective viewpoints
 * 
 * @author dlam
 * 
 */
@SuppressWarnings("serial")
public class InstanceViewpointAction extends MDAction {

    private Element            viewpoint;
    private Stereotype         sysmlView;
    private ElementsFactory    ef;
    private Stereotype         sysmlViewpoint;

    public static final String actionid = "InstanceViewpoint";

    public InstanceViewpointAction(Element e) {
        super(actionid, "Instance Viewpoint", null, null);
        viewpoint = e;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUILog gl = Application.getInstance().getGUILog();
        sysmlView = Utils.getViewStereotype();
        sysmlViewpoint = Utils.getViewpointStereotype();
        ef = Project.getProject(viewpoint).getElementsFactory();
        if (sysmlView == null) {
            gl.log("The sysml view stereotype cannot be found");
            return;
        }
        if (sysmlViewpoint == null) {
            gl.log("The sysml viewpoint stereotype cannot be found");
            return;
        }
        List<java.lang.Class<?>> types = new ArrayList<java.lang.Class<?>>();
        types.add(Package.class);
        Element pack = (Element)Utils.getUserSelection(types, "Choose where the instanced views should go");
        if (pack == null || !(pack instanceof Package)) {
            gl.log("you didn't select a package");
            return;
        }
        try {
            SessionManager.getInstance().createSession("instance viewpoint");
            instance((Package)pack, (Class)viewpoint, ((Class)viewpoint).getName());
            SessionManager.getInstance().closeSession();
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            gl.log(sw.toString()); // stack trace as a string
            ex.printStackTrace();
        }
    }

    private void instance(Package owner, Class vp, String name) {
        Package view = ef.createPackageInstance();
        view.setOwner(owner);
        view.setName(name);
        StereotypesHelper.addStereotype(view, sysmlView);
        Dependency conforms = ef.createDependencyInstance();
        StereotypesHelper.addStereotype(conforms, Utils.getConformsStereotype());
        ModelHelper.setClientElement(conforms, view);
        ModelHelper.setSupplierElement(conforms, vp);
        conforms.setOwner(view);
        for (Property p: vp.getOwnedAttribute()) {
            Type type = p.getType();
            if (type instanceof Class && StereotypesHelper.hasStereotypeOrDerived(type, sysmlViewpoint)) {
                if (p.getName().equals(""))
                    instance(view, (Class)type, ((Class)type).getName());
                else
                    instance(view, (Class)type, p.getName());
            }
        }
    }
}

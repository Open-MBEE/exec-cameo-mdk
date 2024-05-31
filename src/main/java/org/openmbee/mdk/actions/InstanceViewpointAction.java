package org.openmbee.mdk.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.sysml.util.SysMLProfile;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;
import org.openmbee.mdk.SysMLExtensions;
import org.openmbee.mdk.options.MDKProjectOptions;
import org.openmbee.mdk.util.Utils;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * given a viewpoint composition hierarchy, makes the views, and have them
 * conform to the respective viewpoints
 *
 * @author dlam
 */
public class InstanceViewpointAction extends MDAction {

    private static final long serialVersionUID = 1L;
    private Element viewpoint;
    private Project project;
    private Stereotype sysmlView;
    private ElementsFactory ef;
    private Stereotype sysmlViewpoint;

    public static final String DEFAULT_ID = "InstanceViewpoint";

    public InstanceViewpointAction(Element e) {
        super(DEFAULT_ID, "Instance Viewpoint", null, null);
        this.viewpoint = e;
        this.project = Project.getProject(e);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUILog gl = Application.getInstance().getGUILog();
        sysmlView = SysMLProfile.getInstanceByProject(project).view().getStereotype();
        sysmlViewpoint = SysMLProfile.getInstanceByProject(project).viewpoint().getStereotype();
        ef = Project.getProject(viewpoint).getElementsFactory();
        if (sysmlView == null) {
            gl.log("The view stereotype cannot be found");
            return;
        }
        if (sysmlViewpoint == null) {
            gl.log("The sysml viewpoint stereotype cannot be found");
            return;
        }
        List<java.lang.Class<?>> types = new ArrayList<>();
        types.add(Package.class);
        Element pack = (Element) Utils.getUserSelection(types, "Choose where the instanced views should go");
        if (pack == null || !(pack instanceof Package)) {
            gl.log("you didn't select a package");
            return;
        }
        Project project = Application.getInstance().getProject();
        try {
            SessionManager.getInstance().createSession(project, "instance viewpoint");
            instance(pack, (Class) viewpoint, ((Class) viewpoint).getName());
            SessionManager.getInstance().closeSession(project);
        } catch (Exception ex) {
            SessionManager.getInstance().cancelSession(project);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            gl.log(sw.toString()); // stack trace as a string
            ex.printStackTrace();
        }
    }

    private Class instance(Element owner, Class vp, String name) {
        Class view = ef.createClassInstance();
        view.setOwner(owner);
        view.setName(name);
        if (MDKProjectOptions.instanceVPDoc(project)) {
            String vp_doc = ModelHelper.getComment(vp);
            if (vp_doc.isEmpty()) {
                vp_doc = name + " Placeholder Text";
            }
            ModelHelper.setComment(view, vp_doc);
        }
        
        
        StereotypesHelper.addStereotype(view, sysmlView);
        Generalization conforms = ef.createGeneralizationInstance();
        StereotypesHelper.addStereotype(conforms, SysMLProfile.getInstance(owner).conform().getStereotype());
        ModelHelper.setClientElement(conforms, view);
        ModelHelper.setSupplierElement(conforms, vp);
        conforms.setOwner(view);
        for (Property p : vp.getOwnedAttribute()) {
            Type type = p.getType();
            if (type instanceof Class && StereotypesHelper.hasStereotypeOrDerived(type, sysmlViewpoint)) {
                Class child = null;
                if (p.getName().isEmpty()) {
                    child = instance(view, (Class) type, type.getName());
                }
                else {
                    child = instance(view, (Class) type, p.getName());
                }
                Association asso = ef.createAssociationInstance();
                asso.getMemberEnd().get(0).setOwner(view);
                asso.getMemberEnd().get(0).setType(child);
                asso.getMemberEnd().get(0).setAggregation(p.getAggregation());
                asso.getMemberEnd().get(1).setType(view);
                asso.getMemberEnd().get(1).setOwner(asso);
                asso.setOwner(owner);
            }
        }
        return view;
    }
}

/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").  
 * U.S. Government sponsorship acknowledged.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice, this list of 
 *    conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list 
 *    of conditions and the following disclaimer in the documentation and/or other materials 
 *    provided with the distribution.
 *  - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory, 
 *    nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER  
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.nasa.jpl.mbee.mdk.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;
import gov.nasa.jpl.mbee.mdk.lib.Utils;

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
    private Stereotype sysmlView;
    private ElementsFactory ef;
    private Stereotype sysmlViewpoint;

    public static final String DEFAULT_ID = "InstanceViewpoint";

    public InstanceViewpointAction(Element e) {
        super(DEFAULT_ID, "Instance Viewpoint", null, null);
        viewpoint = e;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUILog gl = Application.getInstance().getGUILog();
        sysmlView = Utils.getViewClassStereotype();
        sysmlViewpoint = Utils.getViewpointStereotype();
        ef = Project.getProject(viewpoint).getElementsFactory();
        if (sysmlView == null) {
            gl.log("The view stereotype cannot be found");
            return;
        }
        if (sysmlViewpoint == null) {
            gl.log("The sysml viewpoint stereotype cannot be found");
            return;
        }
        List<java.lang.Class<?>> types = new ArrayList<java.lang.Class<?>>();
        types.add(Package.class);
        Element pack = (Element) Utils.getUserSelection(types, "Choose where the instanced views should go");
        if (pack == null || !(pack instanceof Package)) {
            gl.log("you didn't select a package");
            return;
        }
        try {
            SessionManager.getInstance().createSession("instance viewpoint");
            instance(pack, (Class) viewpoint, ((Class) viewpoint).getName());
            SessionManager.getInstance().closeSession();
        } catch (Exception ex) {
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
        StereotypesHelper.addStereotype(view, sysmlView);
        Generalization conforms = ef.createGeneralizationInstance();
        StereotypesHelper.addStereotype(conforms, Utils.getSysML14ConformsStereotype());
        ModelHelper.setClientElement(conforms, view);
        ModelHelper.setSupplierElement(conforms, vp);
        conforms.setOwner(view);
        for (Property p : vp.getOwnedAttribute()) {
            Type type = p.getType();
            if (type instanceof Class && StereotypesHelper.hasStereotypeOrDerived(type, sysmlViewpoint)) {
                Class child = null;
                if (p.getName().equals("")) {
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

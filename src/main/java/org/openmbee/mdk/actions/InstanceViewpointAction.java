package org.openmbee.mdk.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.sysml.util.SysMLProfile;
import com.nomagic.ui.ResizableIconImageIcon;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdtemplates.TemplateBinding;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdtemplates.TemplateParameterSubstitution;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;
import org.openmbee.mdk.SysMLExtensions;
import org.openmbee.mdk.api.incubating.MDKConstants;
import org.openmbee.mdk.docgen.DocGenUtils;
import org.openmbee.mdk.options.MDKProjectOptions;
import org.openmbee.mdk.util.Utils;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            instanceViewpoint(pack, (Class) viewpoint, ((Class) viewpoint).getName());
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

    private Class instanceViewpoint(Element owner, Class vp, String name) {
        

        HashMap<String,String> paramMap = new HashMap<>();

        if (vp.getTemplateBinding().size() > 0) {
            TemplateBinding binding = vp.getTemplateBinding().stream().collect(Collectors.toList()).get(0);

            getTemplateMap(paramMap, binding);
            Class templateVP = (Class) binding.getTarget().stream().collect(Collectors.toList()).get(0).getOwner();

            return instance(owner, templateVP, templateVP.getName(), paramMap);
        } else {
            return instance(owner, vp, name, paramMap);
        }
    }

    private Class instance(Element owner, Class vp, String name, Map<String, String> paramMap) {
        

        Class view = ef.createClassInstance();
        view.setOwner(owner);
        
        if (paramMap.size() > 0) {
            StringBuilder nameSB = new StringBuilder(name);
            paramMap.forEach((param, replace) -> {
                replaceAll(nameSB, "[[" + param + "]]", replace);
            });
            name = nameSB.toString();
        }
        view.setName(name);
        if (MDKProjectOptions.instanceVPDoc(project)) {
            
            String vp_doc = ModelHelper.getComment(vp);
            if (vp_doc.isEmpty()) {
                vp_doc = "<i>" + name + " placeholder documentation</i>";
            } else {
                if (paramMap.size() > 0) {
                    StringBuilder docSB = new StringBuilder(vp_doc);
                    paramMap.forEach((param, replace) -> {
                        replaceAll(docSB, "[[" + param + "]]", replace);
                    });
                    vp_doc = docSB.toString();
                }
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
                    child = instance(view, (Class) type, type.getName(), paramMap);
                }
                else {
                    child = instance(view, (Class) type, p.getName(), paramMap);
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

    public static void getTemplateMap(Map<String,String> paramMap, TemplateBinding binding) {
       //Get Paramter Subs
        binding.getParameterSubstitution().stream().forEach((parameter) -> {
            String name = ((NamedElement) parameter.getFormal().getParameteredElement()).getName();
            if (!paramMap.containsKey(name)) {
                Element element = parameter.getActual();
                
                if (!(name.endsWith(MDKConstants.ID_KEY_SUFFIX) || name.endsWith(MDKConstants.NAME_KEY_SUFFIX))) {
                    paramMap.put(name + MDKConstants.ID_KEY_SUFFIX, DocGenUtils.fixId(element));
                    paramMap.put(name + MDKConstants.NAME_KEY_SUFFIX, DocGenUtils.fixString(element));
                    paramMap.put(name, DocGenUtils.fixString(element));
                } else if (name.endsWith(MDKConstants.ID_KEY_SUFFIX)) {
                    paramMap.put(name, DocGenUtils.fixId(element));
                } else {
                    paramMap.put(name, DocGenUtils.fixString(element));
                }
            }
            
        });
        //Get Defaults
        binding.getSignature().getParameter().forEach((parameter) -> {
            String name = ((NamedElement) parameter.getParameteredElement()).getName();
            if (!paramMap.containsKey(name)) {
                Element element = parameter;
                
                if (!(name.endsWith(MDKConstants.ID_KEY_SUFFIX) || name.endsWith(MDKConstants.NAME_KEY_SUFFIX))) {
                    paramMap.put(name + MDKConstants.ID_KEY_SUFFIX, DocGenUtils.fixId(element));
                    paramMap.put(name + MDKConstants.NAME_KEY_SUFFIX, DocGenUtils.fixString(element));
                    paramMap.put(name, DocGenUtils.fixString(element));
                } else if (name.endsWith(MDKConstants.ID_KEY_SUFFIX)) {
                    paramMap.put(name, DocGenUtils.fixId(element));
                } else {
                    paramMap.put(name, DocGenUtils.fixString(element));
                }
                
            }
        });
    }
    public static void replaceAll(StringBuilder sb, String from, String to) {
        int index = sb.indexOf(from);
        while (index != -1) {
            sb.replace(index, index + from.length(), to);
            index += to.length();
            index = sb.indexOf(from, index);
        }
    }
}

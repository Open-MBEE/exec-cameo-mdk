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
package gov.nasa.jpl.mbee.actions.vieweditor;

import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.viewedit.ViewEditUtils;
import gov.nasa.jpl.mbee.web.sync.CommentChangeListener;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ViolationSeverity;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.magicdraw.uml2.util.UML2ModelUtil;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;

/**
 * import changed elements in views
 * 
 * @author dlam
 * 
 */
public class ImportViewAction extends MDAction {
    private static final long serialVersionUID = 1L;
    private Element            doc;
    public static final String actionid = "ImportView";

    public ImportViewAction(Element e) {
    	//JJS--MDEV-567 fix: changed 'Import' to 'Accept'
    	//
        super(actionid, "Accept View (Overwrite)", null, null);
        doc = e;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        doImportView(doc, true, false, null);
    }

    public static void doImportView(Element e, boolean willchange, Boolean recursive, String curl) {
        Boolean recurse = recursive;
        String url = curl;
        GUILog gl = Application.getInstance().getGUILog();
    	//JJS--MDEV-567 fix: changed 'Import' to 'Accept'
    	//
        ValidationSuite vs = new ValidationSuite("Accept changes");
        ViolationSeverity sev = ViolationSeverity.WARNING;
        if (willchange)
            sev = ViolationSeverity.INFO;
        ValidationRule nameChange = new ValidationRule("Name change", "Element name is changed", sev);
        ValidationRule notLocked = new ValidationRule("Not Editable", "Element is not locked",
                ViolationSeverity.ERROR);
        ValidationRule docChange = new ValidationRule("Doc change", "Element documentation is changed", sev);
        ValidationRule valueChange = new ValidationRule("Value change",
                "Property's default value is changed", sev);
        vs.addValidationRule(nameChange);
        vs.addValidationRule(notLocked);
        vs.addValidationRule(docChange);
        vs.addValidationRule(valueChange);
        try {
            if (recurse == null)
                recurse = false;
            if (url == null)
                url = ViewEditUtils.getUrl();
            if (url == null)
                return;
            String geturl = url + "/rest/views/" + e.getID();
            if (recurse)
                geturl += "?recurse=true";
            GetMethod gm = new GetMethod(geturl);
            // PostMethod pm = new PostMethod(url + "/rest/views/committed");
            try {
                if (willchange)
                    gl.log("*** Starting import view ***");
                else
                    gl.log("*** Starting consistency check ***");
                HttpClient client = new HttpClient();
                ViewEditUtils.setCredentials(client, geturl);
                int code = client.executeMethod(gm);
                if (ViewEditUtils.showErrorMessage(code))
                    return;
                String json = gm.getResponseBodyAsString();
                // gl.log(json);
                if (json.equals("[]")) {
                    gl.log("[INFO] There are nothing to import.");
                    return;
                }
                if (json.equals("NotFound")) {
                    gl.log("[ERROR] This view is not on the view editor yet, export this view first");
                    return;
                }
                change(json, willchange, nameChange, notLocked, docChange, valueChange);
                /*
                 * if (willchange) {
                 * //gl.log("[INFO] Notifying view editor of imported changes."
                 * ); pm.setRequestHeader("Content-Type", "text/json");
                 * pm.setRequestEntity
                 * (JsonRequestEntity.create(changed.toJSONString()));
                 * client.executeMethod(pm); String res =
                 * pm.getResponseBodyAsString(); if (res.equals("ok")) {}
                 * //gl.log
                 * ("[INFO] Imported changes are marked as imported on view editor."
                 * ); else gl.log(res); }
                 */
            } finally {
                gm.releaseConnection();
                // pm.releaseConnection();
            }
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            gl.log(sw.toString()); // stack trace as a string
            ex.printStackTrace();
        }
        Collection<ValidationSuite> cvs = new ArrayList<ValidationSuite>();
        cvs.add(vs);
        if (willchange)
        	//JJS--MDEV-567 fix: changed 'Import' to 'Accept'
        	//
            Utils.displayValidationWindow(cvs, "Accept/Synchronize View Results");
        else
            Utils.displayValidationWindow(cvs, "Validate Sync Results");
    }

    @SuppressWarnings("unchecked")
    public static JSONArray change(String json, boolean willchange, ValidationRule nameChange,
            ValidationRule notLocked, ValidationRule docChange, ValidationRule valueChange) {
        JSONArray res = new JSONArray();
        JSONArray changed = (JSONArray)JSONValue.parse(json);
        GUILog gl = Application.getInstance().getGUILog();
        Project p = Application.getInstance().getProject();
        CommentChangeListener.disable(); // if the documentation of element is
                                         // changed, we don't want the listener
                                         // to add any stereotypes to the
                                         // comment representing documentation
        try {
            SessionManager.getInstance().createSession("import view");

            int modified = 0;
            int cannot = 0;
            int notfound = 0;
            for (Object change: changed) {
                if (change instanceof JSONObject) {
                    JSONObject element = (JSONObject)change;
                    String mdid = (String)element.get("mdid");
                    String name = (String)element.get("name");
                    String doc = (String)element.get("documentation");
                    String dochtml = Utils.addHtmlWrapper(doc);
                    BaseElement mdelement = p.getElementByID(mdid);
                    if (mdelement == null) {
                        gl.log("[ERROR] The element with id " + mdid
                                + " is no longer in the model - the web version is probably out of date.");
                        notfound++;
                        continue;
                    }
                    /*
                     * if (!mdelement.isEditable()) { if (mdelement instanceof
                     * NamedElement)
                     * gl.log(((NamedElement)mdelement).getQualifiedName() +
                     * " is not editable!"); else gl.log("the element with id "
                     * + mdid + " is not editable!"); cannot++; continue; }
                     */
                    if (mdelement instanceof Element) {
                        boolean echanged = false;
                        boolean editable = true;
                        if (mdelement instanceof NamedElement) {
                            String curName = ((NamedElement)mdelement).getName();
                            if (!curName.equals(name)) {
                                if (!mdelement.isEditable()) {
                                    notLocked.addViolation((Element)mdelement,
                                            "Name can't be changed - not editable");
                                    editable = false;
                                } else {
                                    nameChange.addViolation((Element)mdelement, "Name change to " + name);
                                    if (willchange)
                                        ((NamedElement)mdelement).setName(name);
                                    echanged = true;
                                }
                            }
                        }
                        if (mdelement instanceof Property && element.containsKey("dvalue")) {
                            if (!UML2ModelUtil.getDefault(((Property)mdelement)).equals(element.get("dvalue"))) {
                                if (!mdelement.isEditable()) {
                                    notLocked.addViolation((Element)mdelement,
                                            "Default value can't be changed - not editable");
                                    editable = false;
                                } else {
                                    valueChange.addViolation((Element)mdelement, "Default value change to "
                                            + (String)element.get("dvalue"));
                                    if (willchange)
                                        Utils.setPropertyValue((Property)mdelement,
                                                (String)element.get("dvalue"));
                                    echanged = true;
                                }
                            }
                        }
                        if (mdelement instanceof Slot && element.containsKey("dvalue")) {
                            if (!Utils.slotValueToString((Slot)mdelement).equals(
                                    element.get("dvalue"))) {
                                if (!mdelement.isEditable()) {
                                    notLocked.addViolation((Element)mdelement,
                                            "Slot value can't be changed - not editable");
                                    editable = false;
                                } else {
                                    valueChange.addViolation((Element)mdelement, "Slot value change to "
                                            + (String)element.get("dvalue"));
                                    if (willchange)
                                        Utils.setSlotValue((Slot)mdelement, (String)element.get("dvalue"));
                                    echanged = true;
                                }
                            }
                        }
                        String curdoc = ModelHelper.getComment((Element)mdelement);
                        if (mdelement instanceof Comment)
                            curdoc = ((Comment)mdelement).getBody();
                        String curdocwithouthtml = Utils.stripHtmlWrapper(curdoc);
                        if (!curdocwithouthtml.equals(doc)) {
                            if (!mdelement.isEditable()) {
                                notLocked.addViolation((Element)mdelement,
                                        "Doc or comment body can't be changed - not editalbe");
                                editable = false;
                            } else {
                                if (mdelement instanceof NamedElement)
                                    docChange.addViolation((Element)mdelement, "Doc change");
                                else
                                    docChange.addViolation((Element)mdelement, "Comment body change");
                                if (dochtml.equals(""))
                                    dochtml = " "; // this is to prevent a
                                                   // documentation getting set
                                                   // to "", then md will set an
                                                   // annotated comment to its
                                                   // documentation
                                if (willchange) {
                                    if (mdelement instanceof Comment)
                                        ((Comment)mdelement).setBody(dochtml);
                                    else
                                        ModelHelper.setComment((Element)mdelement, dochtml);
                                }
                                echanged = true;
                            }
                        }

                        if (echanged) {
                            modified++;
                            res.add(mdelement.getID());
                        }
                        if (!editable)
                            cannot++;
                    }
                }
            }
            if (willchange)
                gl.log("[INFO] Elements modified: " + modified);
            else
                gl.log("[INFO] Elements different: " + modified);
            gl.log("[INFO] Elements different but not editable: " + cannot);
            gl.log("[INFO] Elements not found: " + notfound);

            SessionManager.getInstance().closeSession();
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            gl.log(sw.toString()); // stack trace as a string
            ex.printStackTrace();
            SessionManager.getInstance().cancelSession();
            res.clear();
        }
        CommentChangeListener.enable();
        return res;
    }
}

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
package gov.nasa.jpl.mbee.ems.validation.actions;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.sync.OutputQueue;
import gov.nasa.jpl.mbee.ems.sync.Request;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

public class ExportName extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    private static final long serialVersionUID = 1L;
    private NamedElement element;
    
    public ExportName(NamedElement e) {
    	//JJS--MDEV-567 fix: changed 'Export' to 'Commit'
    	//
        super("ExportName", "Commit name", null, null);
        this.element = e;
    }
    
    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(Collection<Annotation> annos) {
        Set<String> duplicatedNames = new HashSet<String>();
        JSONObject send = new JSONObject();
        JSONArray infos = new JSONArray();
        Set<Element> set = new HashSet<Element>();
        for (Annotation anno: annos) {
            Element e = (Element)anno.getTarget();
            if (duplicateName(e)) {
                String qname = ((NamedElement)e).getQualifiedName();
                if (!duplicatedNames.contains(qname)) {
                    Application.getInstance().getGUILog().log("[WARNING] " + qname + " has the same qualified name as another element.");
                    duplicatedNames.add(qname);
                }
                //return;
            }
            if (e instanceof NamedElement) {
                set.add(e);
                infos.add(ExportUtility.fillName(e, null));
            }
        }
        if (!ExportUtility.okToExport(set))
            return;
        send.put("elements", infos);
        send.put("source", "magicdraw");
        String url = ExportUtility.getPostElementsUrl();
        if (url == null) {
            return;
        }
        Application.getInstance().getGUILog().log("[INFO] Request is added to queue.");
        OutputQueue.getInstance().offer(new Request(url, send.toJSONString(), annos.size()));
        /*if (ExportUtility.send(url, send.toJSONString()) != null) {
            this.removeViolationsAndUpdateWindow(annos);
        }*/
    }

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!ExportUtility.okToExport(element))
            return;
        if (duplicateName(element)) {
            Application.getInstance().getGUILog().log("[WARNING] " + ((NamedElement)element).getQualifiedName() + " has the same qualified name as another element.");
            //return;
        }
        JSONArray elements = new JSONArray();
        JSONObject send = new JSONObject();
        elements.add(ExportUtility.fillName(element, null));
        send.put("elements", elements);
        send.put("source", "magicdraw");
        String url = ExportUtility.getPostElementsUrl();
        if (url == null) {
            return;
        }
        Application.getInstance().getGUILog().log("[INFO] Request is added to queue.");
        OutputQueue.getInstance().offer(new Request(url, send.toJSONString()));
        /*if (ExportUtility.send(url, send.toJSONString()) != null) {
            this.removeViolationsAndUpdateWindow(annos);
        }*/
    }
    
    private boolean duplicateName(Element e) {
        if (e instanceof NamedElement) {
            if (e.getOwner() != null) {
                for (Element c: e.getOwner().getOwnedElement()) {
                    if (c instanceof NamedElement && c != e && c.getHumanName().equals(e.getHumanName()) && !((NamedElement)c).getName().equals(""))
                        return true;
                }
            }
        }
        return false;
    }
}

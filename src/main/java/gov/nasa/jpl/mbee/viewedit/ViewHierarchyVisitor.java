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
package gov.nasa.jpl.mbee.viewedit;

import gov.nasa.jpl.mbee.model.AbstractModelVisitor;
import gov.nasa.jpl.mbee.model.Document;
import gov.nasa.jpl.mbee.model.Section;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;


public class ViewHierarchyVisitor extends AbstractModelVisitor {

    private JSONObject       result = new JSONObject();
    private Map<Element, List<Element>> resultElements = new HashMap<Element, List<Element>>();
    private Stack<JSONArray> curChildren = new Stack<JSONArray>();
    private Stack<List<Element>> curChildrenElements = new Stack<List<Element>>();
    private JSONArray        nosections = new JSONArray();

    @SuppressWarnings("unchecked")
    public JSONObject getResult() {
        JSONObject res = new JSONObject();
        res.put("views", result);
        res.put("noSections", nosections);
        return res;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(Document doc) {
        if (doc.getDgElement() != null) {
            curChildren.push(new JSONArray());
            curChildrenElements.push(new ArrayList<Element>());
        }
        visitChildren(doc);
        if (doc.getDgElement() != null) {
            result.put(doc.getDgElement().getID(), curChildren.pop());
            resultElements.put(doc.getDgElement(), curChildrenElements.pop());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(Section sec) {
        if (sec.isView()) {
            if (sec.isNoSection())
                nosections.add(sec.getDgElement().getID());
            if (!curChildren.isEmpty()) {
                curChildren.peek().add(sec.getDgElement().getID());
                curChildrenElements.peek().add(sec.getDgElement());
            }
            curChildren.push(new JSONArray());
            curChildrenElements.push(new ArrayList<Element>());
        }
        visitChildren(sec);
        if (sec.isView()) {
            result.put(sec.getDgElement().getID(), curChildren.pop());
            resultElements.put(sec.getDgElement(), curChildrenElements.pop());
        }
    }
    
    public JSONObject getView2View() {
        return result;
    }
    
    public Map<Element, List<Element>> getView2ViewElements() {
        return resultElements;
    }
    
    public JSONArray getNosections() {
        return nosections;
    }
}

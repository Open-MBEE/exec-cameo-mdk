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
package gov.nasa.jpl.mbee.ems;

import gov.nasa.jpl.mbee.lib.Utils;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Extension;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.ProfileApplication;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class ModelExporter {

    //private JSONObject elementHierarchy = new JSONObject();
    private JSONObject elements = new JSONObject();
    //private JSONArray roots = new JSONArray();
    
    private List<Element> starts;
    private int depth;
    private boolean packageOnly;
    
    private Stereotype view = Utils.getViewStereotype();
    private Stereotype viewpoint = Utils.getViewpointStereotype();
    
    public ModelExporter(Project prj, int depth, boolean pkgOnly) {
        this.depth = depth;
        starts = new ArrayList<Element>();
        for (Package pkg: prj.getModel().getNestedPackage()) {
            if (ProjectUtilities.isElementInAttachedProject(pkg))
                continue;//check for module??
            starts.add(pkg);
        }
        packageOnly = pkgOnly;
        
    }
    
    public ModelExporter(List<Element> roots, int depth, boolean pkgOnly) {
        this.depth = depth;
        this.starts = roots;
        packageOnly = pkgOnly;
    }
    
    public int getNumberOfElements() {
        return elements.size();
    }
    
    @SuppressWarnings("unchecked")
    public JSONObject getResult() {
        for (Element e: starts) {
            addToElements(e, 1);
                //roots.add(e.getID());
        }
        JSONObject result = new JSONObject();
        //result.put("roots", roots);
        JSONArray elementss = new JSONArray();
        elementss.addAll(elements.values());
        result.put("elements", elementss);
        //result.put("elementHierarchy", elementHierarchy);
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private boolean addToElements(Element e, int curdepth) {
        if (elements.containsKey(e.getID()))
            return true;
        if (e instanceof ValueSpecification || !(e instanceof Package) && packageOnly || e instanceof Extension || e instanceof ProfileApplication)
            return false;
        if (ProjectUtilities.isElementInAttachedProject(e))
            return false;
        if (e instanceof Comment && ExportUtility.isElementDocumentation((Comment)e)) 
            return false;
        JSONObject elementInfo = new JSONObject();
        ExportUtility.fillElement(e, elementInfo, view, viewpoint);
        elements.put(e.getID(), elementInfo);
        
        if ((depth != 0 && curdepth > depth) || curdepth == 0)
            return true;
        //JSONArray children = new JSONArray();
        for (Element c: e.getOwnedElement()) {
            addToElements(c, curdepth+1);
                //children.add(c.getID());
        }
        //elementHierarchy.put(e.getID(), children);
        return true;
    }
}

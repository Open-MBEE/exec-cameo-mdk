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
package gov.nasa.jpl.mbee.mdk.ems;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.ci.persistence.IProject;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Extension;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.ProfileApplication;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.mdk.MDKPlugin;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;

import java.util.HashSet;
import java.util.Set;

//@donbot update json simple to jackson
@Deprecated
public class ModelExporter {
//    private JSONObject elementHierarchy = new JSONObject();
    private ObjectNode elements;
    private ObjectNode emfElements;

//    private JSONObject elementsj = new JSONObject();
//    private JSONObject emfElementsj = new JSONObject();
//    private JSONArray roots = new JSONArray();

    private Set<Element> starts;
    private int depth;
    private boolean packageOnly;
    private IProject parentProject;

    private Stereotype view = Utils.getViewStereotype();
    private Stereotype viewpoint = Utils.getViewpointStereotype();

    public ModelExporter(Project project, int depth, boolean pkgOnly) {
        this.depth = depth;
        this.starts = new HashSet<Element>();
        for (Package pkg : project.getModel().getNestedPackage()) {
            if (ProjectUtilities.isElementInAttachedProject(pkg)) {
                continue;// check for module??
            }
            this.starts.add(pkg);
        }
        this.packageOnly = pkgOnly;
        this.parentProject = project.getPrimaryProject();

    }

    public ModelExporter(Set<Element> roots, int depth, boolean pkgOnly, IProject prj) {
        this.depth = depth;
        this.starts = roots;
        this.packageOnly = pkgOnly;
        this.parentProject = prj;
    }

    public int getNumberOfElements() {
        return elements.size();
    }

    public ObjectNode getResult() {
        ObjectNode result = JacksonUtils.getObjectMapper().createObjectNode();
        for (Element e: starts) {
            addToElements(e, 1);
        }
        ArrayNode elements = result.putArray("elements");
        //TODO finish this later if we don't migrate off it


        return result;
    }

    public JSONObject getResultj() {
        for (Element e : starts) {
            addToElements(e, 1);
            // roots.add(e.getID());
        }
        JSONObject result = new JSONObject();
        // result.put("roots", roots);
        JSONArray elementss = new JSONArray();
        elementss.addAll(elements.values());
        result.put("elements", elementss);
        result.put("source", "magicdraw");
        result.put("mmsVersion", MDKPlugin.VERSION);
        // result.put("elementHierarchy", elementHierarchy);
        return resultj;
    }

    //TODO @donbot refactor?
    @SuppressWarnings("unchecked")
    private boolean addToElements(Element e, int curdepth) {
        if (elements.get(e.getID()) != null) {
            return true;
        }
        if (// e instanceof ValueSpecification ||
                (packageOnly && !(e instanceof Package)) || e instanceof Extension || e instanceof ProfileApplication)
        // if (!(e instanceof Package) && packageOnly)
        {
            return false;
        }
        if (ProjectUtilities.isAttachedProjectRoot(e) && !starts.contains(e))
        // if (ProjectUtilities.isElementInAttachedProject(e))
        {
            return false;
        }
        if (!ExportUtility.shouldAdd(e)) {
            return false;
        }

        ObjectNode elementInfo = Converters.getElementToJsonConverter().apply(e, Project.getProject(e));
        elements.set(e.getID(), elementInfo);

        if (starts.contains(e) && ProjectUtilities.isAttachedProjectRoot(e)) {
            elementInfo.put("ownerId", parentProject.getProjectID());
        }

        // if (e instanceof Property || e instanceof Slot)
        // elements.putAll(ExportUtility.getReferencedElements(e));
        if ((depth != 0 && curdepth > depth) || curdepth == 0) {
            return true;
        }
        // JSONArray children = new JSONArray();
        for (Element c : e.getOwnedElement()) {
            addToElements(c, curdepth + 1);
            // children.add(c.getID());
        }
        // elementHierarchy.put(e.getID(), children);
        return true;
    }
}

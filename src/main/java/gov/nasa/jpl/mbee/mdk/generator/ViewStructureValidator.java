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
package gov.nasa.jpl.mbee.mdk.generator;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.AggregationKindEnum;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.mdk.lib.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * compliment of the InstanceViewpointAction this checks a view hierarchy model
 * against the viewpoints it conforms to
 *
 * @author dlam
 */
public class ViewStructureValidator {

    private Stereotype sysmlViewpoint = Utils.getViewpointStereotype();
    private Stereotype sysmlConforms = Utils.getSysML14ConformsStereotype();
    private Stereotype sysmlView = Utils.getViewStereotype();
    private GUILog gl = Application.getInstance().getGUILog();
    private List<Element> missing = new ArrayList<Element>();

    public void validate(Element curView) {
        List<Element> childrenViews = getChildrenViews(curView);
        Element curViewpoint = getConforms(curView);
        if (curViewpoint == null) {
            return;
        }
        List<Element> childrenViewpoints = getChildrenViewpoints(curViewpoint);
        Set<Element> childrenConforms = getChildrenConforms(childrenViews);
        for (Element vp : childrenViewpoints) {
            if (!childrenConforms.contains(vp)) {
                missing.add(vp);
            }
        }
        for (Element cv : childrenViews) {
            validate(cv);
        }
    }

    public void printErrors() {
        for (Element e : missing) {
            gl.log("Viewpoint " + ((NamedElement) e).getQualifiedName() + " is missing from view structure.");
        }
    }

    private List<Element> getChildrenViewpoints(Element v) {
        return Utils.filterElementsByStereotype(
                Utils.collectAssociatedElements(v, 1, AggregationKindEnum.COMPOSITE), sysmlViewpoint, true,
                true);
    }

    private List<Element> getChildrenViews(Element e) {
        return Utils.filterElementsByStereotype(
                Utils.collectAssociatedElements(e, 1, AggregationKindEnum.COMPOSITE), sysmlView, true, true);
    }

    private Element getConforms(Element v) {
        List<Element> conforms = Utils.collectDirectedRelatedElementsByRelationshipStereotype(v,
                sysmlConforms, 1, false, 1);
        if (conforms.isEmpty()) {
            return null;
        }
        return conforms.get(0);
    }

    private Set<Element> getChildrenConforms(List<Element> views) {
        Set<Element> res = new HashSet<Element>();
        for (Element e : views) {
            res.addAll(Utils.collectDirectedRelatedElementsByRelationshipStereotype(e,
                    sysmlConforms, 1, false, 1));
        }
        return res;
    }
}

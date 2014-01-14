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
package gov.nasa.jpl.mbee.generator;

import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mbee.lib.Utils;

import java.util.List;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

/**
 * numbers view dependencies (first, next) to the section number their target
 * has
 * 
 * @author dlam
 * 
 */
public class ViewDependencyNumberer {
    /**
     * Clear out any existing dependency numbering names on the first, next,
     * nosection relations. Recursively goes through each view to remove
     * relation names.
     * 
     * @param view
     *            View to clear strings for
     */
    public static void clearAll(Element view) {
        Dependency first = findStereotypedRelationship(view, DocGen3Profile.firstStereotype);
        if (first != null) {
            if (first.isEditable())
                first.setName("");
            clearAll(ModelHelper.getSupplierElement(first));
        }
        Dependency next = findStereotypedRelationship(view, DocGen3Profile.nextStereotype);
        if (next != null) {
            if (next.isEditable())
                next.setName("");
            clearAll(ModelHelper.getSupplierElement(next));
        }
        Dependency nosection = findStereotypedRelationship(view, DocGen3Profile.nosectionStereotype);
        if (nosection != null) {
            if (nosection.isEditable())
                nosection.setName("");
            clearAll(ModelHelper.getSupplierElement(nosection));
        }
    }

    /**
     * Applies the numbering to the relationships so the outline numbering can
     * be seen. Recursively goes through each view to add dependency numbers.
     * 
     * @param view
     *            View to add dependency numbering strings for
     * @param prefix
     *            List of the tree hierarchy that includes the numbering
     *            prefixes
     */
    public static void start(Element view, List<Integer> prefix) {
        Dependency first = findStereotypedRelationship(view, DocGen3Profile.firstStereotype);
        if (first != null) {
            prefix.add(1);
            String s = Utils.join(prefix, ".");
            if (first.isEditable())
                first.setName(s);
            start(ModelHelper.getSupplierElement(first), prefix);
            prefix.remove(prefix.size() - 1);
        }
        Dependency next = findStereotypedRelationship(view, DocGen3Profile.nextStereotype);
        if (next != null) {
            if (!prefix.isEmpty()) {
                prefix.set(prefix.size() - 1, prefix.get(prefix.size() - 1) + 1);
                String s = Utils.join(prefix, ".");
                if (next.isEditable())
                    next.setName(s);
                start(ModelHelper.getSupplierElement(next), prefix);
            }
        }
    }

    public static Dependency findStereotypedRelationship(Element e, String s) {
        Stereotype stereotype = StereotypesHelper.getStereotype(Application.getInstance().getProject(), s);
        for (DirectedRelationship dr: e.get_directedRelationshipOfSource()) {
            if (dr instanceof Dependency && StereotypesHelper.hasStereotype(dr, stereotype)) // REVIEW
                                                                                             // --
                                                                                             // hasStereotypeOrDerived()?
                return (Dependency)dr;
        }
        return null;
    }

}

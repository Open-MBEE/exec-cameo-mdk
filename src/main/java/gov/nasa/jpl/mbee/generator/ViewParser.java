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

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mbee.model.Container;
import gov.nasa.jpl.mbee.model.Document;
import gov.nasa.jpl.mbee.model.Section;

/**
 * This parses the view structure constructed using First, Next, NoSection
 * dependencies
 *
 * @author dlam
 */
public class ViewParser {

    private DocumentGenerator dg;
    private boolean singleView;
    private boolean recurse;
    private Document doc;
    private Element start;

    public ViewParser(DocumentGenerator dg, boolean singleView, boolean recurse, Document doc, Element start) {
        this.dg = dg;
        this.singleView = singleView;
        this.recurse = recurse;
        this.doc = doc;
        this.start = start;
    }

    public Section parse() {
        Stereotype documentView = StereotypesHelper.getStereotype(Application.getInstance().getProject(),
                DocGen3Profile.documentViewStereotype, "Document Profile");
        if (StereotypesHelper.hasStereotypeOrDerived(start, documentView)) {
            doc.setDgElement(start); // only set the DgElement if this is
            // actually a document view, this affects
            // processing down the line for various
            // things (like docweb visitors)
            Element first = GeneratorUtils.findStereotypedRelationship(start, DocGen3Profile.firstStereotype);
            if (first != null) {
                return parseView(first, doc, true, false);
            }
        }
        else {// starting from regular view, not document
            return parseView(start, doc, true, true);
        }
        return null;
    }

    /**
     * @param view       current view
     * @param parent     parent view
     * @param section    should current view be a section
     * @param singleView parse only one view
     * @param recurse    if singleView is true, but want all children view from top
     *                   view
     * @param top        is current view the top view
     */
    private Section parseView(Element view, Container parent, boolean section, boolean top) {
        Section viewSection = dg.parseView(view);

        parent.addElement(viewSection);
        if (!section && parent instanceof Section) // parent can be Document, in
        // which case this view must
        // be a section
        {
            viewSection.setNoSection(true);
        }

        if (!singleView) { // does everything from here including nexts
            Element content = GeneratorUtils.findStereotypedRelationship(view,
                    DocGen3Profile.nosectionStereotype);
            if (content != null && section) // current view is a section,
            // nosection children should go
            // under it
            {
                parseView(content, viewSection, false, false);
            }
            if (content != null && !section) // current view is not a section,
            // further nosection children
            // should be siblings
            {
                parseView(content, parent, false, false);
            }
            Element first = GeneratorUtils.findStereotypedRelationship(view, DocGen3Profile.firstStereotype);
            if (first != null) {
                parseView(first, viewSection, true, false);
            }
            Element next = GeneratorUtils.findStereotypedRelationship(view, DocGen3Profile.nextStereotype);
            if (next != null) {
                parseView(next, parent, true, false);
            }

        }
        else if (recurse) {// single view, but recursive (gets everything
            // underneath view including view, but not nexts
            // from the top view
            Element content = GeneratorUtils.findStereotypedRelationship(view,
                    DocGen3Profile.nosectionStereotype);
            if (content != null && section) {
                parseView(content, viewSection, false, false);
            }
            if (content != null && !section) {
                parseView(content, parent, false, false);
            }
            Element first = GeneratorUtils.findStereotypedRelationship(view, DocGen3Profile.firstStereotype);
            if (first != null) {
                parseView(first, viewSection, true, false);
            }
            if (!top) {
                Element next = GeneratorUtils
                        .findStereotypedRelationship(view, DocGen3Profile.nextStereotype);
                if (next != null) {
                    parseView(next, parent, true, false);
                }
            }
        }
        return viewSection;
    }
}

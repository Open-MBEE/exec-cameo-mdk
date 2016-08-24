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

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import gov.nasa.jpl.mbee.lib.Pair;
import gov.nasa.jpl.mbee.model.Container;
import gov.nasa.jpl.mbee.model.DocGenElement;
import gov.nasa.jpl.mbee.model.Document;
import gov.nasa.jpl.mbee.model.Section;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

/**
 * This parses a view structure and Product spec that uses associations for
 * specifying children views and their order to form a view hierarchy
 * 
 * @author dlam
 * 
 */
public class ProductViewParser {

    private Class             start;
    private DocumentGenerator dg;
    private Document          doc;
    private boolean           recurse;
    private boolean           singleView;
    private Set<Element>      noSections;
    private Set<Element>      excludeViews;
    private Stereotype        productS;
    private boolean           product;

    @SuppressWarnings("unchecked")
    public ProductViewParser(DocumentGenerator dg, boolean singleView, boolean recurse, Document doc,
            Element start) {
        this.dg = dg;
        this.singleView = singleView;
        this.recurse = recurse;
        this.doc = doc;
        if (start instanceof Class)
            this.start = (Class)start;
        this.productS = dg.getProductStereotype();
        if (productS != null && StereotypesHelper.hasStereotypeOrDerived(start, productS)) {
            product = true;
            doc.setProduct(true);
            doc.setDgElement(start);
            List<Element> noSections = StereotypesHelper.getStereotypePropertyValue(start,
                    productS, "noSections");
            List<Element> excludeViews = StereotypesHelper.getStereotypePropertyValue(start,
                    productS, "excludeViews");
            this.noSections = new HashSet<Element>(noSections);
            this.excludeViews = new HashSet<Element>(excludeViews);
        } else {
            noSections = new HashSet<Element>();
            excludeViews = new HashSet<Element>();
        }
    }

    public void parse() {
        if (start == null)
            return;
        Container top = doc;
        if (!product) {
            Section chapter1 = dg.parseView(start);
            top = chapter1;
            doc.addElement(chapter1);
        } else {
            Section s = dg.parseView(start);
            for (DocGenElement e: s.getChildren())
                top.addElement(e);
        }
        if (!singleView || recurse)
            handleViewChildren(start, top);
    }

    /**
     * 
     * @param view
     * @param parent
     *            parent view the current view should go under
     * @param nosection
     *            whether current view is a nosection
     */
    private void parseView(Class view, Container parent, boolean nosection, boolean recurse) {
        Section viewSection = dg.parseView(view);
        viewSection.setNoSection(nosection);
        parent.addElement(viewSection);
        if (recurse) {
            handleViewChildren(view, viewSection);
        }
    }

    private void handleViewChildren(Class view, Container viewSection) {
        List<Pair<Class, AggregationKind>> childSections = new ArrayList<>(),
                childNoSections = new ArrayList<>();
        for (Property prop: view.getOwnedAttribute()) {
            if (!(prop.getType() instanceof Class))
                continue;
            Class type = (Class)prop.getType();
            if (type == null || !StereotypesHelper.hasStereotypeOrDerived(type, dg.getView())
                    || excludeViews.contains(prop) || excludeViews.contains(type))
                continue;
            if (noSections.contains(prop) || noSections.contains(type)) {
                childNoSections.add(new Pair<>(type, prop.getAggregation()));
            } else {
                childSections.add(new Pair<>(type, prop.getAggregation()));
            }
        }
        for (Pair<Class, AggregationKind> pair : childNoSections) {
            parseView(pair.getFirst(), viewSection, true, !AggregationKindEnum.NONE.equals(pair.getSecond()));
        }
        for (Pair<Class, AggregationKind> pair: childSections) {
            parseView(pair.getFirst(), viewSection, false, !AggregationKindEnum.NONE.equals(pair.getSecond()));
        }
    }
}

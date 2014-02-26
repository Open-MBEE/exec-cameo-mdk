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
package gov.nasa.jpl.mbee.model;

import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.From;

import java.util.ArrayList;
import java.util.List;

import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

public class Paragraph extends Query {

    private String         text;
    private List<Property> stereotypeProperties;
    private From           fromProperty;

    public Paragraph(String t) {
        text = t;
    }

    public Paragraph() {
    }

    public void setText(String t) {
        text = t;
    }

    public String getText() {
        return text;
    }

    public void setStereotypeProperties(List<Property> p) {
        stereotypeProperties = p;
    }

    public List<Property> getStereotypeProperties() {
        return stereotypeProperties;
    }

    public void setFrom(From f) {
        fromProperty = f;
    }

    public From getFrom() {
        return fromProperty;
    }

    @Override
    public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
        List<DocumentElement> res = new ArrayList<DocumentElement>();
        if (getIgnore())
            return res;
        if (getText() != null) {
            if (forViewEditor || !getText().trim().equals(""))
                res.add(new DBParagraph(getText(), getDgElement(), getFrom()));
        } else if (getTargets() != null) {
            List<Object> targets = isSortElementsByName() ? Utils.sortByName(getTargets()) : getTargets();
            for (Object o: targets) {
                if ( !( o instanceof Element ) ) continue;
                Element e = (Element)o;
                if ( getStereotypeProperties() != null && !getStereotypeProperties().isEmpty() ) {
                    for (Property p: getStereotypeProperties()) {
                        res.addAll(Common.getReferenceAsDocumentElements(Reference.getPropertyReference(e, p)));
                        // List<Object> ob =
                        // Utils.getStereotypePropertyValues(e, p, true);
                        // for (Object o: ob) {
                        // if (o instanceof String)
                        // parent.addElement(new DBParagraph((String)o));
                        // }
                    }
                } else
                    res.add(new DBParagraph(ModelHelper.getComment(e), e, From.DOCUMENTATION));
            }
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize() {
        String body = (String)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.paragraphStereotype,
                "body", null);
        setText(body);
        setStereotypeProperties((List<Property>)GeneratorUtils
                .getListProperty(dgElement, DocGen3Profile.stereotypePropertyChoosable,
                        "stereotypeProperties", new ArrayList<Property>()));
    }

}

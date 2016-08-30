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

import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBHasContent;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTableEntry;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.stereotypes.EditableChoosable;

/**
 * Common is a collection of utility functions for creating DocumentElements
 * from model Elements.
 */
public class Common {

    public static void addReferenceToDBHasContent(Reference ref, DBHasContent parent, Query query) {
        parent.addElements(getReferenceAsDocumentElements(ref, query));
    }

    public static void addReferenceToDBHasContent(Reference ref, DBHasContent parent, Boolean editable) {
        parent.addElements(getReferenceAsDocumentElements(ref, editable));
    }

    public static List<DocumentElement> getReferenceAsDocumentElements(Reference ref, Query query) {
        Boolean editable = null;
        Object o;
        if (query != null && query.getDgElement() != null && (o = StereotypesHelper.getStereotypePropertyFirst(query.getDgElement(), DocGen3Profile.editableChoosable, "editable")) instanceof Boolean) {
            editable = (Boolean) o;
        }
        return getReferenceAsDocumentElements(ref, editable);
    }

    public static List<DocumentElement> getReferenceAsDocumentElements(Reference ref, Boolean editable) {
        List<DocumentElement> res = new ArrayList<DocumentElement>();
        if (ref.result == null)
            return res;
        if (!ref.isResultEditable()) {
            if (ref.result instanceof Collection) {
                for (Object r: (Collection<?>)ref.result) {
                    DocumentElement documentElement = new DBParagraph(r);
                    initEditable(documentElement, editable);
                    res.add(documentElement);
                }
            } else {
                DocumentElement documentElement = new DBParagraph(ref.result);
                initEditable(documentElement, editable);
                res.add(documentElement);
            }
        } else {
            //if (ref.result instanceof Collection && !((Collection<?>)ref.result).isEmpty()) {
            //    res.add(new DBParagraph(((Collection<?>)ref.result).iterator().next(), ref.element, ref.from));
            //} else {
            DocumentElement documentElement = new DBParagraph(ref.result, ref.element, ref.from);
            initEditable(documentElement, editable);
            res.add(documentElement);
            //}
        }
        return res;
    }

    /**
     * This set is used to prevent infinite recursion while traversing nested
     * collections of model elements.
     */
    public static Set<Object> seen = Collections.synchronizedSet(new HashSet<Object>());

    public static DBTableEntry getStereotypePropertyEntry(Element e, Property p, Query query) {
        DBTableEntry res = new DBTableEntry();
        addReferenceToDBHasContent(Reference.getPropertyReference(e, p), res, query);
        return res;
    }

    /**
     * This assumes no context for what object is or how it can be editable,
     * should not be used except in old queries like GenericTable
     * 
     * @param o
     * @return
     */
    public static DBTableEntry getTableEntryFromObject(Object o) {
        DBTableEntry res = new DBTableEntry();
        if (o instanceof Collection) {
            for (Object r: (Collection<?>)o) {
                res.addElement(new DBParagraph(r));
            }
        } else
            res.addElement(new DBParagraph(o));
        return res;
    }

    private static void initEditable(DocumentElement documentElement, Boolean editable) {
        if (documentElement instanceof EditableChoosable && editable != null) {
            ((EditableChoosable) documentElement).setEditable(editable);
        }
    }
}

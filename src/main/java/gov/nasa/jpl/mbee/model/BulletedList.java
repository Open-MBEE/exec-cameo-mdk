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
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBHasContent;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBList;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBListItem;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBText;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.From;

import java.util.ArrayList;
import java.util.List;

import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

public class BulletedList extends Table {
    private boolean orderedList;
    private boolean showTargets;
    private boolean showStereotypePropertyNames;

    public BulletedList() {
        orderedList = false;
        showTargets = true;
        showStereotypePropertyNames = true;
        setSortElementsByName(false);
    }

    public void setOrderedList(boolean b) {
        orderedList = b;
    }

    public void setShowTargets(boolean b) {
        showTargets = b;
    }

    public void setShowStereotypePropertyNames(boolean b) {
        showStereotypePropertyNames = b;
    }

    public boolean isOrderedList() {
        return orderedList;
    }

    public boolean isShowTargets() {
        return showTargets;
    }

    public boolean isShowStereotypePropertyNames() {
        return showStereotypePropertyNames;
    }

    public void addStereotypeProperties(DBHasContent parent, Element e, Property p) {
        Common.addReferenceToDBHasContent(Reference.getPropertyReference(e, p), parent, this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize() {
        Boolean showTargets = (Boolean)GeneratorUtils.getObjectProperty(dgElement,
                DocGen3Profile.bulletedListStereotype, "showTargets", false);
        Boolean showSPN = (Boolean)GeneratorUtils.getObjectProperty(dgElement,
                DocGen3Profile.bulletedListStereotype, "showStereotypePropertyNames", false);
        Boolean ordered = (Boolean)GeneratorUtils.getObjectProperty(dgElement,
                DocGen3Profile.bulletedListStereotype, "orderedList", false);
        setShowTargets(showTargets);
        setShowStereotypePropertyNames(showSPN);
        setOrderedList(ordered);
        setIncludeDoc((Boolean)GeneratorUtils.getObjectProperty(dgElement,
                DocGen3Profile.documentationChoosable, "includeDoc", false));
        setStereotypeProperties((List<Property>)GeneratorUtils
                .getListProperty(dgElement, DocGen3Profile.stereotypePropertyChoosable,
                        "stereotypeProperties", new ArrayList<Property>()));
    }

    @Override
    public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
        List<DocumentElement> res = new ArrayList<DocumentElement>();
        if (ignore)
            return res;
        if (targets != null && !targets.isEmpty()) {
            DBList l = new DBList();
            res.add(l);
            l.setOrdered(isOrderedList());
            List<Object> targets = isSortElementsByName() ? Utils.sortByName(getTargets()) : getTargets();
            if (isShowTargets() || isIncludeDoc()) {

                for (Object o: targets) {
                    DBListItem li = new DBListItem();
                    l.addElement(li);
                    if ( !( o instanceof Element ) ) {
                        li.addElement( new DBText( o ) );
                        continue;
                    }
                    Element e = (Element)o;
                    if (isShowTargets() && e instanceof NamedElement) {
                        li.addElement(new DBParagraph(((NamedElement)e).getName(), (NamedElement)e, From.NAME));
                    }
                    if (isIncludeDoc() && (e instanceof Element) && (!ModelHelper.getComment((Element)e).equals("") || forViewEditor)) {
                        li.addElement(new DBParagraph(ModelHelper.getComment((Element)e), (Element)e, From.DOCUMENTATION));
                    }
                    if (getStereotypeProperties() != null && !getStereotypeProperties().isEmpty()) {
                        if (isShowStereotypePropertyNames()) {
                            DBList l2 = new DBList();
                            l2.setOrdered(isOrderedList());
                            li.addElement(l2);
                            for (Property p: getStereotypeProperties()) {
                                DBListItem li2 = new DBListItem();
                                l2.addElement(li2);
                                li2.addElement(new DBParagraph(p.getName(), p, From.NAME));
                                DBList l3 = new DBList();
                                l3.setOrdered(isOrderedList());
                                li2.addElement(l3);
                                addStereotypeProperties(l3, e, p);
                            }
                        } else {
                            DBList l2 = new DBList();
                            l2.setOrdered(isOrderedList());
                            li.addElement(l2);
                            for (Property p: getStereotypeProperties()) {
                                addStereotypeProperties(l2, e, p);
                            }
                        }
                    }
                }
            } else {
                for (Object o: targets) {
                    if ( !( o instanceof Element ) ) {
                        continue;
                    }
                    Element e = (Element)o;
                    if (getStereotypeProperties() != null && !getStereotypeProperties().isEmpty()) {
                        if (isShowStereotypePropertyNames()) {
                            for (Property p: getStereotypeProperties()) {
                                DBListItem li2 = new DBListItem();
                                li2.addElement(new DBParagraph(p.getName(), p, From.NAME));
                                l.addElement(li2);
                                DBList l3 = new DBList();
                                li2.addElement(l3);
                                l3.setOrdered(isOrderedList());
                                addStereotypeProperties(l3, e, p);
                            }
                        } else {
                            for (Property p: getStereotypeProperties()) {
                                addStereotypeProperties(l, e, p);
                            }
                        }
                    }
                }
            }
        }
        return res;
    }
}

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
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBColSpec;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTable;

import java.util.ArrayList;
import java.util.List;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

public abstract class Table extends Query {

    protected boolean        includeDoc;
    protected List<Property> stereotypeProperties;
    protected List<String>   captions;
    protected boolean        showCaptions;
    protected String         style;
    protected List<String>   colwidths;
    protected boolean       transpose;
    
    public void setIncludeDoc(boolean d) {
        includeDoc = d;
    }

    public boolean isIncludeDoc() {
        return includeDoc;
    }

    public void setStereotypeProperties(List<Property> p) {
        stereotypeProperties = p;
    }

    public void setCaptions(List<String> c) {
        captions = c;
    }

    public void setShowCaptions(boolean b) {
        showCaptions = b;
    }

    public Boolean isShowCaptions() {
        return showCaptions;
    }

    public List<String> getCaptions() {
        return captions;
    }

    public List<Property> getStereotypeProperties() {
        return stereotypeProperties;
    }

    public void setStyle(String s) {
        style = s;
    }

    public String getStyle() {
        return style;
    }

    public void setColwidths(List<String> colwidths) {
        this.colwidths = colwidths;
    }

    public List<String> getColwidths() {
        return colwidths;
    }

    public boolean isTranspose() {
        return transpose;
    }

    public void setTranspose(boolean transpose) {
        this.transpose = transpose;
    }

    protected void setTableThings(DBTable dbTable) {
        String title = "";
        if (getTitles() != null && getTitles().size() > 0)
            title = getTitles().get(0);
        title = getTitlePrefix() + title + getTitleSuffix();
        dbTable.setTitle(title);

        if (getCaptions() != null && getCaptions().size() > 0 && isShowCaptions())
            dbTable.setCaption(getCaptions().get(0));

        dbTable.setStyle(getStyle());

        List<DBColSpec> cslist = new ArrayList<DBColSpec>();
        if (getColwidths() != null && !getColwidths().isEmpty()) {
            int i = 1;
            for (String s: getColwidths()) {
                DBColSpec cs = new DBColSpec(i);
                cs.setColwidth(s);
                cslist.add(cs);
                i++;
            }
            dbTable.setColspecs(cslist);
        }
        dbTable.setTranspose(transpose);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize() {
        setCaptions((List<String>)GeneratorUtils.getListProperty(dgElement, DocGen3Profile.hasCaptions,
                "captions", new ArrayList<String>()));
        setShowCaptions((Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.hasCaptions,
                "showCaptions", true));
        setStereotypeProperties((List<Property>)GeneratorUtils
                .getListProperty(dgElement, DocGen3Profile.stereotypePropertyChoosable,
                        "stereotypeProperties", new ArrayList<Property>()));
        setIncludeDoc((Boolean)GeneratorUtils.getObjectProperty(dgElement,
                DocGen3Profile.documentationChoosable, "includeDoc", false));
        setStyle((String)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.tableStereotype, "style",
                null));
        setColwidths((List<String>)GeneratorUtils.getListProperty(dgElement, DocGen3Profile.tableStereotype,
                "colwidths", new ArrayList<String>()));
        setTranspose((Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.tableStereotype,
                "transpose", false));
    }
}

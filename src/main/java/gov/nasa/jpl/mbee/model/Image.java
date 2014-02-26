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
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBImage;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.From;

import java.util.ArrayList;
import java.util.List;

import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class Image extends Query {

    protected List<String> captions;
    protected boolean      showCaptions;
    protected boolean      doNotShow;

    public void setCaptions(List<String> c) {
        captions = c;
    }

    public void setShowCaptions(boolean b) {
        showCaptions = b;
    }

    public void setDoNotShow(boolean b) {
        doNotShow = b;
    }

    public Boolean getDoNotShow() {
        return doNotShow;
    }

    public Boolean getShowCaptions() {
        return showCaptions;
    }

    public List<String> getCaptions() {
        return captions;
    }

    @Override
    public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
        List<DocumentElement> res = new ArrayList<DocumentElement>();
        if (getIgnore())
            return res;
        if (getTargets() != null) {
            List<Object> targets = isSortElementsByName() ? Utils.sortByName(getTargets()) : getTargets();
            for (int i = 0; i < targets.size(); i++) {
                Object o = targets.get(i);
                if (o instanceof Diagram) {
                    Diagram diagram = (Diagram)o;
                    DBImage im = new DBImage();
                    im.setDiagram(diagram);
                    im.setDoNotShow(getDoNotShow());
                    String title = "";
                    if (getTitles() != null && getTitles().size() > i)
                        title = getTitles().get(i);
                    else
                        title = diagram.getName();
                    if (getTitlePrefix() != null)
                        title = getTitlePrefix() + title;
                    if (getTitleSuffix() != null)
                        title = title + getTitleSuffix();
                    im.setTitle(title);
                    if (getCaptions() != null && getCaptions().size() > i && getShowCaptions())
                        im.setCaption(getCaptions().get(i));
                    im.setId(diagram.getID());
                    res.add(im);

                    String doc = ModelHelper.getComment(diagram);
                    if (doc != null && (forViewEditor || (!doc.trim().equals("") && !getDoNotShow()))) {
                        if ((Boolean)GeneratorUtils.getObjectProperty(diagram, DocGen3Profile.editableChoosable, "editable", true))
                            res.add(new DBParagraph(doc, diagram, From.DOCUMENTATION));
                        else
                            res.add(new DBParagraph(doc));
                    }

                }
            }
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize() {
        // TODO Auto-generated method stub
        Boolean doNotShow = (Boolean)GeneratorUtils.getObjectProperty(dgElement,
                DocGen3Profile.imageStereotype, "doNotShow", false);
        setCaptions((List<String>)GeneratorUtils.getListProperty(dgElement, DocGen3Profile.hasCaptions,
                "captions", new ArrayList<String>()));
        setShowCaptions((Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.hasCaptions,
                "showCaptions", true));
        setDoNotShow(doNotShow);
    }

}

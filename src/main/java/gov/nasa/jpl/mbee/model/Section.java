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

import java.lang.reflect.Field;
import java.util.List;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * this should really be called View now
 * 
 * @author dlam
 * 
 */
public class Section extends Container {
    private boolean isAppendix;
    private boolean isChapter;
    private String  id;
    private boolean isView;
    private boolean isNoSection;

    private Element viewpoint;
    private List<Element> exposes;
    
    public Section() {
        isAppendix = false;
        isChapter = false;
    }

    public void isAppendix(boolean a) {
        isAppendix = a;
    }

    public boolean isAppendix() {
        return isAppendix;
    }

    public void isChapter(boolean c) {
        isChapter = c;
    }

    public boolean isChapter() {
        return isChapter;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setView(boolean b) {
        this.isView = b;
    }

    public boolean isView() {
        return this.isView;
    }

    public void setNoSection(boolean b) {
        this.isNoSection = b;
    }

    public boolean isNoSection() {
        return this.isNoSection;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public void accept(IModelVisitor v) {
        v.visit(this);
    }
    
    @Override
    public String toStringStart() {
        return super.toStringStart() + ",id=" + id;
    }

    public Element getViewpoint() {
        return viewpoint;
    }

    public void setViewpoint(Element viewpoint) {
        this.viewpoint = viewpoint;
    }

    public List<Element> getExposes() {
        return exposes;
    }

    public void setExposes(List<Element> exposes) {
        this.exposes = exposes;
    }

}

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
package gov.nasa.jpl.mbee.mdk.docgen.docbook;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.model.DocGenElement;

/**
 * @author dlam
 */
public abstract class DocumentElement implements IDocumentElement {
    protected String id;
    protected String title;
    protected Element from;        // this is for view editor syncing,
    // indicates what md element is the source
    // of this document element's content, if
    // applicable
    protected From fromProperty; // this is for view editor purpose (where
    // the document fragment comes from -
    // element's name/documentation, etc)

    protected DocGenElement dgElement; //the DocGenElement that generated this

    public DocumentElement() {
        title = "";
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    @Override
    public Element getFrom() {
        return from;
    }

    @Override
    public void setFrom(Element e) {
        this.from = e;
    }

    @Override
    public void setFromProperty(From f) {
        this.fromProperty = f;
    }

    @Override
    public From getFromProperty() {
        return fromProperty;
    }

    public DocGenElement getDgElement() {
        return dgElement;
    }

    public void setDgElement(DocGenElement dgElement) {
        this.dgElement = dgElement;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        sb.append("id=" + id + ", ");
        sb.append("title=" + title + ", ");
        sb.append("from=" + (from == null ? "null" : from.getHumanName()) + ", ");
        sb.append("fromProperty=" + fromProperty);
        sb.append(")");
        return sb.toString();
    }

}

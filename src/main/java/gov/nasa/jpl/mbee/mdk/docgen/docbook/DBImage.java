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

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;

/**
 * Print an image with optional caption. If you're using this directly, you will
 * need to add the documentation as the caption if applicable, and set the
 * title.
 *
 * @author dlam
 */
public class DBImage extends DocumentElement {

    private Diagram image;
    private String caption;
    private boolean gennew;
    private boolean doNotShow;

    public DBImage(Diagram d) {
        image = d;
        gennew = false;
    }

    public DBImage() {
        gennew = false;
    }

    public void setDiagram(Diagram d) {
        image = d;
    }

    public void setCaption(String cap) {
        caption = cap;
    }

    public void setGennew(boolean b) {
        gennew = b;
    }

    public Diagram getImage() {
        return image;
    }

    public void setImage(Diagram image) {
        this.image = image;
    }

    public String getCaption() {
        return caption;
    }

    public boolean isGennew() {
        return gennew;
    }

    public boolean isDoNotShow() {
        return doNotShow;
    }

    public void setDoNotShow(boolean b) {
        doNotShow = b;
    }

    @Override
    public void accept(IDBVisitor v) {
        v.visit(this);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        int pos = sb.lastIndexOf(")");
        sb.insert(pos, ", " + getImage());
        return sb.toString();
    }

}

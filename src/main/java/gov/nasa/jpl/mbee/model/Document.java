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

import gov.nasa.jpl.mbee.lib.MoreToString;
import gov.nasa.jpl.mbee.model.docmeta.DocumentMeta;

import java.lang.reflect.Field;
import java.util.List;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;

public class Document extends Container {

    private DocumentMeta metadata;
    
    private String       header;
    private String       footer;
    private String       subheader;
    private String       subfooter;

    private String       RemoveBlankPages;
    private boolean      UseDefaultStylesheet;

    private boolean      chunkFirstSections;
    private int          chunkSectionDepth;
    private int          tocSectionDepth;

    private boolean      genNewImage;

    private boolean      product;

    public boolean isProduct() {
        return product;
    }

    public void setProduct(boolean product) {
        this.product = product;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getFooter() {
        return footer;
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }

    public String getSubheader() {
        return subheader;
    }

    public void setSubheader(String subheader) {
        this.subheader = subheader;
    }

    public String getSubfooter() {
        return subfooter;
    }

    public void setSubfooter(String subfooter) {
        this.subfooter = subfooter;
    }

    public boolean getChunkFirstSections() {
        return chunkFirstSections;
    }

    public void setChunkFirstSections(boolean chunkFirstSections) {
        this.chunkFirstSections = chunkFirstSections;
    }

    public int getChunkSectionDepth() {
        return chunkSectionDepth;
    }

    public void setChunkSectionDepth(int chunkSectionDepth) {
        this.chunkSectionDepth = chunkSectionDepth;
    }

    public int getTocSectionDepth() {
        return tocSectionDepth;
    }

    public void setTocSectionDepth(int tocSectionDepth) {
        this.tocSectionDepth = tocSectionDepth;
    }

    public boolean getUseDefaultStylesheet() {
        return UseDefaultStylesheet;
    }

    public Boolean getRemoveBlankPages() {
        if (RemoveBlankPages == "1")
            return true;
        else
            return false;
    }

    
    public void setRemoveBlankPages(String s) {
        RemoveBlankPages = s;
    }

    public void setUseDefaultStylesheet(boolean s) {
        UseDefaultStylesheet = s;
    }

    public Document() {
        chunkFirstSections = false;
        chunkSectionDepth = 20;
        tocSectionDepth = 20;
    }

    public boolean getGenNewImage() {
        return genNewImage;
    }

    public void setGenNewImage(boolean n) {
        genNewImage = n;
    }

    public DocumentMeta getMetadata() {
        return metadata;
    }

    public void setMetadata(DocumentMeta metadata) {
        this.metadata = metadata;
    }

    @Override
    public void accept(IModelVisitor v) {
        v.visit(this);
    }
    
    @Override
    public String toStringStart() {
        StringBuffer sb = new StringBuffer();
        sb.append( super.toStringStart() );
        for ( Field f : getClass().getFields() ) {
            if ( f.getDeclaringClass().equals( getClass().getSuperclass() ) ) {
                continue;
            }
            try {
                sb.append( "," + f.getName() + "=" + f.get( this ) );
            } catch ( IllegalArgumentException e ) {
            } catch ( IllegalAccessException e ) {
            }
        }
//        sb.append( super.toStringEnd() );
        return sb.toString();
    }

}

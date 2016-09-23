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

/**
 * A Section or Appendix. If you want to make a new section, instance this and
 * add content to it. You can also set it to skip or output a string if the
 * content ends up being empty.<br/>
 * You don't have to set the chapter flags, those will be done automatically
 * based on document structure once the whole document is assembled.<br/>
 * <p>
 * This should really be a representation of a view instead since now view info
 * is carried all the way through the generation process (even if they're not
 * sections)
 *
 * @author dlam
 */
public class DBSection extends DBHasContent {

    private boolean isAppendix;
    private boolean isChapter;
    private boolean skipIfEmpty;
    private String stringIfEmpty;
    private boolean isView;
    private boolean isNoSection;

    public DBSection() {
        isAppendix = false;
        isChapter = false;
        skipIfEmpty = false;
        stringIfEmpty = "";
    }

    public void isChapter(boolean c) {
        isChapter = c;
    }

    public boolean isChapter() {
        return isChapter;
    }

    public boolean isSkipIfEmpty() {
        return skipIfEmpty;
    }

    public String getStringIfEmpty() {
        return stringIfEmpty;
    }

    public void setAppendix(boolean isAppendix) {
        this.isAppendix = isAppendix;
    }

    public void setChapter(boolean isChapter) {
        this.isChapter = isChapter;
    }

    public void setSkipIfEmpty(boolean s) {
        skipIfEmpty = s;
    }

    public void setStringIfEmpty(String s) {
        stringIfEmpty = s;
    }

    public void isAppendix(boolean a) {
        isAppendix = a;
    }

    public boolean isAppendix() {
        return isAppendix;
    }

    public void isNoSection(boolean a) {
        isNoSection = a;
    }

    public boolean isNoSection() {
        return isNoSection;
    }

    public boolean isView() {
        return isView;
    }

    public void setView(boolean b) {
        this.isView = b;
    }

    @Override
    public void accept(IDBVisitor v) {
        v.visit(this);
    }
}

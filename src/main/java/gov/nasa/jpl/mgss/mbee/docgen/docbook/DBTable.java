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
package gov.nasa.jpl.mgss.mbee.docgen.docbook;

import java.util.List;

/**
 * A docbook table<br/>
 * Body and headers are essentially 2d matrix (headers can be multi-line)<br/>
 * Cells in body and header must be instances of other DocumentElement. If you
 * only want some simple text in a cell, use DBText.<br/>
 * If you want complex elements in a cell, use DBTableEntry which allows a list
 * of DocumentElements as its content. DBTableEntry needs to be used if you want
 * your cell to span.<br/>
 * If a cell is not a DBTableEntry, it'll automatically add the docbook entry
 * tags, so use DBTableEntry only if you have a complex cell.<br/>
 * <br/>
 * A list of DBColSpecs can also be set, these need to be filled in if any cell
 * has a span.<br/>
 * The rows of the header and body 2d matrix doesn't all have to have the same
 * amount of "columns".<br/>
 * If you have cells that span, the spanning info will take care of the
 * alignments, do not put in null or empty things if the cell's spanned by
 * something else.<br/>
 * Ex. if your first header row has a cell that spans two rows, your second
 * header row would have 1 less cell than the first, because one cell is
 * "covered" by the spanning cell in the first row.
 * 
 * @author dlam
 * 
 */
public class DBTable extends DocumentElement {

    private List<List<DocumentElement>> body;
    private String                      caption;
    private String                      style;
    private List<List<DocumentElement>> headers;
    private List<DBColSpec>             colspecs;
    private int                         cols;

    public List<List<DocumentElement>> getBody() {
        return body;
    }

    public String getCaption() {
        return caption;
    }

    public List<List<DocumentElement>> getHeaders() {
        return headers;
    }

    public List<DBColSpec> getColspecs() {
        return colspecs;
    }

    public int getCols() {
        return cols;
    }

    public String getStyle() {
        return style;
    }

    /**
     * This must be set
     * 
     * @param body
     */
    public void setBody(List<List<DocumentElement>> body) {
        this.body = body;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    /**
     * This must be set
     * 
     * @param headers
     */
    public void setHeaders(List<List<DocumentElement>> headers) {
        this.headers = headers;
    }

    public void setColspecs(List<DBColSpec> colspecs) {
        this.colspecs = colspecs;
    }

    /**
     * this must be set (the cols is the max number of cols in your table)
     * 
     * @param cols
     */
    public void setCols(int cols) {
        this.cols = cols;
    }

    public void setStyle(String style) {
        this.style = style;
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
        sb.insert(pos, ", " + getBody());
        return sb.toString();
    }

}

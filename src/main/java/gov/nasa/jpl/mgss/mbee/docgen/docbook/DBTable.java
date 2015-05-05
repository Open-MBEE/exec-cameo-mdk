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

import java.util.ArrayList;
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
    private boolean                     transpose;

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

    public boolean isTranspose() {
        return transpose;
    }

    public void setTranspose(boolean transpose) {
        this.transpose = transpose;
    }

    @Override
    public void accept(IDBVisitor v) {
        v.visit(this);
    }
    
    /*
     * transpose the table 
     */
    public void transpose() {
        //this whole thing looks complicated because of transposing colspans and rowspans
        //the new table wouldn't have a header since the new header would be part of the body
        removeAllNulls(); //every cell should be a document element, this is to remove any inconsistent user set nulls
        addNulls(); //go through the current table and add in nulls according to colspans and rowspans 
                    //so the table is truly m x n, every cell should be either null or a document element
 
        //do the transpose, if i is index of the col and j is index of the row in the old table, 
        //the new table's rows would be i and cols would be j
        List<List<DocumentElement>> newbody = new ArrayList<List<DocumentElement>>();
        for (int i = headers.get(0).size()-1; i >= 0; i--) {
            List<DocumentElement> newrow = new ArrayList<DocumentElement>();
            newbody.add(newrow);
            for (int j = 0; j < headers.size(); j++) {
                DocumentElement oldcell = headers.get(j).get(i);
                if (oldcell instanceof DBTableEntry) {
                    DBTableEntry oldcelll = ((DBTableEntry)oldcell);
                    String namest = oldcelll.getNamest();
                    String nameend = oldcelll.getNameend();
                    int morerows = oldcelll.getMorerows();
                    oldcelll.setMorerows(0);
                    oldcelll.setNamest(null);
                    oldcelll.setNameend(null);
                    if (namest != null && nameend != null) {
                        int oldnamest = Integer.parseInt(namest);
                        int oldnameend = Integer.parseInt(nameend);
                        int diff = oldnameend - oldnamest;
                        oldcelll.setMorerows(diff);
                    }
                    if (morerows > 0) {
                        String newnamest = Integer.toString(newrow.size()+1);
                        String newnameend = Integer.toString(newrow.size()+1+morerows);
                        oldcelll.setNamest(newnamest);
                        oldcelll.setNameend(newnameend);
                    }
                    newrow.add(oldcelll);
                } else {
                    newrow.add(oldcell);
                }
            }
        }
        for (int i = body.get(0).size()-1; i >= 0; i--) {
            List<DocumentElement> newrow = newbody.get(newbody.size()-i-1);
            for (int j = 0; j < body.size(); j++) {
                DocumentElement oldcell = body.get(j).get(i);
                if (oldcell instanceof DBTableEntry) {
                    DBTableEntry oldcelll = ((DBTableEntry)oldcell);
                    String namest = oldcelll.getNamest();
                    String nameend = oldcelll.getNameend();
                    int morerows = oldcelll.getMorerows();
                    oldcelll.setMorerows(0);
                    oldcelll.setNamest(null);
                    oldcelll.setNameend(null);
                    if (namest != null && nameend != null) {
                        int oldnamest = Integer.parseInt(namest);
                        int oldnameend = Integer.parseInt(nameend);
                        int diff = oldnameend - oldnamest;
                        oldcelll.setMorerows(diff);
                    }
                    if (morerows > 0) {
                        String newnamest = Integer.toString(newrow.size()+1);
                        String newnameend = Integer.toString(newrow.size()+1+morerows);
                        oldcelll.setNamest(newnamest);
                        oldcelll.setNameend(newnameend);
                    }
                    newrow.add(oldcelll);
                } else {
                    newrow.add(oldcell);
                }
            }
        }
        this.body = newbody;
        this.headers =  null;
        this.cols = newbody.get(0).size();
        List<DBColSpec> newcolspecs = new ArrayList<DBColSpec>();
        for (int i = 1; i <= cols; i++) {
            newcolspecs.add(new DBColSpec(i));
        }
        this.colspecs = newcolspecs;
        
    }
    
    private void removeAllNulls() {
        for (List<DocumentElement> row: headers) {
            while (row.remove(null)) {}
        }
        for (List<DocumentElement> row: body) {
            while (row.remove(null)) {}
        }
    }
    
    private void addNulls() {
        //handle old colspans first for each row
        handleColspan(headers);
        handleColspan(body);
        
        handleRowspan(headers);
        handleRowspan(body);
    }
    
    private void handleColspan(List<List<DocumentElement>> body) {
        for (List<DocumentElement> row: body) {
            List<DocumentElement> copy = new ArrayList<DocumentElement>(row);
            for (int i = copy.size()-1; i >=0; i--) {
                DocumentElement cell = copy.get(i);
                if (cell instanceof DBTableEntry) {
                    String namest = ((DBTableEntry)cell).getNamest();
                    String nameend = ((DBTableEntry)cell).getNameend();
                    if (namest != null && nameend != null) {
                        try {
                            int start = Integer.parseInt(namest);
                            int end = Integer.parseInt(nameend);
                            int morecols = end-start;
                            while (morecols > 0) {
                                row.add(i, null);
                                morecols--;
                            }
                        } catch (Exception e) {
                            
                        }
                    }
                }
            }
        }
    }
    
    private void handleRowspan(List<List<DocumentElement>> body) {
      //int j = 0;
        //need to handle old rowspans bottom up because of stuff
        int i = 0;
        for (int j = body.size()-1; j >=0; j--) { //List<DocumentElement> row: headers) {
            List<DocumentElement> row = body.get(j);
            for (DocumentElement cell: row) {
                if (cell instanceof DBTableEntry && ((DBTableEntry)cell).getMorerows() > 0) {
                    int length = ((DBTableEntry)cell).getMorerows();
                    for (int k = j; k < j+length; k++) {
                        body.get(k+1).add(i, null);
                    }
                }
                i++;
            }
            i = 0;
            //j++;
        }
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

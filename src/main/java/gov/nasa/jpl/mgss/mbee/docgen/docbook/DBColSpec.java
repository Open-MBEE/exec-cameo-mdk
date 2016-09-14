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

/**
 * docbook column spec in a table<br/>
 * assign a name to a column number (this is needed if you have table entries
 * that span columns)<br/>
 * if just given a column number to constructor the col name would be the string
 * of the integer a list of colspecs can be assigned to a DBTable<br/>
 *
 * @author dlam
 */
public class DBColSpec extends DocumentElement {
    private int colnum;
    private String colname;
    private String colwidth;

    public DBColSpec(int num, String name) {
        colnum = num;
        colname = name;
    }

    public DBColSpec(int num) {
        colnum = num;
        colname = Integer.toString(num);
    }

    public DBColSpec() {

    }

    public DBColSpec(int num, String name, String colwidth) {
        colnum = num;
        colname = name;
        this.colwidth = colwidth;
    }

    public void setColnum(int num) {
        colnum = num;
    }

    public void setColname(String name) {
        colname = name;
    }

    public void setColwidth(String colwidth) {
        this.colwidth = colwidth;
    }

    public int getColnum() {
        return colnum;
    }

    public String getColname() {
        return colname;
    }

    public String getColwidth() {
        return colwidth;
    }

    @Override
    public void accept(IDBVisitor v) {
        v.visit(this);
    }

}

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
 * Use this if a cell in your table needs to have multiple things in it or it
 * spans.<br/>
 * The morerows, namest, and nameend attributes correspond to the same things in
 * actual docbook entry tag.<br/>
 * morerows is how many more rows the cell will span downward (if it doesn't,
 * don't set it, 1 means it spans 2 rows)<br/>
 * namest and nameend refer to the names of columns the cell start and end,
 * inclusive, you need to set colspecs in the containing table for this to work.
 *
 * @author dlam
 */
public class DBTableEntry extends DBHasContent {

    private int morerows;
    private String namest;
    private String nameend;

    public void setMorerows(int i) {
        morerows = i;
    }

    public void setNamest(String s) {
        namest = s;
    }

    public void setNameend(String s) {
        nameend = s;
    }

    public int getMorerows() {
        return morerows;
    }

    public String getNamest() {
        return namest;
    }

    public String getNameend() {
        return nameend;
    }

    @Override
    public void accept(IDBVisitor v) {
        v.visit(this);
    }
}

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
package gov.nasa.jpl.mbee.mdk.viewedit;

import gov.nasa.jpl.mbee.mdk.docgen.DocGenUtils;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.*;

public class DBHTMLVisitor extends DBAbstractVisitor {

    private StringBuilder out;

    public DBHTMLVisitor() {
        out = new StringBuilder();
    }

    public String getOut() {
        return out.toString();
    }

    @Override
    public void visit(DBBook book) {
        visitChildren(book);
    }

    @Override
    public void visit(DBColSpec colspec) {
    }

    @Override
    public void visit(DBImage image) {

    }

    @Override
    public void visit(DBList list) {
        if (list.isOrdered()) {
            out.append("<ol>");
        }
        else {
            out.append("<ul>");
        }
        visitChildren(list);
        if (list.isOrdered()) {
            out.append("</ol>");
        }
        else {
            out.append("</ul>");
        }
    }

    @Override
    public void visit(DBListItem listitem) {
        out.append("<li>");
        visitChildren(listitem);
        out.append("</li>");
    }

    @Override
    public void visit(DBParagraph para) {
        out.append(DocGenUtils.addP(DocGenUtils.fixString(para.getText(), false)));
    }

    @Override
    public void visit(DBText text) {
        out.append(DocGenUtils.addP(DocGenUtils.fixString(text.getText(), false)));
    }

    @Override
    public void visit(DBSection section) {
        visitChildren(section);
    }

    @Override
    public void visit(DBSimpleList simplelist) {
        out.append("<ul>");
        for (Object o : simplelist.getContent()) {
            out.append("<li>");
            out.append(DocGenUtils.addP(DocGenUtils.fixString(o, false)));
            out.append("</li>");
        }
        out.append("</ul>");
    }

    @Override
    public void visit(DBTable table) {
    }

    @Override
    public void visit(DBTableEntry tableentry) {
    }

    @Override
    public void visit(DBTomSawyerDiagram tomSawyerDiagram) {

    }
}

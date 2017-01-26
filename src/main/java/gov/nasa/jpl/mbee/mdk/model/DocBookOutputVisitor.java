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
package gov.nasa.jpl.mbee.mdk.model;

import gov.nasa.jpl.mbee.mdk.docgen.docbook.*;

import java.util.List;
import java.util.Stack;

public class DocBookOutputVisitor extends AbstractModelVisitor {

    private boolean forViewEditor;
    private Stack<DBHasContent> parent;
    private String outputDir;

    public DocBookOutputVisitor(boolean forViewEditor) {
        this.forViewEditor = forViewEditor;
        this.parent = new Stack<DBHasContent>();
    }

    public DocBookOutputVisitor(boolean forViewEditor, String outputDir) {
        this.forViewEditor = forViewEditor;
        this.parent = new Stack<DBHasContent>();
        this.outputDir = outputDir;
    }

    public Stack<DBHasContent> getParent() {
        return parent;
    }

    public DBBook getBook() {
        if (!parent.isEmpty() && parent.get(0) instanceof DBBook) {
            return (DBBook) parent.get(0);
        }
        return null;
    }

    @Override
    public void visit(Query q) {
        List<DocumentElement> results = q.visit(forViewEditor, outputDir);
        for (DocumentElement result : results) {
            result.setDgElement(q);
        }
        parent.peek().addElements(results);
    }

    @Override
    public void visit(Document doc) {
        DBBook book = new DBBook();
        book.setDgElement(doc);
        book.setTitle(doc.getTitle());
        if (doc.getTitle() == null || doc.getTitle().isEmpty()) {
            book.setTitle("Default Title");
        }
        book.setFrom(doc.getDgElement());
        book.setRemoveBlankPages(doc.getRemoveBlankPages());
        book.setUseDefaulStylesheet(doc.getUseDefaultStylesheet());
        book.setMetadata(doc.getMetadata());
        parent.push(book);
        visitChildren(doc);
    }

    @Override
    public void visit(Section section) {
        if (section.getIgnore()) {
            return;
        }
        DBSection sec = new DBSection();
        sec.setDgElement(section);
        sec.setFrom(section.getDgElement());
        sec.isAppendix(section.isAppendix());
        sec.isChapter(section.isChapter());
        sec.setView(section.isView());
        sec.isNoSection(section.isNoSection());
        String title = "";
        if (section.getTitle() != null && !section.getTitle().isEmpty()) {
            title = section.getTitle();
        }
        if (section.getTitlePrefix() != null) {
            title = section.getTitlePrefix() + title;
        }
        if (section.getTitleSuffix() != null) {
            title = title + section.getTitleSuffix();
        }
        sec.setTitle(title);
        sec.setStringIfEmpty(section.getStringIfEmpty());
        sec.setSkipIfEmpty(section.getSkipIfEmpty());
        if (section.getId() != null) {
            sec.setId(section.getId());
        }

        parent.push(sec);
        visitChildren(section);
        parent.pop();

        if (section.isNoSection()) {
            for (DocumentElement de : sec.getChildren()) {
                if (de instanceof DBTable) {
                    de.setId(section.getId());
                }
            }
        }
        if (sec.getChildren().isEmpty() && !forViewEditor) {
            if (section.getSkipIfEmpty()) {
                return;
            }
            if (section.getStringIfEmpty() != null) {
                sec.addElement(new DBParagraph(section.getStringIfEmpty()));
            }
            else {
                sec.addElement(new DBParagraph(""));
            }
        }
        parent.peek().addElement(sec);
    }
}

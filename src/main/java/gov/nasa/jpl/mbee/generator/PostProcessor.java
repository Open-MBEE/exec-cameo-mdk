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
package gov.nasa.jpl.mbee.generator;

import gov.nasa.jpl.mbee.model.Container;
import gov.nasa.jpl.mbee.model.DocGenElement;
import gov.nasa.jpl.mbee.model.Document;
import gov.nasa.jpl.mbee.model.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * process document model to set those with option "useSectionNameAsTitle"
 * should probably be redone as a visitor or include in the transformation to DB
 * classes
 * 
 * @author dlam
 * 
 */
public class PostProcessor {

    private Stack<String> titles;

    public PostProcessor() {
        titles = new Stack<String>();
    }

    public void process(Document d) {
        titles.push(d.getTitle());
        processContainer(d);
        titles.pop();
    }

    private void processContainer(Container c) {
        for (DocGenElement de: c.getChildren()) {
            if (de instanceof Container) {
                String title = ((Container)de).getTitle();
                if ((title == null || title.equals("")) && de.getUseContextNameAsTitle()) {
                    ((Container)de).setTitle(titles.peek());
                }
                titles.push(((Container)de).getTitle());
                processContainer((Container)de);
                titles.pop();
            } else if (de instanceof Query) {
                List<String> title = ((Query)de).getTitles();
                if ((title == null || title.isEmpty()) && de.getUseContextNameAsTitle()) {
                    title = new ArrayList<String>();
                    title.add(titles.peek());
                    ((Query)de).setTitles(title);
                }
            }
        }
    }
}

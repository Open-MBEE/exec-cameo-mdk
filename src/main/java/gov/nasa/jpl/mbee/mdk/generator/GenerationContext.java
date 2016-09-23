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
package gov.nasa.jpl.mbee.mdk.generator;

import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;

import java.util.List;
import java.util.Stack;

/**
 * <p>
 * A more OO way to pass around targets, current node, and whatever other
 * variables might be added to track generation in DocumentGenerator, which has
 * been refactored to be more modular.
 * </p>
 *
 * @author bcompane
 */
public class GenerationContext {

    private Stack<List<Object>> targets;
    private ActivityNode current;
    private DocumentValidator validator;
    private GUILog log;

    public GenerationContext(Stack<List<Object>> t, ActivityNode a, DocumentValidator dv, GUILog l) {
        targets = t;
        current = a;
        validator = dv;
        log = l;
    }

    public GenerationContext(Stack<List<Object>> t, ActivityNode a, GUILog l) {
        targets = t;
        current = a;
        validator = null;
        log = l;
    }

    public void pushTargets(List<Object> t) {
        targets.push(t);
    }

    public List<Object> peekTargets() {
        return targets.peek();
    }

    public List<Object> popTargets() {
        return targets.pop();
    }

    public boolean targetsEmpty() {
        return targets.isEmpty();
    }

    public Stack<List<Object>> getTargets() {
        return targets;
    }

    public void setCurrentNode(ActivityNode a) {
        current = a;
    }

    public ActivityNode getCurrentNode() {
        return current;
    }

    public DocumentValidator getValidator() {
        return validator;
    }

    public void setValidator(DocumentValidator validator) {
        this.validator = validator;
    }

    public void log(String msg) {
        log.log(msg);
    }
}

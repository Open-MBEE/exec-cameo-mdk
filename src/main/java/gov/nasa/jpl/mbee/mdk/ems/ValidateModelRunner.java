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
package gov.nasa.jpl.mbee.mdk.ems;

import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.ems.validation.ModelValidator;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationSuite;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.Map;

public class ValidateModelRunner implements RunnableWithProgress {

    private Collection<Element> start;
    private Map<String, JSONObject> keyedElements;
    private ValidationSuite suite;
    private boolean recurse;
    private int depth;

    public ValidateModelRunner(Collection<Element> start, boolean recurse, int depth) {
        this.start = start;
        this.recurse = recurse;
        this.depth = depth;
    }

    public ValidateModelRunner(Collection<Element> start) {
        this(start, true, 0);
    }

    public Map<String, JSONObject> getKeyed() {
        return keyedElements;
    }

    public ValidationSuite getSuite() {
        return suite;
    }

    @Override
    public void run(ProgressStatus arg0) {
        ModelValidator validator = new ModelValidator(start, null, true, null, false, recurse, depth);
        if (validator.checkProject(arg0)) {
            try {
                validator.validate(true, arg0);
                if (!arg0.isCancel()) {
                    validator.showWindow();
                }
                suite = validator.getSuite();
                keyedElements = validator.getKeyed();
            } catch (ServerException ex) {
                Utils.guilog("[ERROR] Validate model cannot be completed because of server error.");
            }
        }
        else {
            suite = validator.getSuite();
            validator.showWindow();
        }
    }

}

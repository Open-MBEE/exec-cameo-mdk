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
package gov.nasa.jpl.mbee.stylesaver;

import java.util.List;

import org.json.simple.JSONObject;

import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;

/**
 * This class contains a run method for the load operation. Updates progress bar
 * dynamically.
 * 
 * @author Benjamin Inada, JPL/Caltech
 */
public class RunnableLoaderWithProgress implements RunnableWithProgress {
    private List<PresentationElement> list;
    private JSONObject                style;
    private boolean                   success;

    /**
     * @param list
     *            the list of elements to load styles into.
     * @param style
     *            the style string to reference.
     */
    public RunnableLoaderWithProgress(List<PresentationElement> list, JSONObject style) {
        this.list = list;
        this.style = style;
    }

    /**
     * Runs the load operation.
     * 
     * @param progressStatus
     *            the status of the operation so far.
     */
    @Override
    public void run(ProgressStatus progressStatus) {
        progressStatus.init("Loading styles...", 0, list.size());
        success = ViewLoader.load(list, style, progressStatus);
    }

    /**
     * Gets the value of the success property.
     * 
     * @return the value of the success property.
     */
    public boolean getSuccess() {
        return success;
    }
}

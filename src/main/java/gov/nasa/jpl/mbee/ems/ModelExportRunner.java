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
package gov.nasa.jpl.mbee.ems;

import gov.nasa.jpl.mbee.ems.sync.queue.OutputQueue;
import gov.nasa.jpl.mbee.ems.sync.Request;
import gov.nasa.jpl.mbee.lib.Utils;

import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONObject;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ModelExportRunner implements RunnableWithProgress {

    private Element start;
    private int depth;
    private boolean packageOnly;
    private String url;
    private Boolean background;
    
    public ModelExportRunner(Element start, int depth, boolean packageOnly, String url, Boolean background) {
        this.start = start;
        this.depth = depth;
        this.packageOnly = packageOnly;
        //if (background)
        	//this.url = url + "?background=true";
        this.url = url;
        this.background = (background == null) ? false : background;
    }
    
    @Override
    public void run(ProgressStatus arg0) {
        ModelExporter me;
        //GUILog gl = Application.getInstance().getGUILog();
        
        if (start == Application.getInstance().getProject().getModel()) {
            me = new ModelExporter(Application.getInstance().getProject(), depth, packageOnly);
        } else {
            Set<Element> root = new HashSet<Element>();
            root.add(start);
            me = new ModelExporter(root, depth, packageOnly, Application.getInstance().getProject().getPrimaryProject());
        }
        JSONObject result = me.getResult();
        String json = result.toJSONString();

        //gl.log(json);
        Utils.guilog("Number of Elements: " + me.getNumberOfElements());
       // gl.log("*** Starting export view comments ***");
        Utils.guilog("[INFO] Request is added to queue.");
        OutputQueue.getInstance().offer(new Request(url, json, me.getNumberOfElements(), "Model Export", background));
        //if (!url.contains("background"))
        if ( !background)
        	Utils.guilog("[INFO] Magicdraw background export running, please wait until it's finished to close Magicdraw. You can continue to use Magicdraw in the meantime. You'll see a message about queued requests finished processing when finished.");
        //ExportUtility.send(url, json, null, false);
    }
}

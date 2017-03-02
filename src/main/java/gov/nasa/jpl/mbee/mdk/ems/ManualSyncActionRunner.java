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

import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.transaction.ModelValidationResult;
import gov.nasa.jpl.mbee.mdk.ems.actions.CommitClientElementAction;
import gov.nasa.jpl.mbee.mdk.ems.sync.manual.ManualSyncRunner;
import gov.nasa.jpl.mbee.mdk.ems.sync.queue.OutputQueue;
import gov.nasa.jpl.mbee.mdk.ems.sync.queue.Request;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import org.json.simple.JSONObject;

import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

public class ManualSyncActionRunner<A extends NMAction & AnnotationAction> implements RunnableWithProgress {
    private final Class<A> actionClass;
    private final Project project;
    private final ManualSyncRunner manualSyncRunner;

    public ManualSyncActionRunner(Class<A> actionClass, Collection<Element> rootElements, Project project, int depth) {
        this.actionClass = actionClass;
        this.project = project;
        manualSyncRunner = new ManualSyncRunner(rootElements, project, depth);
    }

    @Override
    public void run(ProgressStatus progressStatus) {
        manualSyncRunner.run(progressStatus);
        if (manualSyncRunner.getValidationSuite() != null && manualSyncRunner.getValidationSuite().hasErrors()) {
            List<Annotation> annotations = manualSyncRunner.getValidationSuite().getValidationRules().stream()
                    .flatMap(validationRule -> validationRule.getViolations().stream())
                    .flatMap(violation -> violation.getActions().stream()).filter(action -> actionClass.isAssignableFrom(action.getClass()))
                    .map(action -> new Annotation(Annotation.getSeverityLevel(project, Annotation.ERROR), action.toString(), "", null, Collections.singletonList(action))).collect(Collectors.toList());
            if (annotations.isEmpty()) {
                Application.getInstance().getGUILog().log("[INFO] No manual sync validation actions of type " + actionClass.getSimpleName() + ".");
                return;
            }
            AnnotationAction annotationAction = ((AnnotationAction) annotations.get(0).getActions().get(0));
            if (!annotationAction.canExecute(annotations)) {
                Application.getInstance().getGUILog().log("[WARNING] Cannot execute " + actionClass.getSimpleName() + ". Aborting action.");
                return;
            }
            annotationAction.execute(annotations);
            Application.getInstance().getGUILog().log("[INFO] Executed " + NumberFormat.getInstance().format(annotations.size()) + " " + actionClass.getSimpleName() + " actions.");
        }
        else {
            Application.getInstance().getGUILog().log("[INFO] All validated elements are equivalent.");
        }
    }
}

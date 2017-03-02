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
package gov.nasa.jpl.mbee.mdk.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonpatch.diff.JsonDiff;
import com.nomagic.ci.persistence.IProject;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.core.project.ProjectDescriptorsFactory;
import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.magicdraw.foundation.MDObject;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.ems.ImportException;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.json.JsonPatchUtils;
import gov.nasa.jpl.mbee.mdk.lib.Changelog;
import gov.nasa.jpl.mbee.mdk.lib.MDUtils;
import gov.nasa.jpl.mbee.mdk.lib.Utils;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Collection;

public class DebugExportImportModels2 extends MDAction {

    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_ID = "Debug";

    public DebugExportImportModels2() {
        super(DEFAULT_ID, "(Debug Export)", null, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Utils.guilog("****");
        Collection<Element> selectedElements = MDUtils.getSelection(e, false);
        Element element = (Element) selectedElements.toArray()[0];
        Project project = Project.getProject(element);

        Utils.guilog(project.getID());
        Utils.guilog(Converters.getProjectToIdConverter().apply(project));
        Utils.guilog(project.getPrimaryProject().getProjectID());
        Utils.guilog(Converters.getIProjectToIdConverter().apply(project.getPrimaryProject()));
        Utils.guilog(" ");
        Utils.guilog(Boolean.toString(!project.getPrimaryProject().getLocationURI().isFile()));
        Utils.guilog(Boolean.toString(project.isRemote()));
        Utils.guilog(ProjectUtilities.getResourceID(project.getPrimaryProject().getLocationURI()));
        IProject iProject = ProjectUtilities.getAttachedProject(element);
        Utils.guilog(iProject == null ? "null attached" : iProject.getProjectID());
        Utils.guilog(iProject == null ? "null attached" : Boolean.toString(!iProject.getLocationURI().isFile()));
        Utils.guilog(" ");

        Utils.guilog(((BaseElement) element).getID());
        Utils.guilog(((MDObject) element).getID());
        Utils.guilog(element.getID());
        Utils.guilog(element.getLocalID());
        Utils.guilog(" ");
    }
}

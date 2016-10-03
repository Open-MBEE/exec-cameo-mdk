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
import com.github.fge.jsonpatch.diff.JsonDiff;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.ems.ImportException;
import gov.nasa.jpl.mbee.mdk.emf.EMFImporter;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.json.JsonPatchUtils;
import gov.nasa.jpl.mbee.mdk.lib.Changelog;
import gov.nasa.jpl.mbee.mdk.lib.MDUtils;
import org.json.simple.JSONObject;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Collection;

public class DebugExportImportModels2 extends MDAction {

    private static final long serialVersionUID = 1L;

    public static final String actionid = "Debug";

    public DebugExportImportModels2() {
        super(actionid, "(Debug Export)", null, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (1 < 0) {
            try {
                JsonNode before = JacksonUtils.getObjectMapper().readTree("{\"elements\":[{\"isStatic\":\"false\",\"ownedCommentIds\":[\"asdf\"],\"qualifierIds\":[],\"defaultValue\":{\"ownedCommentIds\":[],\"visibility\":null,\"max\":null,\"documentation\":\"\",\"mdExtensionsIds\":[],\"sysmlId\":\"_18_4_8bf0285_1474754768575_378243_17682\",\"appliedStereotypeInstanceId\":null,\"templateParameterId\":null,\"type\":\"DurationInterval\",\"ownerId\":\"_18_4_8bf0285_1474754759386_371969_17656\",\"clientDependencyIds\":[],\"expressionId\":null,\"min\":{\"ownedCommentIds\":[],\"visibility\":null,\"documentation\":\"\",\"mdExtensionsIds\":[],\"sysmlId\":\"_18_4_8bf0285_1474754783386_820149_17683\",\"appliedStereotypeInstanceId\":null,\"templateParameterId\":null,\"type\":\"Duration\",\"ownerId\":\"_18_4_8bf0285_1474754783391_679302_17685\",\"clientDependencyIds\":[],\"expressionId\":null,\"syncElementId\":null,\"observationIds\":[],\"name\":\"\",\"appliedStereotypeIds\":[],\"typeId\":null,\"expr\":{\"ownedCommentIds\":[],\"visibility\":null,\"documentation\":\"\",\"mdExtensionsIds\":[],\"sysmlId\":\"_18_4_8bf0285_1474754783392_354263_17686\",\"appliedStereotypeInstanceId\":null,\"templateParameterId\":null,\"type\":\"LiteralString\",\"ownerId\":\"_18_4_8bf0285_1474754783386_820149_17683\",\"clientDependencyIds\":[],\"expressionId\":null,\"syncElementId\":null,\"name\":\"\",\"appliedStereotypeIds\":[],\"typeId\":null,\"supplierDependencyIds\":[],\"value\":\"durationIntervalValue\",\"nameExpression\":null},\"supplierDependencyIds\":[],\"nameExpression\":null},\"syncElementId\":null,\"name\":\"\",\"appliedStereotypeIds\":[],\"typeId\":null,\"supplierDependencyIds\":[],\"nameExpression\":null},\"mdExtensionsIds\":[],\"isUnique\":\"true\",\"sysmlId\":\"_18_4_8bf0285_1474754759386_371969_17656\",\"appliedStereotypeInstanceId\":\"_18_4_8bf0285_1474754759387_574165_17657\",\"templateParameterId\":null,\"aggregation\":\"composite\",\"endIds\":[],\"type\":\"Property\",\"ownerId\":\"_18_4_8bf0285_1474754260823_764159_17452\",\"isLeaf\":\"false\",\"clientDependencyIds\":[],\"redefinedPropertyIds\":[],\"isReadOnly\":\"false\",\"syncElementId\":null,\"associationEndId\":null,\"isDerivedUnion\":\"false\",\"supplierDependencyIds\":[],\"isOrdered\":\"false\",\"nameExpression\":null,\"isDerived\":\"false\",\"upperValue\":null,\"visibility\":\"public\",\"documentation\":\"\",\"lowerValue\":null,\"datatypeId\":null,\"subsettedPropertyIds\":[],\"isID\":\"false\",\"name\":\"durationIntervalProp\",\"appliedStereotypeIds\":[\"_12_0_be00301_1164123483951_695645_2041\"],\"typeId\":null,\"interfaceId\":null,\"deploymentIds\":[],\"associationId\":null},{\"ownedCommentIds\":[],\"isStatic\":\"false\",\"qualifierIds\":[],\"defaultValue\":{\"ownedCommentIds\":[],\"elementId\":\"_18_4_8bf0285_1474754260823_764159_17452\",\"visibility\":null,\"documentation\":\"\",\"mdExtensionsIds\":[],\"sysmlId\":\"_18_4_8bf0285_1474754825760_412523_17716\",\"appliedStereotypeInstanceId\":null,\"templateParameterId\":null,\"type\":\"ElementValue\",\"ownerId\":\"_18_4_8bf0285_1474754799434_637544_17690\",\"clientDependencyIds\":[],\"expressionId\":null,\"syncElementId\":null,\"name\":\"\",\"appliedStereotypeIds\":[],\"typeId\":null,\"supplierDependencyIds\":[],\"nameExpression\":null},\"mdExtensionsIds\":[],\"isUnique\":\"true\",\"sysmlId\":\"_18_4_8bf0285_1474754799434_637544_17690\",\"appliedStereotypeInstanceId\":\"_18_4_8bf0285_1474754799434_362590_17691\",\"templateParameterId\":null,\"aggregation\":\"composite\",\"endIds\":[],\"type\":\"Property\",\"ownerId\":\"_18_4_8bf0285_1474754260823_764159_17452\",\"isLeaf\":\"false\",\"clientDependencyIds\":[],\"redefinedPropertyIds\":[],\"isReadOnly\":\"false\",\"syncElementId\":null,\"associationEndId\":null,\"isDerivedUnion\":\"false\",\"supplierDependencyIds\":[],\"isOrdered\":\"false\",\"nameExpression\":null,\"isDerived\":\"false\",\"upperValue\":null,\"visibility\":\"public\",\"documentation\":\"\",\"lowerValue\":null,\"datatypeId\":null,\"subsettedPropertyIds\":[],\"isID\":\"false\",\"name\":\"elementValueProp\",\"appliedStereotypeIds\":[\"_12_0_be00301_1164123483951_695645_2041\"],\"typeId\":null,\"interfaceId\":null,\"deploymentIds\":[],\"associationId\":null}],\"mmsVersion\":\"2.4\",\"source\":\"magicdraw\"}");
                JsonNode after = JacksonUtils.getObjectMapper().readTree("{\"elements\":[{\"ownedCommentIds\":[\"asdf\"],\"isStatic\":\"false\",\"qualifierIds\":[],\"defaultValue\":{\"ownedCommentIds\":[],\"visibility\":null,\"max\":null,\"documentation\":\"\",\"mdExtensionsIds\":[],\"sysmlId\":\"_18_4_8bf0285_1474754768575_378243_17682\",\"appliedStereotypeInstanceId\":null,\"templateParameterId\":null,\"type\":\"DurationInterval\",\"ownerId\":\"_18_4_8bf0285_1474754759386_371969_17656\",\"clientDependencyIds\":[],\"expressionId\":null,\"min\":{\"ownedCommentIds\":[],\"visibility\":null,\"documentation\":\"\",\"mdExtensionsIds\":[],\"sysmlId\":\"_18_4_8bf0285_1474754783386_820149_17683\",\"appliedStereotypeInstanceId\":null,\"templateParameterId\":null,\"type\":\"Duration\",\"ownerId\":\"_18_4_8bf0285_1474754783391_679302_17685\",\"clientDependencyIds\":[],\"expressionId\":null,\"syncElementId\":null,\"observationIds\":[],\"name\":\"\",\"appliedStereotypeIds\":[],\"typeId\":null,\"expr\":{\"ownedCommentIds\":[],\"visibility\":null,\"documentation\":\"\",\"mdExtensionsIds\":[],\"sysmlId\":\"_18_4_8bf0285_1474754783392_354263_17686\",\"appliedStereotypeInstanceId\":null,\"templateParameterId\":null,\"type\":\"LiteralString\",\"ownerId\":\"_18_4_8bf0285_1474754783386_820149_17683\",\"clientDependencyIds\":[],\"expressionId\":null,\"syncElementId\":null,\"name\":\"\",\"appliedStereotypeIds\":[],\"typeId\":null,\"supplierDependencyIds\":[],\"value\":\"durationIntervalValue\",\"nameExpression\":null},\"supplierDependencyIds\":[],\"nameExpression\":null},\"syncElementId\":null,\"name\":\"\",\"appliedStereotypeIds\":[],\"typeId\":null,\"supplierDependencyIds\":[],\"nameExpression\":null},\"mdExtensionsIds\":[],\"isUnique\":\"true\",\"sysmlId\":\"_18_4_8bf0285_1474754759386_371969_17656\",\"appliedStereotypeInstanceId\":\"_18_4_8bf0285_1474754759387_574165_17657\",\"templateParameterId\":null,\"aggregation\":\"composite\",\"endIds\":[],\"type\":\"Property\",\"ownerId\":\"_18_4_8bf0285_1474754260823_764159_17452\",\"isLeaf\":\"false\",\"clientDependencyIds\":[],\"redefinedPropertyIds\":[],\"isReadOnly\":\"false\",\"syncElementId\":null,\"associationEndId\":null,\"isDerivedUnion\":\"false\",\"supplierDependencyIds\":[],\"isOrdered\":\"false\",\"nameExpression\":null,\"isDerived\":\"false\",\"upperValue\":null,\"visibility\":\"public\",\"documentation\":\"\",\"lowerValue\":null,\"datatypeId\":null,\"subsettedPropertyIds\":[],\"isID\":\"false\",\"name\":\"durationIntervalProp\",\"appliedStereotypeIds\":[\"_12_0_be00301_1164123483951_695645_2041\"],\"typeId\":null,\"interfaceId\":null,\"deploymentIds\":[],\"associationId\":null},{\"ownedCommentIds\":[],\"isStatic\":\"false\",\"qualifierIds\":[],\"defaultValue\":{\"ownedCommentIds\":[],\"elementId\":\"_18_4_8bf0285_1474754260823_764159_17452\",\"visibility\":null,\"documentation\":\"\",\"mdExtensionsIds\":[],\"sysmlId\":\"_18_4_8bf0285_1474754825760_412523_17716\",\"appliedStereotypeInstanceId\":null,\"templateParameterId\":null,\"type\":\"ElementValue\",\"ownerId\":\"_18_4_8bf0285_1474754799434_637544_17690\",\"clientDependencyIds\":[],\"expressionId\":null,\"syncElementId\":null,\"name\":\"\",\"appliedStereotypeIds\":[],\"typeId\":null,\"supplierDependencyIds\":[],\"nameExpression\":null},\"mdExtensionsIds\":[],\"isUnique\":\"true\",\"sysmlId\":\"_18_4_8bf0285_1474754799434_637544_17690\",\"appliedStereotypeInstanceId\":\"_18_4_8bf0285_1474754799434_362590_17691\",\"templateParameterId\":null,\"aggregation\":\"composite\",\"endIds\":[],\"type\":\"Property\",\"ownerId\":\"_18_4_8bf0285_1474754260823_764159_17452\",\"isLeaf\":\"false\",\"clientDependencyIds\":[],\"redefinedPropertyIds\":[],\"isReadOnly\":\"false\",\"syncElementId\":null,\"associationEndId\":null,\"isDerivedUnion\":\"false\",\"supplierDependencyIds\":[],\"isOrdered\":\"false\",\"nameExpression\":null,\"isDerived\":\"false\",\"upperValue\":null,\"visibility\":\"public\",\"documentation\":\"\",\"lowerValue\":null,\"datatypeId\":null,\"subsettedPropertyIds\":[],\"isID\":\"false\",\"name\":\"elementValueProp\",\"appliedStereotypeIds\":[\"_12_0_be00301_1164123483951_695645_2041\"],\"typeId\":null,\"interfaceId\":null,\"deploymentIds\":[],\"associationId\":null}],\"mmsVersion\":\"2.4\",\"source\":\"magicdraw\"}");
                JsonNode patch = JsonDiff.asJson(before, after);
                System.out.println("Patch: " + JsonPatchUtils.isEqual(before, after) + " : " + JacksonUtils.getObjectMapper().writeValueAsString(patch));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            return;
        }
        //Application.getInstance().getGUILog().log("BOO!");
        Collection<Element> selectedElements = MDUtils.getSelection(e, false);
        if (selectedElements.size() != 1) {
            Application.getInstance().getGUILog().log("[ERROR] Invalid selection.");
            return;
        }
        Element element = selectedElements.iterator().next();

        Application.getInstance().getGUILog().log("[INFO] Exporting, deleting, and importing " + element.getHumanName() + ".");
        JSONObject jsonObject = Converters.getElementToJsonConverter().apply(element, Project.getProject(element));
        if (jsonObject == null) {
            Application.getInstance().getGUILog().log("[ERROR] Null JSON for " + element + " " + element.getHumanName());
            return;
        }
        System.out.println(jsonObject);
        if (!SessionManager.getInstance().isSessionCreated()) {
            SessionManager.getInstance().createSession("CopyAction");
        }
        Changelog.Change<Element> elementChange = null;
        try {
            ModelElementsManager.getInstance().removeElement(element);
            elementChange = Converters.getJsonToElementConverter().apply(jsonObject, Project.getProject(element), false);
        } catch (ReadOnlyElementException | ImportException ie) {
            ie.printStackTrace();
            SessionManager.getInstance().cancelSession();
        }
        if (SessionManager.getInstance().isSessionCreated()) {
            SessionManager.getInstance().closeSession();
        }
        if (elementChange == null) {
            Application.getInstance().getGUILog().log("[ERROR] Null imported element.");
            return;
        }
        Application.getInstance().getGUILog().log("[INFO] " + elementChange.getType() + " => " + elementChange.getChanged().getHumanName());


        //EMFImporter imp = new EMFImporter(element);
        //imp.createElementsFromJSON();
        jsonObject = Converters.getElementToJsonConverter().apply(elementChange.getChanged(), Project.getProject(elementChange.getChanged()));
        if (jsonObject == null) {
            Application.getInstance().getGUILog().log("[ERROR] Null JSON2 for " + elementChange.getChanged() + " " + elementChange.getChanged());
            return;
        }
        System.out.println(jsonObject);

        /*
        Collection<Element> selectedElements = MDUtils.getSelection(e, Configurator.isLastContextDiagram());

        if (selectedElements.toArray()[0] instanceof Element) {
            element = (Element) selectedElements.toArray()[0];
        }
        int depth = 0;
        boolean packageOnly = false;

        ModelExporter me;
        // GUILog gl = Application.getInstance().getGUILog();

        if (element == Application.getInstance().getProject().getModel()) {
            me = new ModelExporter(Application.getInstance().getProject(), depth, packageOnly);
            System.out.println(element);
        }
        else {
            System.out.println(element);
            Set<Element> root = new HashSet<Element>();
            root.add(element);
            me = new ModelExporter(root, depth, packageOnly, Application.getInstance().getProject().getPrimaryProject());
        }
        long start1 = System.currentTimeMillis();
        // JSONObject result = me.getResult();
        // String json = result.toJSONString();
        long stop1 = System.currentTimeMillis();
        JSONObject result1 = me.getResult();
        String jsonemf = result1.toJSONString();
        long stop2 = System.currentTimeMillis();
        // System.out.println(json);
        System.out.println((stop1 - start1) + " milliseconds pre and EMF: " + (stop2 - stop1));
        // System.out.println(jsonemf);

        FileWriter fw;
        try {
            fw = new FileWriter(new File("DebugExportImportModelsData.json"));
            fw.append(jsonemf);
            fw.flush();
            fw.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        */
    }
}

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

import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.ui.EnvironmentLockManager;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.validation.ElementValidationRuleImpl;
import com.nomagic.magicdraw.validation.SmartListenerConfigurationProvider;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.jmi.smartlistener.SmartListenerConfig;
import com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces.Interface;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.stylesaver.validationfixes.FixNone;
import gov.nasa.jpl.mbee.stylesaver.validationfixes.FixStyleMismatchRestore;
import gov.nasa.jpl.mbee.stylesaver.validationfixes.FixStyleMismatchUpdate;

import java.util.*;

/**
 * Validation class that provides a run() method for comparing the styles in the
 * view tag to the styles currently on the diagram.
 *
 * @author Benjamin Inada, JPL/Caltech
 */
public class StyleValidation implements ElementValidationRuleImpl, SmartListenerConfigurationProvider {
    /**
     * First method invoked.
     *
     * @param project    a project of the constraint.
     * @param constraint constraint which defines validation rules.
     */
    @Override
    public void init(Project project, Constraint constraint) {
    }

    /**
     * Returns a map of classes and smart listener configurations.
     *
     * @return a <code>Map</code> of smart listener configurations.
     */
    @Override
    public Map<Class<? extends Element>, Collection<SmartListenerConfig>> getListenerConfigurations() {
        Map<Class<? extends Element>, Collection<SmartListenerConfig>> configMap = new HashMap<Class<? extends Element>, Collection<SmartListenerConfig>>();
        SmartListenerConfig config = new SmartListenerConfig();
        SmartListenerConfig nested = config.listenToNested("ownedOperation");
        nested.listenTo("name");
        nested.listenTo("ownedParameter");

        Collection<SmartListenerConfig> configs = Collections.singletonList(config);
        configMap.put(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class, configs);
        configMap.put(Interface.class, configs);
        return configMap;
    }

    /**
     * Executes the rule.
     *
     * @param project    project of the constraint.
     * @param constraint constraint that defines validation rules.
     * @param elements   collection of elements to validate.
     * @return a set of <code>Annotation</code> objects which specify invalid
     * objects.
     */
    @Override
    public Set<Annotation> run(Project project, Constraint constraint, Collection<? extends Element> elements) {
        Set<Annotation> result = new HashSet<Annotation>();

        boolean wasLocked = EnvironmentLockManager.isLocked();
        try {
            EnvironmentLockManager.setLocked(true);

            // Note that validation rule has to constrain elements to Diagrams
            // otherwise scope will break
            Collection<DiagramPresentationElement> diagCollection = StyleSaverUtils
                    .findDiagramPresentationElements(elements);

            Stereotype workingStereotype = StyleSaverUtils.getWorkingStereotype(project);

            // iterate over each diagram checking for matched tag styling and
            // actual styling
            for (DiagramPresentationElement diag : diagCollection) {
                if (StyleSaverUtils.isGoodStereotype(diag, workingStereotype)) {
                    // get the style currently in the tag
                    Object tagStyleObj = StereotypesHelper.getStereotypePropertyFirst(diag.getElement(),
                            workingStereotype, "style");
                    String tagStyle = StereotypesHelper.getStereotypePropertyStringValue(tagStyleObj);

                    if ((tagStyle == null) || (tagStyle.equals(""))) {
                        continue;
                    }

                    // get the style currently on the diagram by performing a
                    // dummy save
                    String currStyle = ViewSaver.save(project, diag, true);

                    // compare styles and add annotation if they do not match
                    if (!tagStyle.equals(currStyle)) {
                        // add a fix for the style mismatch -- update the tag
                        NMAction styleMismatchUpdate = new FixStyleMismatchUpdate(diag);

                        // add a fix for the style mismatch -- restore the
                        // styles
                        NMAction styleMismatchRestore = new FixStyleMismatchRestore(diag);

                        List<NMAction> actionList = new ArrayList<NMAction>();
                        actionList.add(styleMismatchUpdate);
                        actionList.add(new FixNone(null));
                        actionList.add(styleMismatchRestore);

                        // create the annotation
                        Annotation annotation = new Annotation(diag, constraint, actionList);
                        result.add(annotation);
                    }
                }
            }

            Application.getInstance().getGUILog().log("Style validation done.");
        } finally {
            EnvironmentLockManager.setLocked(wasLocked);
        }

        return result;
    }

    /**
     * Invoked when this instance is no longer needed.
     */
    @Override
    public void dispose() {
    }
}

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

import gov.nasa.jpl.mbee.stylesaver.validationfixes.FixNoViewStereotype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.ui.EnvironmentLockManager;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.validation.ElementValidationRuleImpl;
import com.nomagic.magicdraw.validation.SmartListenerConfigurationProvider;
import com.nomagic.uml2.ext.jmi.smartlistener.SmartListenerConfig;
import com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces.Interface;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

/**
 * Validation class that provides a run() method for checking whether or not the
 * active diagram has the stereotype view.
 * 
 * @author Benjamin Inada, JPL/Caltech
 */
public class ViewStereotypeSetValidation implements ElementValidationRuleImpl,
        SmartListenerConfigurationProvider {
    /**
     * First method invoked.
     * 
     * @param project
     *            a project of the constraint.
     * @param constraint
     *            constraint which defines validation rules.
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
     * @param project
     *            project of the constraint.
     * @param constraint
     *            constraint that defines validation rules.
     * @param elements
     *            collection of elements to validate.
     * @return a set of <code>Annotation</code> objects which specify invalid
     *         objects.
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

            // iterate over each diagram checking if the view stereotype is
            // applied
            for (DiagramPresentationElement diag: diagCollection) {
                if (!StyleSaverUtils.isGoodStereotype(diag, workingStereotype)) {
                    // add a fix for diagrams without the view stereotype
                    NMAction noView = new FixNoViewStereotype(diag);

                    List<NMAction> actionList = new ArrayList<NMAction>();

                    actionList.add(noView);

                    // create the annotation
                    Annotation annotation = new Annotation(diag, constraint, actionList);
                    result.add(annotation);
                }
            }
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

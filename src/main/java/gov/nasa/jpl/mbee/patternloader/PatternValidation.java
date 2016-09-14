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
package gov.nasa.jpl.mbee.patternloader;

import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.ui.EnvironmentLockManager;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.magicdraw.uml.valueprovider.SmartListenerConfigurationProvider;
import com.nomagic.magicdraw.validation.ElementValidationRuleImpl;
import com.nomagic.uml2.ext.jmi.smartlistener.SmartListenerConfig;
import com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces.Interface;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.patternloader.validationfixes.FixPatternMismatchAll;
import gov.nasa.jpl.mbee.patternloader.validationfixes.FixPatternMismatchSelect;
import gov.nasa.jpl.mbee.stylesaver.StyleSaverUtils;
import gov.nasa.jpl.mbee.stylesaver.ViewSaver;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.*;

/**
 * Validation class for comparing the styles in the pattern diagram to the
 * styles currently on the diagram.
 *
 * @author Benjamin Inada, JPL/Caltech
 */
public class PatternValidation implements ElementValidationRuleImpl, SmartListenerConfigurationProvider {
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

            for (DiagramPresentationElement currDiagram : diagCollection) {
                // try to find a good requester on the diagram
                PresentationElement requester = locateTarget(currDiagram, project);

                if (requester == null) {
                    continue;
                }

                // if we did find a good requester, save the style of the first
                // diagram in the package as the pattern
                Collection<DiagramPresentationElement> patternDiags = PatternLoaderUtils
                        .getPatternDiagrams(requester);
                PatternSaver ps = new PatternSaver();
                ps.savePattern(project, patternDiags.iterator().next());

                // get the style on the pattern
                JSONObject pattern = ps.getPattern();

                // get the style on the target
                Diagram requesterDiagramElem = (Diagram) project
                        .getElementByID(requester.getElement().getID());
                DiagramPresentationElement requesterDiag = project.getDiagram(requesterDiagramElem);
                String style = ViewSaver.save(project, requesterDiag, true);

                // parse the style string
                JSONParser parser = new JSONParser();
                Object parsedStyle = null;
                try {
                    parsedStyle = parser.parse(style);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return null;
                }

                JSONObject styleObj = (JSONObject) parsedStyle;

                // add types that do not match the pattern style to this
                // variable
                HashSet<String> badElemTypes = new HashSet<String>();

                for (PresentationElement elem : requesterDiag.getPresentationElements()) {
                    // get the style of the element on the diagram
                    String unparsedElemStyle = (String) styleObj.get(elem.getID());
                    String elemStyle = PatternLoaderUtils.removeUnnecessaryProperties(unparsedElemStyle);

                    // get the style of the element on the pattern
                    String elemType = elem.getHumanType();
                    String patternStyle = (String) pattern.get(elemType);

                    boolean styleMatch = elemStyle.equals(patternStyle);

                    // there is a difference between the style and the pattern
                    if (!styleMatch) {
                        badElemTypes.add(elemType);
                    }
                }

                if (!badElemTypes.isEmpty()) {
                    // add a fix for the mismatch - user select repairs for
                    // styles per element type
                    NMAction patternMismatchSelect = new FixPatternMismatchSelect(project, requesterDiag,
                            pattern, badElemTypes);

                    // add a fix for the mismatch - automatically sync styles on
                    // all element types
                    NMAction patternMismatchAll = new FixPatternMismatchAll(requesterDiag, pattern);

                    List<NMAction> actionList = new ArrayList<NMAction>();
                    actionList.add(patternMismatchAll);
                    actionList.add(patternMismatchSelect);

                    // create the annotation
                    Annotation annotation = new Annotation(requesterDiag, constraint, actionList);
                    result.add(annotation);
                }
            }

            Application.getInstance().getGUILog().log("Pattern validation done.");
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

    private PresentationElement locateTarget(DiagramPresentationElement diag, Project project) {
        PresentationElement requester = null;
        List<PresentationElement> diagElems = diag.getPresentationElements();

        // iterate over each diagram element checking to see if it's a good
        // requester
        for (PresentationElement requesterCand : diagElems) {
            if (PatternLoaderUtils.isGoodElementRequester(requesterCand)) {
                requester = requesterCand;
            }
        }

        return requester;
    }
}

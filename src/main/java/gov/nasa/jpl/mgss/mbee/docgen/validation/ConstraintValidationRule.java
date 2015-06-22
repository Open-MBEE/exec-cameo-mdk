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
package gov.nasa.jpl.mgss.mbee.docgen.validation;

import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mbee.constraint.BasicConstraint;
import gov.nasa.jpl.mbee.constraint.BasicConstraint.Type;
import gov.nasa.jpl.mbee.generator.DocumentValidator;
import gov.nasa.jpl.mbee.lib.CompareUtils;
import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.MdDebug;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.lib.Utils2;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.magicdraw.validation.ElementValidationRuleImpl;
import com.nomagic.magicdraw.validation.SmartListenerConfigurationProvider;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.jmi.smartlistener.SmartListenerConfig;
import com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces.Interface;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.OpaqueExpression;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;

/**
 * A constraint in some context of the model, whose violation will be posted in
 * the MD validation results window.
 * 
 */
public class ConstraintValidationRule extends ValidationRule implements ElementValidationRuleImpl,
        SmartListenerConfigurationProvider {

    protected Element                                                        constraintElement               = null;
    // protected List<Element> context = new ArrayList<Element>();
    protected Element                                                        context                         = null;
    protected Constraint                                                     constraint                      = null;

    protected Set<BaseElement>                                               elementsWithConstraints         = new LinkedHashSet<BaseElement>();
    protected Map<BaseElement, Set<gov.nasa.jpl.mbee.constraint.Constraint>> elementToConstraintMap          = new TreeMap<BaseElement, Set<gov.nasa.jpl.mbee.constraint.Constraint>>(
                                                                                                                     CompareUtils.GenericComparator
                                                                                                                             .instance());
    protected Map<gov.nasa.jpl.mbee.constraint.Constraint, Set<BaseElement>> constraintToElementMap          = new TreeMap<gov.nasa.jpl.mbee.constraint.Constraint, Set<BaseElement>>(
                                                                                                                     CompareUtils.GenericComparator
                                                                                                                             .instance());
    protected Map<BaseElement, Set<ValidationRuleViolation>>                 constraintElementToViolationMap = new TreeMap<BaseElement, Set<ValidationRuleViolation>>(
                                                                                                                     CompareUtils.GenericComparator
                                                                                                                             .instance());
    protected Set<Annotation>                                                annotations                     = null;

    public BasicConstraint.Type                                              constraintType                  = Type.ANY;
    public boolean                                                           loggingResults                  = true;

    public ConstraintValidationRule() {
        super("Constraint", "Model constraint violation", ViolationSeverity.WARNING);
        Debug.outln("ConstraintValidationRule()");
    }

    // public ConstraintValidationRule( Element constraintElement, Element
    // context, Constraint constraint ) {
    // this.constraintElement = constraintElement;
    // this.context = context;
    // this.constraint = constraint;
    // }
    // // public ConstraintValidationRule( Element constraint,
    // Collection<Element> context ) {
    // // this.constraint = constraint;
    // // this.context.addAll( context );
    // // }

    @Override
    public ValidationRuleViolation addViolation(ValidationRuleViolation v) {
        if (v == null)
            return null;
        Utils2.addToSet(constraintElementToViolationMap, v.getElement(), v);
        return super.addViolation(v);
    }

    @Override
    public ValidationRuleViolation addViolation(Element e, String comment) {
        ValidationRuleViolation v = super.addViolation(e, comment);
        Utils2.addToSet(constraintElementToViolationMap, v.getElement(), v);
        return v;
    }

    @Override
    public ValidationRuleViolation addViolation(Element e, String comment, boolean reported) {
        ValidationRuleViolation v = super.addViolation(e, comment, reported);
        Utils2.addToSet(constraintElementToViolationMap, v.getElement(), v);
        return v;
    }

    public Set<ValidationRuleViolation> getViolations(Element constraintElement) {
        return constraintElementToViolationMap.get(constraintElement);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.nomagic.magicdraw.validation.ElementValidationRuleImpl#init(com.nomagic
     * .magicdraw.core.Project,
     * com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint)
     */
    @Override
    public void init(Project paramProject, Constraint paramConstraint) {
        Debug.outln("init(Project=" + paramProject + ", Constraint=" + paramConstraint + ")");
        if (constraintElement == null)
            constraintElement = paramConstraint;
    }

    protected void initConstraintMaps(Project paramProject, Collection<? extends Element> paramCollection) {
        elementToConstraintMap.clear();
        constraintToElementMap.clear();
        elementsWithConstraints.clear();
        constraintElementToViolationMap.clear();

        if (Utils2.isNullOrEmpty(paramCollection)) {
            paramCollection = Utils2.newList(Utils.getRootElement());
        }

        // // collect all constraints and the objects they constrain
        // Collection< String > ids = paramProject.getAllIDS();
        //
        // for ( String id : ids ) {
        // BaseElement elem = paramProject.getElementByID( id );

        for (Element elemt: paramCollection) {
            if (elemt == null)
                continue;

            // Get all constraints on this element since it was specifically
            // selected.
            List<gov.nasa.jpl.mbee.constraint.Constraint> constraints = BasicConstraint.getConstraints(elemt,
                    constraintType);
            if (!Utils2.isNullOrEmpty(constraints)) {
                elementsWithConstraints.add(elemt);
            }
            Utils2.addAllToSet(elementToConstraintMap, elemt, constraints);
            for (gov.nasa.jpl.mbee.constraint.Constraint constr: constraints) {
                Utils2.addToSet(constraintToElementMap, constr, elemt);
            }

            // Now look to see if it or its children are constraints
            List<Element> subElems = Utils2.newList(elemt);
            if (elemt instanceof Package) {
                subElems.addAll(Utils.collectOwnedElements(elemt, 0));
            }
            for (Element elem: subElems) {

                // Make a Constraint if elem is a constraint element and add it
                // with its constrained elements.
                if (BasicConstraint.constraintIsType(elem, constraintType)) { // elementIsConstraint(
                                                                              // elem
                                                                              // )
                                                                              // )
                                                                              // {
                    List<Object> constrained = BasicConstraint
                            .getConstrainedObjectsFromConstraintElement(elem);
                    gov.nasa.jpl.mbee.constraint.Constraint constr = new BasicConstraint(elem, constrained);
                    for (Object o: constrained) {
                        if (o instanceof BaseElement) {
                            BaseElement baseElem = (BaseElement)o;
                            elementsWithConstraints.add(baseElem);
                            Utils2.addToSet(constraintToElementMap, constr, baseElem);
                            Utils2.addToSet(elementToConstraintMap, baseElem, constr);
                        }
                    }
                }

            }
        }
    }

    public Collection<gov.nasa.jpl.mbee.constraint.Constraint> getAffectedConstraints(
            Collection<BaseElement> elements) {
        Set<gov.nasa.jpl.mbee.constraint.Constraint> constraints = new TreeSet<gov.nasa.jpl.mbee.constraint.Constraint>(
                CompareUtils.GenericComparator.instance());
        for (BaseElement elem: elements) {
            constraints.addAll(elementToConstraintMap.get(elem));
        }
        return constraints;
    }

    public static Element getConstraintObject(gov.nasa.jpl.mbee.constraint.Constraint constraint) {
        if (constraint == null || Utils2.isNullOrEmpty(constraint.getConstrainingElements())) {
            return null;
        }
        return constraint.getConstrainingElements().iterator().next();
    }

    public static boolean isUml(gov.nasa.jpl.mbee.constraint.Constraint constraint) {
        Element constrObj = getConstraintObject(constraint);
        return (constrObj != null && constrObj instanceof Constraint);
    }

    public static boolean isDocGenConstraint(gov.nasa.jpl.mbee.constraint.Constraint constraint) {
        Element constrObj = getConstraintObject(constraint);
        return (constrObj != null && StereotypesHelper.hasStereotypeOrDerived(constrObj,
                DocGen3Profile.constraintStereotype));
    }

    private static boolean isLanguageOcl(gov.nasa.jpl.mbee.constraint.Constraint constraint) {
        if (isDocGenConstraint(constraint))
            return true;
        if (!isUml(constraint))
            return false; // complain??
        ValueSpecification spec = ((Constraint)getConstraintObject(constraint)).getSpecification();
        if (!(spec instanceof OpaqueExpression))
            return false;
        OpaqueExpression expr = (OpaqueExpression)spec;
        List<String> languages = expr.getLanguage();
        for (String lang: languages) {
            if (lang.trim().equalsIgnoreCase("ocl")) {
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.nomagic.magicdraw.validation.ElementValidationRuleImpl#run(com.nomagic
     * .magicdraw.core.Project,
     * com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint,
     * java.util.Collection)
     */
    @Override
    public Set<Annotation> run(Project paramProject, Constraint paramConstraint,
            Collection<? extends Element> paramCollection) {
        Set<Annotation> result = new HashSet<Annotation>();

        MdDebug.logForce("*** Starting MDK Validate Constraints: " + constraintType + " ***");
        // boolean wasOn = Debug.isOn();
        // Debug.turnOn();

        // Debug.outln( "run(Project, " + paramConstraint + " , "
        // + paramCollection + ")" );

        initConstraintMaps(paramProject, paramCollection);

        // Set< BaseElement > elements = elementToConstraintMap.keySet();

        @SuppressWarnings("unchecked")
        Collection<gov.nasa.jpl.mbee.constraint.Constraint> constraints = (Collection<gov.nasa.jpl.mbee.constraint.Constraint>)(Utils2
                .isNullOrEmpty(elementsWithConstraints) ? (constraintToElementMap == null ? Utils2.newList()
                : constraintToElementMap.keySet()) : getAffectedConstraints(elementsWithConstraints));

        for (gov.nasa.jpl.mbee.constraint.Constraint constraint: constraints) {
            try {
                Boolean satisfied = DocumentValidator.evaluateConstraint(constraint, this,
                        isLanguageOcl(constraint));
                if (loggingResults) {
                    logResults(satisfied, constraint);
                }
                // Boolean satisfied = constraint.evaluate();
                // if ( satisfied != null && satisfied.equals( Boolean.FALSE ) )
                // {
                // //List<NMAction> actionList = new ArrayList<NMAction>();
                // //actionList.add(styleAdd);
                // Annotation annotation =
                // new Annotation( constraint.getViolatedConstraintElement(),
                // paramConstraint );
                // result.add(annotation);
                // }
            } catch (Throwable e) {
                Debug.error(true, false, "ConstraintValidationRule: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
        Project project = Utils.getProject();
        Constraint cons = (Constraint)project.getElementByID("_17_0_2_2_f4a035d_1360957024690_702520_27755");
        result = Utils.getAnnotations(this, project, cons);
        annotations = result;

        MdDebug.logForce("*** Finished MDK Validate Constraints: " + constraintType + " ***");

        // if ( !wasOn ) Debug.turnOff();
        return result;
    }

    public static void logResults(Boolean satisfied, gov.nasa.jpl.mbee.constraint.Constraint constraint) {
        if (satisfied == null) {
            String errorMsg = "";
            MdDebug.logForce("  Not OCL parsable: " + constraint + "; " + errorMsg);
        } else if (satisfied) {
            MdDebug.logForce("            Passed: " + constraint);
        } else {
            MdDebug.logForce("            Failed: " + constraint);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nomagic.magicdraw.validation.ElementValidationRuleImpl#dispose()
     */
    @Override
    public void dispose() {
    }

    @Override
    public Map<Class<? extends Element>, Collection<SmartListenerConfig>> getListenerConfigurations() {
        Map<Class<? extends Element>, Collection<SmartListenerConfig>> configMap = new HashMap<Class<? extends Element>, Collection<SmartListenerConfig>>();
        SmartListenerConfig config = new SmartListenerConfig();
        // SmartListenerConfig nested =
        // config.listenToNested(PropertyNames.ELEMENT);
        SmartListenerConfig nested = config.listenToNested("ownedOperation");
        nested.listenTo("name");
        nested.listenTo("ownedParameter");

        Collection<SmartListenerConfig> configs = Collections.singletonList(config);
        configMap.put(Element.class, configs);
        configMap.put(Interface.class, configs);
        return configMap;
    }

}

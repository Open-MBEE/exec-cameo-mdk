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
package gov.nasa.jpl.mbee.actions;

import gov.nasa.jpl.mbee.Configurator;
import gov.nasa.jpl.mbee.constraint.BasicConstraint.Type;
import gov.nasa.jpl.mbee.lib.MDUtils;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ConstraintValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ValidateConstraints extends MDAction {
    /**
     * 
     */
    private static final long           serialVersionUID = 2202161655434764023L;

    protected List<Element>             context          = new ArrayList<Element>();        // REVIEW
                                                                                             // --
                                                                                             // Is
                                                                                             // this
                                                                                             // being
                                                                                             // used?

    public static final String          actionid         = "ValidateConstraints";

    public static String                actionText       = "Validate constraints";

    private ConstraintValidationRule    constraintRule   = new ConstraintValidationRule();   // new
                                                                                             // ValidationRule("Constraint",
                                                                                             // "Model constraint violation",
                                                                                             // ViolationSeverity.WARNING);

    private ValidationSuite             validationUi     = new ValidationSuite("sweet");
    private Collection<ValidationSuite> validationOutput = new ArrayList<ValidationSuite>();

    public ValidateConstraints(Element context) {
        super(actionid, actionText, null, null);
        if (context != null)
            getContext().add(context);
        validationUi.addValidationRule(constraintRule);
        // Need Collection to use the utils.DisplayValidationWindow method
        validationOutput.add(validationUi);
    }

    public ValidateConstraints() {
        this(null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Collection<Element> selectedElements = MDUtils.getSelection(e, Configurator.isLastContextDiagram());
        // for ( Element elem : new ArrayList<Element>( selectedElements ) ) {
        // if ( elem instanceof Package ) {
        // selectedElements.addAll( Utils.collectOwnedElements( elem, 0 ) );
        // }
        // }
        setContext(selectedElements);

        // ConstraintValidationRule rule = new ConstraintValidationRule();
        constraintRule.constraintType = Type.STATIC;
        constraintRule.init(Utils.getProject(), null);
        constraintRule.run(Utils.getProject(), null, selectedElements);
        // RunnableSessionWrapperWithResult< Boolean > checkForRepairs =
        // new
        // RunnableSessionWrapperWithResult<Boolean>(String.format("%s - (iteration=%d)",
        // message, iterations)) {
        //
        // @Override
        // public Boolean run() {
        //
        // }
        // };
        Utils.displayValidationWindow(validationOutput, "User Validation Script Results");
    }

    @Override
    public void updateState() {
        // TODO Auto-generated method stub
        super.updateState();
    }

    /**
     * @return the context
     */
    public List<Element> getContext() {
        if (context == null)
            context = new ArrayList<Element>();
        return context;
    }

    /**
     * @param context
     *            the context to set
     */
    public void setContext(List<Element> context) {
        this.context = context;
    }

    /**
     * @param context
     *            the context to set
     */
    public void setContext(Collection<Element> context) {
        getContext().clear();
        getContext().addAll(context);
    }
}

package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mbee.lib.Utils2;
import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.generator.DocumentValidator;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ConstraintValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ViolationSeverity;
import gov.nasa.jpl.ocl.OclEvaluator;

import java.util.ArrayList;
import java.util.List;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

public class ViewpointConstraint extends Query {

    private Boolean iterate;
    private String expression;
    private Boolean report;
    private DocumentValidator dv;
    
    public ViewpointConstraint(DocumentValidator dv) {
        super();
        this.dv = dv;
    }
    
    
    
    @Override
    public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
        // construct a temporary validation suite from the global one to
        // generate docbook output for one constraint.
        ValidationSuite vs = new ValidationSuite(((NamedElement)dgElement).getName());
        ValidationRule rule = new ValidationRule(((NamedElement)dgElement).getName(), "Viewpoint Constraint", ViolationSeverity.WARNING);
        vs.addValidationRule(rule);
        ValidationRule r = dv.getViewpointConstraintRule();
        if ( r instanceof ConstraintValidationRule ) {
            rule.addViolations( ( (ConstraintValidationRule)r ).getViolations( dgElement ) );
        }
//        if (expression != null) {
//            if (iterate) {
//                for (Element e: targets) {
//                    
//                }
//            } else {
//                
//            }
            
            if ( report && !Utils2.isNullOrEmpty( rule.getViolations() ) ) {
                return vs.getDocBook();
            }
//        }
        
        
        return new ArrayList<DocumentElement>();
    }
    
    @Override
    public void initialize() {
        iterate = (Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.viewpointConstraintStereotype, "iterate", true);
        expression = (String)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.viewpointConstraintStereotype, "expression", "");
        report = (Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.viewpointConstraintStereotype, "validationReport", false);
    }
    
}

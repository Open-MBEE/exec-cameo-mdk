package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.generator.DocumentValidator;
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
        ValidationSuite vs = new ValidationSuite(((NamedElement)dgElement).getName());
        ValidationRule rule = new ValidationRule(((NamedElement)dgElement).getName(), "User Constraint", ViolationSeverity.ERROR);
        vs.addValidationRule(rule);
        if (expression != null) {
            if (iterate) {
                for (Element e: targets) {
                    
                }
            } else {
                
            }
            
            if (report) {
                return vs.getDocBook();
            }
        }
        
        
        return new ArrayList<DocumentElement>();
    }
    
    @Override
    public void initialize() {
        iterate = (Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.viewpointConstraintStereotype, "iterate", true);
        expression = (String)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.viewpointConstraintStereotype, "expression", "");
        report = (Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.viewpointConstraintStereotype, "report", false);
    }
    
}

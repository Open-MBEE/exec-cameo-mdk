package gov.nasa.jpl.mgss.mbee.docgen.validation;

import java.util.Collection;

import javax.swing.KeyStroke;


import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationManager;
import com.nomagic.magicdraw.validation.RuleViolationResult;
import com.nomagic.magicdraw.validation.ui.ValidationResultsWindowManager;

public abstract class RuleViolationAction extends MDAction implements IRuleViolationAction {
    
    private static final long serialVersionUID = 1L;

    public RuleViolationAction(String paramString1, String paramString2,
            KeyStroke paramKeyStroke, String paramString3) {
        super(paramString1, paramString2, paramKeyStroke, paramString3);
        // TODO Auto-generated constructor stub
    }

    protected Annotation annotation;
    private RuleViolationResult rvr;
    private ValidationWindowRun vwr;
    
    @Override
    public void setAnnotation(Annotation anno) {
        annotation = anno;
    }
    
    @Override
    public void setRuleViolationResult(RuleViolationResult rvr) {
        this.rvr = rvr;
    }
    
    @Override
    public void setValidationWindowRun(ValidationWindowRun vwr) {
        this.vwr = vwr;
    }

    public void removeViolationAndUpdateWindow() {
        vwr.results.remove(rvr);
        AnnotationManager.getInstance().remove(annotation);
        AnnotationManager.getInstance().update();
        //ValidationResultsWindowManager.updateValidationResultsWindow(vwr.id, vwr.title, vwr.runData, vwr.results);
    }
    
    public void removeViolationsAndUpdateWindow(Collection<Annotation> annos) {
        for (Annotation anno: annos) {
            vwr.results.remove(vwr.mapping.get(anno));
            AnnotationManager.getInstance().remove(anno);
        }
        AnnotationManager.getInstance().update();
        //ValidationResultsWindowManager.updateValidationResultsWindow(vwr.id, vwr.title, vwr.runData, vwr.results);
    }
}

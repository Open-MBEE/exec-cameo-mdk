package gov.nasa.jpl.mbee.mdk.validation;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationManager;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.validation.RuleViolationResult;
import gov.nasa.jpl.mbee.mdk.mms.sync.local.LocalDeltaProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.mdk.mms.sync.local.LocalDeltaTransactionCommitListener;
import gov.nasa.jpl.mbee.mdk.util.Utils;

import javax.swing.*;
import java.util.Collection;
import java.util.HashSet;

public abstract class RuleViolationAction extends MDAction implements IRuleViolationAction {

    private static final long serialVersionUID = 1L;

    public RuleViolationAction(String id, String name, KeyStroke stroke, String group) {
        super(id, name, stroke, group);
    }

    protected Annotation annotation;
    private RuleViolationResult rvr;
    private ValidationWindowRun vwr;

    public Annotation getAnnotation() {
        return annotation;
    }

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
        if (vwr == null) {
            return;
        }
        vwr.results.remove(rvr);
        AnnotationManager.getInstance().remove(annotation);
        AnnotationManager.getInstance().update();
        //ValidationResultsWindowManager.updateValidationResultsWindow(vwr.id, vwr.title, vwr.runData, vwr.results);
    }

    public void removeViolationsAndUpdateWindow(Collection<Annotation> annos) {
        for (Annotation anno : annos) {
            vwr.results.remove(vwr.mapping.get(anno));
            AnnotationManager.getInstance().remove(anno);
        }
        AnnotationManager.getInstance().update();
        //ValidationResultsWindowManager.updateValidationResultsWindow(vwr.id, vwr.title, vwr.runData, vwr.results);
    }

    public void saySuccess() {
        Application.getInstance().getGUILog().log("[INFO] Successful");
    }

    protected boolean doAction(Annotation anno) throws ReadOnlyElementException {
        return true;
    }

    protected void doAfterSuccess() {
    }

    protected void executeMany(Collection<Annotation> annos, String sessionName) {
        LocalDeltaTransactionCommitListener listener = LocalDeltaProjectEventListenerAdapter.getProjectMapping(Application.getInstance().getProject()).getLocalDeltaTransactionCommitListener();
        if (listener != null) {
            listener.setDisabled(true);
        }
        SessionManager.getInstance().createSession(sessionName);
        Collection<Annotation> toremove = new HashSet<Annotation>();
        try {
            boolean noneditable = false;
            for (Annotation anno : annos) {
                if (doAction(anno)) {
                    toremove.add(anno);
                }
                else {
                    noneditable = true;
                }
            }
            SessionManager.getInstance().closeSession();
            if (noneditable) {
                Application.getInstance().getGUILog().log("[ERROR] There were some elements couldn't be imported");
            }
            else {
                saySuccess();
            }
            //AnnotationManager.getInstance().update();
            this.removeViolationsAndUpdateWindow(toremove);

        } catch (Exception ex) {
            SessionManager.getInstance().cancelSession();
            Utils.printException(ex);
        }
        if (listener != null) {
            listener.setDisabled(false);
        }
    }

    protected void execute(String sessionName) {
        LocalDeltaTransactionCommitListener listener = LocalDeltaProjectEventListenerAdapter.getProjectMapping(Application.getInstance().getProject()).getLocalDeltaTransactionCommitListener();
        if (listener != null) {
            listener.setDisabled(true);
        }
        SessionManager.getInstance().createSession("Change Rel");
        try {
            if (doAction(null)) {
                SessionManager.getInstance().closeSession();
                saySuccess();
                doAfterSuccess();
                this.removeViolationAndUpdateWindow();
            }
            else {
                SessionManager.getInstance().cancelSession();
            }
            //AnnotationManager.getInstance().remove(annotation);
            //AnnotationManager.getInstance().update();
        } catch (Exception ex) {
            SessionManager.getInstance().cancelSession();
            Utils.printException(ex);
        }
        if (listener != null) {
            listener.setDisabled(false);
        }
    }
}

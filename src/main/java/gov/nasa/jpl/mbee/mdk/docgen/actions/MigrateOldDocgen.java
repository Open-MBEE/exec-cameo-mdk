package gov.nasa.jpl.mbee.mdk.docgen.actions;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.actions.MigrateToClassViewAction;
import gov.nasa.jpl.mbee.mdk.validation.IRuleViolationAction;
import gov.nasa.jpl.mbee.mdk.validation.RuleViolationAction;

import java.awt.event.ActionEvent;
import java.util.Collection;

public class MigrateOldDocgen extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {
    private static final long serialVersionUID = 1L;
    private Element element;

    public MigrateOldDocgen(Element e) {
        //JJS--MDEV-567 fix: changed 'Export' to 'Commit'
        //
        super("MigrateOldDocgen", "Migrate", null, null);
        this.element = e;
    }

    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return false;
    }

    @Override
    public void execute(Collection<Annotation> annos) {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        MigrateToClassViewAction a = new MigrateToClassViewAction(element);
        a.actionPerformed(null);
    }
}

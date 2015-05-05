package gov.nasa.jpl.mbee.ems.validation.actions;

import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;

import java.awt.event.ActionEvent;
import java.util.Collection;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;

public class CreateModuleSite extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    private static final long serialVersionUID = 1L;
    private String site;
    private String url;
    
    public CreateModuleSite(String site, String server) {
        super("Remedy", "Remedy", null, null);
        this.site = site;
        this.url = server;
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
        String message = "Go to " + url + "/share/page and create a site with name of " + site + ", then rerun validation.";
        Utils.showPopupMessage(message);
    }

}

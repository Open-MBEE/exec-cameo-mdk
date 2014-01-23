package gov.nasa.jpl.mbee.ems;

import gov.nasa.jpl.mbee.ems.validation.ViewValidator;

import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ValidateViewRunner implements RunnableWithProgress {

    private Element view;
    private boolean recurse; 
    
    public ValidateViewRunner(Element view, boolean recurse) {
        this.view = view;
        this.recurse = recurse;
    }
    
    @Override
    public void run(ProgressStatus arg0) {
        ViewValidator vv = new ViewValidator(view, recurse);
        if (vv.validate())
            vv.showWindow();
        
    }

}

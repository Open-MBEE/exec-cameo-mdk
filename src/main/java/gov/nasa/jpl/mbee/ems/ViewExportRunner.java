package gov.nasa.jpl.mbee.ems;

import gov.nasa.jpl.mbee.ems.validation.actions.ExportView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.simple.JSONObject;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ViewExportRunner implements RunnableWithProgress {

    private ExportView action;
    private Collection<Annotation> annotations;
    
    public ViewExportRunner(ExportView action, Collection<Annotation> annotations) {
        this.action = action;
        this.annotations = annotations;
    }
    
    @Override
    public void run(ProgressStatus arg0) {
        if (annotations == null)
            action.performAction();
        else
            action.performActions(annotations);
        
    }

}


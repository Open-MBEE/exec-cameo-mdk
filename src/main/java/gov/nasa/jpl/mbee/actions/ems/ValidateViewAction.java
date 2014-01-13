package gov.nasa.jpl.mbee.actions.ems;

import gov.nasa.jpl.mbee.ems.validation.ModelValidator;
import gov.nasa.jpl.mbee.ems.validation.ResultHolder;
import gov.nasa.jpl.mbee.ems.validation.ViewValidator;
import gov.nasa.jpl.mbee.viewedit.ViewEditUtils;

import java.awt.event.ActionEvent;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ValidateViewAction extends MDAction {

    private static final long serialVersionUID = 1L;
    private Element view;
    public static final String actionid = "ValidateViewAlfresco";
    
    public ValidateViewAction(Element e) {
        super(actionid, "Validate View With VE", null, null);
        view = e;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        ViewValidator vv = new ViewValidator(view, false);
        vv.validate();
        vv.showWindow();
    }
}


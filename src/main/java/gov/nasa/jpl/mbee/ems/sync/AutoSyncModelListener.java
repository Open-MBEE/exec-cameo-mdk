package gov.nasa.jpl.mbee.ems.sync;

import java.beans.PropertyChangeEvent;

import javax.jmi.reflect.RefObject;
import javax.swing.JOptionPane;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.transaction.ModelListener;

public class AutoSyncModelListener implements ModelListener {

    @Override
    public void modelChanged(PropertyChangeEvent paramPropertyChangeEvent) {
        //this gets called during a session on every model change, the difference with TransactionCommitListener is tcl gets the entire
        //sequence of events after a session ends and this gets them one by one. with tcl, the source element would have its "complete" properties like
        //owner since it's at the session end, and we can just use the source element on a change like INSTANCE_CREATED. With this, if the change
        //is INSTANCE_CREATED, the element doesn't have its owner yet. we'll need to cache these somehow and consolidate changes after the session ends.
        
        //JOptionPane.showConfirmDialog(null, "calle");
        Object a = paramPropertyChangeEvent.getSource();
        Element e = (Element)a;
        return;
        
    }

    @Override
    public void changed(RefObject paramRefObject) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void beforeChange(RefObject paramRefObject, String paramString) {
        // TODO Auto-generated method stub
        
    }

}

package gov.nasa.jpl.mbee.ems.sync;

import java.beans.PropertyChangeEvent;

import javax.jmi.reflect.RefObject;
import javax.swing.JOptionPane;

import com.nomagic.uml2.transaction.ModelListener;

public class AutoSyncModelListener implements ModelListener {

    @Override
    public void modelChanged(PropertyChangeEvent paramPropertyChangeEvent) {
        JOptionPane.showConfirmDialog(null, "calle");
        
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

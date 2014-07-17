package gov.nasa.jpl.mbee.ems.sync;

import gov.nasa.jpl.mbee.AutoSyncPlugin;
import gov.nasa.jpl.mbee.ems.ExportUtility;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.jmi.InstanceDeletedEvent;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralBoolean;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.transaction.TransactionCommitListener;
import com.nomagic.uml2.transaction.TransactionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.impl.PropertyNames;

/**
 * This class responds to commit done in the document.
 * @author jsalcedo
 *
 */
public class AutoSyncCommitListener implements TransactionCommitListener {
	/**
	 * Allow listener to be disabled during imports.
	 */
	private boolean disabled = false;
	private TransactionManager tm;

	public AutoSyncCommitListener() {
		
	}
    /**
     * Adapter to call handleChangeEvent() from the TransactionCommitListener
     * interface.
     */
	private class TransactionCommitHandler implements Runnable {
		private final Collection<PropertyChangeEvent> events;
		private Map<String, JSONObject> elements = new HashMap<String, JSONObject>();
		
		TransactionCommitHandler(final Collection<PropertyChangeEvent> events) {
			this.events = events;
		}

        @Override
        public void run() {
            if (disabled)
                return;
        	for (PropertyChangeEvent event: events) {

        		String strTmp = "NULL";
        		if(event != null) {
        			strTmp = event.toString();
        		}   
        		Object source = event.getSource();
        		if (source instanceof Element) {
        		    if (event instanceof InstanceDeletedEvent) {
        		        
        		    } else {
        		        String changedProperty = event.getPropertyName();
        		        if (changedProperty == null) {
        		            //multliple property changed...
        		        } else {
        		            if (event.getNewValue() == null && event.getOldValue() == null)
        		                continue;
        		            if (event.getNewValue() == null && event.getOldValue() != null || 
        		                    event.getNewValue() != null && event.getOldValue() == null ||
        		                    !event.getNewValue().equals(event.getOldValue()))
        		                handleChangedProperty((Element)source, changedProperty, event.getNewValue(), event.getOldValue());
        		        }
        		    }
        		}
        	}
        	if (!elements.isEmpty())
        	    sendChanges();
        	
        }
        
        private void sendChanges() {
            JSONObject toSend = new JSONObject();
            JSONArray eles = new JSONArray();
            eles.addAll(elements.values());
            toSend.put("elements", eles);
            String url = ExportUtility.getPostElementsUrl();
            if (url != null)
                ExportUtility.send(url, toSend.toJSONString());
        }
        
        private void handleChangedProperty(Element e, String property, Object newValue, Object oldValue) {
            JSONObject elementOb = null;
            if (property.equals(PropertyNames.NAME)) {
                if (elements.containsKey(ExportUtility.getElementID(e))) {
                    elementOb = elements.get(ExportUtility.getElementID(e));
                    elementOb.put("name", newValue);
                } else {
                    elementOb = new JSONObject();
                    elementOb.put("sysmlid", ExportUtility.getElementID(e));
                    elementOb.put("name", newValue);
                    elements.put(ExportUtility.getElementID(e), elementOb);
                }
            } else if (e instanceof Comment && ExportUtility.isElementDocumentation((Comment)e) && property.equals(PropertyNames.BODY)) {
                
            } else if (e instanceof ValueSpecification && property.equals(PropertyNames.VALUE)) {
                
            }
        }
	}

	public void disable() {
		disabled = true;
	}

	public  void enable() {
		disabled = false;
	}

    public TransactionManager getTm() {
		return tm;
	}

	public void setTm(TransactionManager tm) {
		this.tm = tm;
	}

	@Override
	public Runnable transactionCommited(Collection<PropertyChangeEvent> events) {
        return new TransactionCommitHandler(events);
	}
}

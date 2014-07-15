package gov.nasa.jpl.mbee.actions;

import gov.nasa.jpl.mbee.AutoSyncPlugin;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.teamwork.application.MUManager;
import com.nomagic.uml2.ext.jmi.InstanceDeletedEvent;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralBoolean;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.transaction.TransactionCommitListener;
import com.nomagic.uml2.transaction.TransactionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

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
	private String userName;
	
	public static final String VERSION_METADATA_STEREOTYPE_NAME = "Version Metadata";
	public static final String IS_CHANGED_TAG_NAME = "isChanged";
	public static final String EDITORS_TAG_NAME = "editors";
	private final Stereotype versionMetadataStereotype;
	
	public AutoSyncCommitListener() {
		this.userName = MUManager.getInstance().getUser();
		if (userName==null) {
			System.err.println("[VERSION-METADATA]: You are not logged in to Teamwork.  Disabling plugin.");
			disabled = true;
		}
		versionMetadataStereotype = StereotypesHelper.getStereotype(
				Application.getInstance().getProject(), 
				VERSION_METADATA_STEREOTYPE_NAME);
		if(versionMetadataStereotype==null) {
			System.err.println("[VERSION-METADATA]: Stereotype '"+VERSION_METADATA_STEREOTYPE_NAME + 
								"' does not exist in this project.  Disabling plugin.");
			disabled = true;
		}
	}
    /**
     * Adapter to call handleChangeEvent() from the TransactionCommitListener
     * interface.
     */
	private class TransactionCommitHandler implements Runnable {
		private final Collection<PropertyChangeEvent> events;

		TransactionCommitHandler(final Collection<PropertyChangeEvent> events) {
			this.events = events;
		}

        @Override
        public void run() {
        	
        	for (PropertyChangeEvent event: events) {

        		String strTmp = "NULL";
        		if(event != null) {
        			strTmp = event.toString();
        		}           	
        		if (event instanceof InstanceDeletedEvent) {
        			System.err.println("Deleted Element! '"+ strTmp +"'");

        			Element element = (Element)event.getSource();
        			if (element!=null && (element instanceof ValueSpecification)) {
        				handleChangeEventHelper(element);
        			}
        		}
        		else if(event instanceof PropertyChangeEvent) {
        			System.err.println("Updated Existing Element! '"+ strTmp +"'");
        			Element element1 = (Element)event.getSource();
        			if (element1 !=null) {
 //           			if (element1 !=null && (element1 instanceof ValueSpecification)) {

        				if (event.getNewValue() == null && event.getOldValue() != null) {
        					handleChangeEventHelper(element1);
        				} 
        				else if (event.getNewValue() != null && event.getOldValue() == null){
        					handleChangeEventHelper(element1);
        				} 
        				else if (event.getNewValue() == null && event.getOldValue() == null){
        					// Do Nothing
        				} 
        				else if (!event.getNewValue().equals(event.getOldValue())){
        					handleChangeEventHelper(element1);
        				}
        			}
        		}
        	}
        }
	}

	private void updateElement(Element e) {
		//these are here so we don't have the plugin fire on its own updates
		AutoSyncPlugin.getInstance().setActive(false);

		if (StereotypesHelper.hasStereotypeOrDerived(e, versionMetadataStereotype)) {
			handleChangeEventHelper(e);
		}
		else {
			for(Element owner : getOwnerSet(e)) { //owner-set is really a list
				if(StereotypesHelper.hasStereotypeOrDerived(owner, versionMetadataStereotype)) {
					handleChangeEventHelper(owner);
					break;
				}
			}
		}
		//these are here so we don't have the plugin fire on its own updates
		AutoSyncPlugin.getInstance().setActive(!disabled);
	}
	
	public void disable() {
		disabled = true;
	}

	public  void enable() {
		disabled = false;
	}

   private void handleChangeEvent(PropertyChangeEvent event) {
	   System.err.println("Change in Property named (" + event.getPropertyName() + ")");
	   System.err.println("new value= (" + event.getNewValue() + ") || old value= (" + event.getOldValue() + ")\n\n");
   }

   private void handleChangeEventHelper(Element e) {
		//check if isChanged

		Runnable runnable = new Runnable() {

			public void run(){

			}
		};

		Application.getInstance().getProject().getRepository().invokeAfterTransaction(runnable);

		//check if username is in editors set
	   }

   public TransactionManager getTm() {
		return tm;
	}

	public void setTm(TransactionManager tm) {
		this.tm = tm;
	}
	
	private List<Element> getOwnerSet(Element e) {
		List<Element> ownerList = new ArrayList<Element>();
		Element owner = e.getOwner();
		int level = 0;
		while(owner!=null) {
			level++;
			ownerList.add(owner);
			owner = owner.getOwner();

			if (level > 5)
				break;
		}
		return ownerList;
	}

	@Override
	public Runnable transactionCommited(Collection<PropertyChangeEvent> events) {
        return new TransactionCommitHandler(events);
	}
}

package gov.nasa.jpl.mbee.actions;

import java.beans.PropertyChangeEvent;
import java.util.Collection;

import com.nomagic.uml2.transaction.TransactionCommitListener;

/**
 * This class responds to commit done in the document.
 * @author jsalcedo
 *
 */
public class AutoSyncCommitListener implements TransactionCommitListener {

    /**
    * Allow listener to be disabled during imports.
    */
   private static boolean disabled = false;

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
            for (PropertyChangeEvent e: events) {
                handleChangeEvent(e);
            }
        }
    }

   public static void disable() {
       disabled = true;
   }

   public static void enable() {
       disabled = false;
   }

   private void handleChangeEvent(PropertyChangeEvent event) {
	   System.err.println("Change in Property named (" + event.getPropertyName() + ")");
	   System.err.println("new value= (" + event.getNewValue() + ") || old value= (" + event.getOldValue() + ")");
   }

   @Override
	public Runnable transactionCommited(Collection<PropertyChangeEvent> events) {
        return new TransactionCommitHandler(events);
	}

}

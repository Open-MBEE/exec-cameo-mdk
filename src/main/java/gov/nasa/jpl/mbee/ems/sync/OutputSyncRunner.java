package gov.nasa.jpl.mbee.ems.sync;

import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.lib.Utils;

public class OutputSyncRunner implements Runnable {
    public static Logger log = Logger.getLogger(OutputSyncRunner.class);

    public class SendThread extends Thread {
        Request r;

        SendThread(Request r) {
            this.r = r;
        }

        public void run() {
            if (r.getMethod().equals("DELETE"))
                ExportUtility.delete(r.getUrl(), r.isFeedback());
            else if (r.getMethod().equals("DELETEALL"))
                ExportUtility.deleteWithBody(r.getUrl(), r.getJson(), r.isFeedback());
            else if (r.getPm() != null)
                ExportUtility.send(r.getUrl(), r.getPm());
            else
                ExportUtility.send(r.getUrl(), r.getJson()/*, null*/, false, r.isSuppressGui()); //POST
        }
    }
    
    @Override
    public void run() {
        log.info("sync runner started");
        OutputQueue q = OutputQueue.getInstance();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                OutputQueueStatusConfigurator.getOutputQueueStatusAction().update();
            }
        });
        while (true) {
            try {
                if (q.isEmpty()) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            OutputQueueStatusConfigurator.getOutputQueueStatusAction().update();
                        }
                    });
                }
                final Request r = q.take();
                q.setCurrent(r);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        OutputQueueStatusConfigurator.getOutputQueueStatusAction().update(true);
                    }
                });
                log.info("got a request");
                if (r.getMethod().equals("LOG"))
                    Utils.guilog(r.getJson());
                else {
                	
                	SendThread st = new SendThread(r);
                    st.setName("SendThread");
                    st.start();
   
                 	//if background on server, the defaultWaitTime, 
                    //if not background and r.getWait() > defaultWaitTime, then use defaultWaitTime, otherwise r.wait ( = num of elements per seconds + 2 mins)
                	final int maximumWaitTime = 10; //second
                	final int waitTime = (r.isBackgournd() ? maximumWaitTime * 1000: (r.getWait() > maximumWaitTime*1000 ? maximumWaitTime*1000: r.getWait()));
                	
                	//if a user press cancel in queuedialog, current is set as null but this thread will keep running
                    log.info("Thread started.");
                    st.join(waitTime);  //TODO: The first time join should be fixed in short time?  Because a user press cancel, stuck here until it joined, not able to go to next queue
                    log.info("joined");
    	            
    	            log.warn("A queued send request didn't complete within wait time: " + r.toString());
    	           
    	            if ( r == q.getCurrent() && st.isAlive()){ //if (r != q.getCurrent() ) if a user pressed "cancel" in the queue dialog.
    	             	 
    	            	Utils.guilog("[INFO] A send request did not finish within expected time.");
   	                 	log.warn("A queued send request didn't complete within wait time: " + r.toString());
 
   	   	    	 		final JOptionPane userInput = new JOptionPane("The current send request did not finish within the timeout: " + waitTime /1000 + " min, do you want to wait longer?", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION );
    	    	 	   	final JDialog dialog = userInput.createDialog(null, "Select an Option");

    	                //?????????????????????? need timer to close the dialog automatically???????????????????????????
    	    	 	   	//timer to dispose the question dialog if a user does not answer within userInputTimeout
    	            	/*int userInputTimeout = 5; // seconds
    	    	 		TimerTask task = new TimerTask() {
    	    	 		    @Override
    	    	 		    public void run() {
	    	                     SwingUtilities.invokeLater(new Runnable() {
	    	                         @Override
	    	                         public void run() {
	    	                        	 dialog.dispose(); //this cause return JOptionPane.UNINITIALIZED_VALUE
	    	                         }
	    	                     });
    	    	 		    }
    	    	 		};
    	    	 		new Timer(true).schedule(task, userInputTimeout * 1000);
    	    	 		*/
    	                final AtomicReference<Boolean> userwait = new AtomicReference<Boolean>();
    	                
    	                //display dialog and wait users input
    	                SwingUtilities.invokeLater(new Runnable() {
    	                     @Override
    	                     public void run() {
    	                    	 if (dialog.isDisplayable()) {
	                                 dialog.setVisible(true);
	                            	 Object selectedValue = userInput.getValue();
	                            	 if ( selectedValue == JOptionPane.UNINITIALIZED_VALUE || selectedValue == null) //UNINITIALIZED_VALUE if a user did nothing and dialog is disposed, null if a user press X to close the dialog
	                            	     userwait.set(false);
	                            	 else if ((Integer)selectedValue == JOptionPane.YES_OPTION)
	                                     userwait.set(true);
	                                 else //if( (int)selectedValue == JOptionPane.NO_OPTION) {
	                                     userwait.set(false);
    	                    	 }
    	                    	 else
    	                    		 userwait.set(false);
    	                    		 
    	                     }
    	                });
    	                //looping until 1) a user press Yes, No or Cancel for wait
    	                //				2) a user press "cancel" button in the queue dialog ( r != q.getCurrent())
    	                //              3) or sending is complete (st.isAlive() == false)
    	                while (st.isAlive() && r == q.getCurrent() ) { // r is not current if "cancel" in queue dialog is pressed
    	                	Thread.sleep(1000);
    	                     final Boolean result = userwait.get();
    	                     if (result != null && result) { //a user input = "Yes"
    	                    	 Utils.guilog("loop - wait");
    	                         st.join(waitTime); //??????????????????????? what this wait time shoud be.....
    	                         if (st.isAlive() && r == q.getCurrent()) {
    	                             userwait.set(null);//reset
    	                             SwingUtilities.invokeLater(new Runnable() {
    	                                 @Override
    	                                 public void run() {
    	                                	 if (dialog.isDisplayable()){
	    	                                     dialog.setVisible(true);
	    	                                	 Object selectedValue = userInput.getValue();
	    	                                	 if ( selectedValue == JOptionPane.UNINITIALIZED_VALUE || selectedValue == null) //UNINITIALIZED_VALUE if a user did nothing and dialog is disposed, null if a user press X to close the dialog
	    	                                	     userwait.set(false);
	    	                                	 else if ((Integer)selectedValue == JOptionPane.YES_OPTION)
	    	                                         userwait.set(true);
	    	                                     else //if( (int)selectedValue == JOptionPane.NO_OPTION) {
	    	                                         userwait.set(false);
    	                                	 }
    	                                	 else
    	                                		 userwait.set(false); //dialog is already disposed 
    	                                 }
    	                             });
    	                         }
    	                     } else if (result != null && !result) {//a user input = "No" or "Cancel" or close dialog (a user does not want to wait any longer) or the timer is reached 
    	                         Utils.guilog("loop - break");
    	                    	 break;
    	                     }
    	                     else {
    	                    	 Utils.guilog("loop - continue");
    	                         continue; //no user input yet
    	                     }
    	                 } //end of while

    	                 
	                     SwingUtilities.invokeLater(new Runnable() {
	                         @Override
	                         public void run() {
	                        	 Utils.guilog("Disposing dialog2");
                        		 dialog.dispose();
	                         }
	                     });
    	                 
    	             }	//end of r == current
    	            
                } //end of if (!r.getMethod().equals("LOG"))
                
                if ( r == q.getCurrent() ) {//if this r is still current - it will be false if a user press "cancel" in the queue dialog
                	q.setCurrent(null);
                	Utils.guilog("Setting current as null");
                }
                else if ( q.getCurrent() == null)
                	Utils.guilog("Current was null");
                else 
                	Utils.guilog("Current is already set to next queue item");
                
            } catch (Exception e) {
                log.error("", e);
            }
            if (q.isEmpty()) {
                Utils.guilog("[INFO] Finished processing queued requests.");
            }
            
        }
    }
}

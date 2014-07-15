package gov.nasa.jpl.mbee;

import gov.nasa.jpl.mbee.actions.AutoSyncCommitListener;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectEventListenerAdapter;
import com.nomagic.uml2.transaction.TransactionManager;

/*
 * This class is responsible for performing automatic syncs with
 * MMS whenever any type of commit is executed.
 * This class is also responsible for start the REST webservices.
 */
public class AutoSyncPlugin extends MDPlugin {
	AutoSyncCommitListener listener = null;
	private static AutoSyncPlugin instance;
	private boolean active;
	
	public AutoSyncPlugin() {
		instance = this;
	}
	
	public static AutoSyncPlugin getInstance() {
		return instance;
	}
	
	@Override
	public void initConfigurations() {
		System.err.println("Initiatlizing the AutoSyncPlugin...");
		setActive(true);
		
		System.err.println("Create the Listener...");
		listener = new AutoSyncCommitListener();
		
		if (listener != null) {
			System.err.println("Finished creating the Listener.");
			//Add a Listener which reacts when a Project is opened.
			//Once this listener is called, create the TransactionManager object and setup
			//the listener for event changes.
			//
			Application.getInstance().getProjectsManager().addProjectListener(new ProjectEventListenerAdapter()
			{
				@Override
				public void projectOpened(Project project)
				{
					TransactionManager transactionManager = project.getRepository().getTransactionManager();
					listener.setTm(transactionManager);
					transactionManager.addTransactionCommitListener(listener);
				}

				@Override
				public void projectClosed(Project project)
				{
					project.getRepository().getTransactionManager().removeTransactionCommitListener(listener);
				}
		});
		}
		else
			System.err.println("Unable to create the Listener");


        //Start REST webservices
		//
		System.err.println("Finish AutoSyncPlugin initialization!");

	}
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
	public boolean isSupported()
	{
	//plugin can check here for specific conditions
	//if false is returned plugin is not loaded.
	return true;
	}
}

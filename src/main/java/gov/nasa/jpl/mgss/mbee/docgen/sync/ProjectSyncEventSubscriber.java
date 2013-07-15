package gov.nasa.jpl.mgss.mbee.docgen.sync;


import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.transaction.TransactionCommitListener;

/**
 * Subscribes a listener to comment change events within a project.
 * Create whenever a project is opened, and unsubscribe when the
 * project is closed.
 */
public class ProjectSyncEventSubscriber {
	private final Project project;
	private final TransactionCommitListener listener;

	/** Registers a listener for transaction commits. */
	public ProjectSyncEventSubscriber(Project project) {
		this.project = project;
		listener = new CommentChangeListener();	
	}

	/** Call when the project is opened. */
	public void subscribe() {
		project.getRepository().getTransactionManager()
				.addTransactionCommitListener(listener);
	}

	/** Called by listener when the project is closed. */
	public void unsubscribe() {
		project.getRepository().getTransactionManager()
				.removeTransactionCommitListener(listener);
	}

//	If you want a "smart listener", do it like this:
//	(but take care managing sessions and threads!)
//
//	SmartListenerConfig listenerConfig = new SmartListenerConfig();
//	listenerConfig.listenTo("ID");
//	listenerConfig.listenTo(PropertyNames.BODY);
//	project.getSmartEventSupport().registerConfig(
//			Comment.class,
//			Collections.singletonList(listenerConfig),
//			listener);

}

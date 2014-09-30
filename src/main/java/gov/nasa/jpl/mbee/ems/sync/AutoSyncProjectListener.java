package gov.nasa.jpl.mbee.ems.sync;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import gov.nasa.jpl.mbee.ems.ExportUtility;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectEventListenerAdapter;
import com.nomagic.magicdraw.uml.transaction.MDTransactionManager;
import com.nomagic.uml2.transaction.TransactionManager;

/*
 * This class is responsible for taking action when a project is opened.
 * This class does the following when instantiated:
 *   1. Create a transaction manager
 *   2. Create a TransactionCommitListener object
 *   3. Add the listener to the transaction manager object 
 *   4. Create a JMS topic and connection to that topic
 *   5. Store that connection so we keep track of the connections to JMS.
 *   
 */
public class AutoSyncProjectListener extends ProjectEventListenerAdapter {

	private static final String CONNECTION = "Connection";
	private static final String LISTENER = "AutoSyncCommitListener";
	private static final String SESSION = "Session";
	private static final String CONSUMER = "MessageConsumer";

	private static final String MSG_SELECTOR_PROJECT_ID = "projectId";
	private static final String MSG_SELECTOR_WS_ID = "workspace";

	public static void init(Project project) {
		Map<String, Object> projectInstances = ProjectListenerMapping.getInstance().get(project);

		// Check if the keywords are found in the current project. If so, it
		// indicates that this JMS subscriber has already been init'ed.
		//
		if (projectInstances.containsKey(CONNECTION) || projectInstances.containsKey(SESSION)
				|| projectInstances.containsKey(CONSUMER) || projectInstances.containsKey(LISTENER)) {
			// already inited?
			return;
		}
		AutoSyncCommitListener listener = new AutoSyncCommitListener();
		MDTransactionManager transactionManager = (MDTransactionManager) project.getRepository()
				.getTransactionManager();
		listener.setTm(transactionManager);
		// project.getRepository().setTransactionModelListener(new
		// AutoSyncModelListener());
		transactionManager.addTransactionCommitListenerIncludingUndoAndRedo(listener);
		projectInstances.put(LISTENER, listener);

		try {
			// should use tag value to get host
			//
			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://europaems-dev-staging-b:61616");

			Connection connection = connectionFactory.createConnection();
			// connection.setExceptionListener(this);
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			Destination destination = session.createTopic("master");

			// TODO:
			// JJS: Set the selector for the consumer:
			// What is the unique identifier which will
			// used to create the TOPIC which identifies
			// a particular project?
			//
			MessageConsumer consumer = session.createConsumer(destination);
			consumer.setMessageListener(new JMSMessageListener(project));
			connection.start();
			projectInstances.put(CONNECTION, connection);
			projectInstances.put(SESSION, session);
			projectInstances.put(CONSUMER, consumer);
			// Wait for a message
			/*
			 * Message message = consumer.receive(1000);
			 * 
			 * if (message instanceof TextMessage) { TextMessage textMessage =
			 * (TextMessage) message; String text = textMessage.getText();
			 * System.out.println("Received: " + text); } else {
			 * System.out.println("Received: " + message); }
			 */
			Application.getInstance().getGUILog().log("sync initiated");
		}
		catch (Exception e) {
			Application.getInstance().getGUILog().log("sync initialization failed");
		}
	}

	public static void initDurable(Project project) {
		Map<String, Object> projectInstances = ProjectListenerMapping.getInstance().get(project);
		String projectID = project.getID();
		String wsID = "jose";

		// Check if the keywords are found in the current project. If so, it
		// indicates that this JMS subscriber has already been init'ed.
		//
		if (projectInstances.containsKey(CONNECTION) || projectInstances.containsKey(SESSION)
				|| projectInstances.containsKey(CONSUMER) || projectInstances.containsKey(LISTENER)) {
			return;
		}
		AutoSyncCommitListener listener = new AutoSyncCommitListener();
		MDTransactionManager transactionManager = (MDTransactionManager) project.getRepository()
				.getTransactionManager();
		listener.setTm(transactionManager);

		transactionManager.addTransactionCommitListenerIncludingUndoAndRedo(listener);
		projectInstances.put(LISTENER, listener);

		try {
			// should use tag value to get host
			//
			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://ems-stg.jpl.nasa.gov:61616");

			Connection connection = connectionFactory.createConnection();
			connection.setClientID("jsalcedo");
			// connection.setExceptionListener(this);
			Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

			Topic topic = session.createTopic("master");

			String subscriberID = "jsalcedo";
			String messageSelector = constructSelectorString(projectID, wsID);
			;
			MessageConsumer consumer = session.createDurableSubscriber(topic, subscriberID, messageSelector, true);
			consumer.setMessageListener(new JMSMessageListener(project));
			connection.start();
			projectInstances.put(CONNECTION, connection);
			projectInstances.put(SESSION, session);
			projectInstances.put(CONSUMER, consumer);

			Application.getInstance().getGUILog().log("sync initiated");
		}
		catch (Exception e) {
			Application.getInstance().getGUILog().log("sync initialization failed: " + e.getMessage());
		}
	}

	public static String constructSelectorString(String projectID, String workspaceID) {
		StringBuilder selectorBuilder = new StringBuilder();

		selectorBuilder.append("(").append(MSG_SELECTOR_WS_ID).append("='").append(workspaceID).append("')");

		// selectorBuilder.append("(").append(MSG_SELECTOR_PROJECT_ID).append(" = '").append(projectID).append("\')")
		// .append(" AND ").append("(").append(MSG_SELECTOR_WS_ID).append(" = '").append(workspaceID).append("')");

		String outputMsgSelector = selectorBuilder.toString();
		selectorBuilder.delete(0, selectorBuilder.length());

		return outputMsgSelector;
	}

	public static void close(Project project) {
		Map<String, Object> projectInstances = ProjectListenerMapping.getInstance().get(project);
		AutoSyncCommitListener listener = (AutoSyncCommitListener) projectInstances.remove(LISTENER);
		if (listener != null)
			project.getRepository().getTransactionManager().removeTransactionCommitListener(listener);
		Connection connection = (Connection) projectInstances.remove(CONNECTION);
		Session session = (Session) projectInstances.remove(SESSION);
		MessageConsumer consumer = (MessageConsumer) projectInstances.remove(CONSUMER);
		try {
			if (consumer != null)
				consumer.close();
			if (session != null)
				session.close();
			if (connection != null)
				connection.close();
		}
		catch (Exception e) {

		}
		Application.getInstance().getGUILog().log("sync ended");
	}

	@Override
	public void projectOpened(Project project) {
		Map<String, Object> projectInstances = new HashMap<String, Object>();
		ProjectListenerMapping.getInstance().put(project, projectInstances);
		// init(project);
	}

	@Override
	public void projectClosed(Project project) {
		close(project);
		ProjectListenerMapping.getInstance().remove(project);
	}
}

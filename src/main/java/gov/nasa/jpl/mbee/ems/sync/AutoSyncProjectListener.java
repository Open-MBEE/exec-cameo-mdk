package gov.nasa.jpl.mbee.ems.sync;

import java.util.HashMap;
import java.util.Map;

import gov.nasa.jpl.mbee.ems.ExportUtility;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

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

	@Override
	public void projectOpened(Project project) {
		Map<String, Object> projectInstances = new HashMap<String, Object>();

		AutoSyncCommitListener listener = new AutoSyncCommitListener();
		MDTransactionManager transactionManager = (MDTransactionManager) project
				.getRepository().getTransactionManager();
		listener.setTm(transactionManager);
		// project.getRepository().setTransactionModelListener(new
		// AutoSyncModelListener());
		transactionManager
				.addTransactionCommitListenerIncludingUndoAndRedo(listener);

		projectInstances.put("AutoSyncCommitListener", listener);

		ProjectListenerMapping.getInstance().put(project, projectInstances);

		String url = ExportUtility.getUrl();
		if (url == null) {

		}

		try {
			// Create a ConnectionFactory
			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					"tcp://europaems-dev-staging-b:61616");

			// Create a Connection
			Connection connection = connectionFactory.createConnection();

			// connection.setExceptionListener(this);

			// Create a Session
			Session session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);

			// Create the destination (Topic or Queue)
			Destination destination = session.createTopic("master");

			// Create a MessageConsumer from the Session to the Topic or Queue
			MessageConsumer consumer = session.createConsumer(destination);

			// Wait for a message
			consumer.setMessageListener(new JMSMessageListener(project));
			connection.start();

			projectInstances.put("Connection", connection);
			projectInstances.put("Session", session);
			projectInstances.put("MessageConsumer", consumer);
			// Wait for a message
			/*
			 * Message message = consumer.receive(1000);
			 * 
			 * if (message instanceof TextMessage) { TextMessage textMessage =
			 * (TextMessage) message; String text = textMessage.getText();
			 * System.out.println("Received: " + text); } else {
			 * System.out.println("Received: " + message); }
			 */

		}
		catch (Exception e) {

		}

	}

	@Override
	public void projectClosed(Project project) {
		Map<String, ?> projectInstances = ProjectListenerMapping.getInstance()
				.get(project);
		AutoSyncCommitListener listener = (AutoSyncCommitListener) projectInstances
				.get("AutoSyncCommitListener");
		Connection connection = (Connection) projectInstances.get("Connection");
		Session session = (Session) projectInstances.get("Session");
		MessageConsumer consumer = (MessageConsumer) projectInstances
				.get("MessageConsumer");

		project.getRepository().getTransactionManager()
				.removeTransactionCommitListener(listener);
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
		ProjectListenerMapping.getInstance().remove(project);
	}
}

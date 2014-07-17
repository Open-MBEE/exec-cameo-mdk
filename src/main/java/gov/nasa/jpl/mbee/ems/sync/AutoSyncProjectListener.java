package gov.nasa.jpl.mbee.ems.sync;

import gov.nasa.jpl.mbee.ems.ExportUtility;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectEventListenerAdapter;
import com.nomagic.uml2.transaction.TransactionManager;

public class AutoSyncProjectListener extends ProjectEventListenerAdapter {

    private AutoSyncCommitListener listener;
    private Connection connection;
    private Session session;
    private MessageConsumer consumer;
    
    @Override
    public void projectOpened(Project project)
    {
        listener = new AutoSyncCommitListener();
        TransactionManager transactionManager = project.getRepository().getTransactionManager();
        listener.setTm(transactionManager);
        transactionManager.addTransactionCommitListener(listener);
        
        String url = ExportUtility.getUrl();
        if (url == null) {
            
        }
                
        try {
     // Create a ConnectionFactory
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://europaems-dev-staging-b:61616");

        // Create a Connection
        connection = connectionFactory.createConnection();

        //connection.setExceptionListener(this);

        // Create a Session
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Create the destination (Topic or Queue)
        Destination destination = session.createTopic("master");

        // Create a MessageConsumer from the Session to the Topic or Queue
        consumer = session.createConsumer(destination);

        // Wait for a message
        consumer.setMessageListener(new JMSMessageListener(project));
        connection.start();
     // Wait for a message
        /*Message message = consumer.receive(1000);

        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            String text = textMessage.getText();
            System.out.println("Received: " + text);
        } else {
            System.out.println("Received: " + message);
        }*/
        //consumer.close();
        //session.close();
        //connection.close();
        } catch (Exception e) {
            
        }
    
    }

    @Override
    public void projectClosed(Project project)
    {
        project.getRepository().getTransactionManager().removeTransactionCommitListener(listener);
        try {
        if (consumer != null)
            consumer.close();
        if (session != null)
            session.close();
        if (connection != null)
            connection.close();
        } catch (Exception e) {
            
        }
    }
}

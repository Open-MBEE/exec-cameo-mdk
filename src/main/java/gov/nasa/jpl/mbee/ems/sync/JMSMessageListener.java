package gov.nasa.jpl.mbee.ems.sync;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class JMSMessageListener implements MessageListener {

    @Override
    public void onMessage(Message msg) {
        TextMessage message = (TextMessage)msg;
        
    }

}

package com.athena.dolly.test;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
 

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import com.athena.dolly.web.messaging.FileDto;
 
public class AmqConsumer {
 
    public static void main(String[] args) throws JMSException {
         
        //String brokerURL = ActiveMQConnection.DEFAULT_BROKER_URL;
    	String brokerURL = "tcp://54.238.120.65:61616";
        String queueName = "storage-sync";
        boolean transacted = false;
         
        Connection connection = null;
        try {
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerURL);
            connection = connectionFactory.createConnection();
            connection.start();
 
            Session session = connection.createSession(transacted, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue(queueName);
 
             
            MessageConsumer consumer = session.createConsumer(destination);
            boolean loop = true;
            while(true && loop) {
                 
                // consumer.receive(); 시에는 메시지가 올때까지 대기 상태를 가진다.
                // Message message = consumer.receive();
 
                Message message = consumer.receive(5 * 1000);
                loop = !(message == null);
     
                if (message instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) message;
                    System.out.println(textMessage.getText());
                     
                } else if (message instanceof MapMessage) {
                    MapMessage mapMessage = (MapMessage) message;
                    System.out.println(mapMessage.getString("name") + "," + mapMessage.getInt("age"));
                     
                } else if (message instanceof BytesMessage) {
                    BytesMessage bytesMessage = (BytesMessage) message;
                    byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
                    bytesMessage.readBytes(bytes);
                    System.out.println(new String(bytes, "UTF-8"));
                     
                } else if (message instanceof ObjectMessage) {
                    ObjectMessage objectMessage = (ObjectMessage) message;
                    FileDto p = (FileDto) objectMessage.getObject();
                    System.out.println(p);
                     
                }
            }
             
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) try { connection.close(); } catch (Exception e) {}
        }
    }
}
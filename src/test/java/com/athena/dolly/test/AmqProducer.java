package com.athena.dolly.test;

import java.io.UnsupportedEncodingException;

import javax.jms.*;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.athena.dolly.web.messaging.FileDto;
 
public class AmqProducer {
     
    public static void main(String[] args) {
         
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
             
             
            MessageProducer producer = session.createProducer(destination);
 
            TextMessage message = session.createTextMessage("텍스트 메시지");
            producer.send(message);
             
            MapMessage mapMessage = session.createMapMessage();
            mapMessage.setString("name", "유저1");
            mapMessage.setInt("age", 10);
            producer.send(mapMessage);
             
            BytesMessage bytesMessage = session.createBytesMessage();
            bytesMessage.writeBytes("바이트 메시지".getBytes("UTF-8"));
            producer.send(bytesMessage);
             
            ObjectMessage objMessage = session.createObjectMessage(new FileDto());
            producer.send(objMessage);
 
        } catch (UnsupportedEncodingException | JMSException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) try { connection.close(); } catch (Exception e) {}
        }
    }
     
}
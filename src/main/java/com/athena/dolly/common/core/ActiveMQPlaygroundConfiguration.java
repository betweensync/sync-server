/* 
 * Athena Dolly Project
 * 
 * Copyright (C) 2013 Open Source Consulting, Inc. All rights reserved by Open Source Consulting, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * Revision History
 * Author			Date				Description
 * ---------------	----------------	------------
 * Ji-Woong Choi	2013. 12. 13.		First Draft.
 */
package com.athena.dolly.common.core;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.MessageListener;

/**
 * <p>
 *   <ul>
 *     <li>Use {@link org.springframework.jms.connection.CachingConnectionFactory}</li>
 *     <li>All calls on the JmsTemplate are synchronous which means the calling thread will block until the method returns.</li>
 *     <li>So use {@link org.springframework.jms.listener.DefaultMessageListenerContainer}</li>
 *   </ul>
 * </p>
 * <p>
 *   <a href="http://codedependents.com/2009/10/16/efficient-lightweight-jms-with-spring-and-activemq/">Efficient Lightweight JMS with Spring and ActiveMQ</a>
 *   <a href="http://activemq.apache.org/spring-support.html">ActiveMQ Spring Support</a>
 * </p>
 */
@Configuration
public class ActiveMQPlaygroundConfiguration {

    @Value("#{contextProperties['jms.user.name']}")
    private String userName;

    @Value("#{contextProperties['jms.password']}")
    private String password;

    @Value("#{contextProperties['jms.broker.url']}")
    private String brokerUrl;

    @Value("#{contextProperties['jms.session.cache.size']}")
    private int sessionCacheSize;

    @Value("#{contextProperties['jms.queue.name']}")
    private String queueName;

    @Value("#{contextProperties['jms.listener.container.concurrency']}")
    private String listenerContainerConcurrency;

    @Bean
    public QueueSender queueSender() {
        return new QueueSender(jmsTemplate(), queueName);
    }

    @Bean
    public AbstractMessageListenerContainer listenerContainer() {
        final DefaultMessageListenerContainer defaultMessageListenerContainer = new DefaultMessageListenerContainer();
        defaultMessageListenerContainer.setConnectionFactory(connectionFactory());
        defaultMessageListenerContainer.setConcurrency(listenerContainerConcurrency);
        defaultMessageListenerContainer.setDestinationName(queueName);
        defaultMessageListenerContainer.setMessageListener(queueListener());
        return defaultMessageListenerContainer;
    }

    private JmsTemplate jmsTemplate() {
        return new JmsTemplate(connectionFactory());
    }

    private ConnectionFactory connectionFactory() {
        final CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(amqConnectionFactory());
        cachingConnectionFactory.setExceptionListener(jmsExceptionListener());
        cachingConnectionFactory.setSessionCacheSize(sessionCacheSize);
        return cachingConnectionFactory;
    }

    private ConnectionFactory amqConnectionFactory() {
        return new ActiveMQConnectionFactory(userName, password, brokerUrl);
    }

    private ExceptionListener jmsExceptionListener() {
        return new JmsExceptionListener();
    }

    private MessageListener queueListener() {
        return new QueueListener();
    }

}
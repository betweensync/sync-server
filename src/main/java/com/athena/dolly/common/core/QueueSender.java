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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

public class QueueSender {

    private static final Logger logger = LoggerFactory.getLogger(QueueSender.class);

    private final JmsTemplate jmsTemplate;

    private final String queueName;

    public QueueSender(final JmsTemplate jmsTemplate, String queueName) {
        this.jmsTemplate = jmsTemplate;
        this.queueName = queueName;
    }

    public void send(final String message) {
        logger.debug("start sending \"{}\"", message);
        jmsTemplate.convertAndSend(queueName, message);
        logger.debug("end sending \"{}\"", message);
    }
}
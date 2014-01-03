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
package com.athena.dolly.common.provider;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * <pre>
 * 
 * </pre>
 * @author Ji-Woong Choi(jchoi@osci.kr)
 * @version 1.0
 */
@Service
public class ApplicationContextProvider implements ApplicationContextAware {  
    
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {  
        AppContext.setApplicationContext(ctx);  
    }  
}//end of ApplicationContextProvider.java
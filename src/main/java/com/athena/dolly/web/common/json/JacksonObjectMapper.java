/* 
 * Athena Project - Server Provisioning Engine for IDC or Cloud
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
package com.athena.dolly.web.common.json;

import java.text.SimpleDateFormat;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;

/**
 * <pre>
 * DTO Date type 을 json 문자열로 변환시 사용할 포맷 지정.
 * - 주의: Controller 에서 @ResponseBody 로 리턴될때 적용됨.
 * </pre>
 * @author Bong-Jin Kwon
 * @author Ji-Woong Choi(jchoi@osci.kr) 
 * @version 1.0
 */
public class JacksonObjectMapper extends ObjectMapper {

	/**
	 * <pre>
	 * 
	 * </pre>
	 */
	public JacksonObjectMapper() {
		super.configure(Feature.WRITE_DATES_AS_TIMESTAMPS, false);
		setDateFormat(new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z"));
	}

}
//end of JacksonObjectMapper.java
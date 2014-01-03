/* 
 * Athena Peacock Project - Server Provisioning Engine for IDC or Cloud
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
package com.athena.dolly.web.messaging;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.athena.dolly.web.common.model.GridJsonResponse;
import com.athena.dolly.web.common.model.SimpleJsonResponse;



/**
 * <pre>
 * This is a controller for RHEV-M API.
 * RHEV-M API를 이용한 작업을 수행하는 컨트롤러
 * </pre>
 * @author Ji-Woong Choi
 * @version 1.0
 */
@Controller
@RequestMapping("/mq")
public class MessagingController {

    protected final Logger logger = LoggerFactory.getLogger(MessagingController.class);
    

    
	@Inject
	@Named("messagingService")
	private MessagingService messagingService;
	
	@RequestMapping("/send")
	public @ResponseBody SimpleJsonResponse retrieve(SimpleJsonResponse jsonRes, @RequestParam String message) throws Exception {
				
		jsonRes.setData(messagingService.send(message));
		return jsonRes;
	}
		
	/**
	 * <pre>
	 * 지정된 RHEV-M(rhevmId)에 해당하는 Virtual Machine 목록을 조회
	 * </pre>
	 * @param jsonRes
	 * @param machineId
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/list")
	public @ResponseBody GridJsonResponse list(GridJsonResponse jsonRes) throws Exception {
		
		List<FileInfoDto> fileList = new ArrayList<FileInfoDto>();
		FileInfoDto info = new FileInfoDto();
		info.setName("CaPubs");
		info.setLength(1234);
		
		FileInfoDto info2 = new FileInfoDto();
		info2.setName("signCert.der");
		info2.setLength(364);
		
		fileList.add(info);
		fileList.add(info2);
		
		FileDto dto = new FileDto();
		dto.setDir("C:/Private");
		dto.setFileList(fileList);
		
		FileDto dto2 = new FileDto();
		dto2.setDir("C:/Private/NPKI");
		dto2.setFileList(fileList);
		
		List<FileDto> dtoList = new ArrayList<FileDto>();
		dtoList.add(dto);
		dtoList.add(dto2);
		
		jsonRes.setList(dtoList);
		return jsonRes;
	}
	
}
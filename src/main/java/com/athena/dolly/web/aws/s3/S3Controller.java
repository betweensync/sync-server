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
package com.athena.dolly.web.aws.s3;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
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
@RequestMapping("/aws/s3")
public class S3Controller {

    protected final Logger logger = LoggerFactory.getLogger(S3Controller.class);
    
	@Inject
	@Named("s3Service")
	private S3Service s3Service;
		
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
		// 아래는 향후 Multi RHEV-M을 컨트롤할 때 rhevmId를 입력받아 처리하도록 함
		//Assert.isTrue(!StringUtils.isEmpty(vms.getRhevmId()), "rhevmId must not be null.");
		
		jsonRes.setList(s3Service.listBuckets());
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
	@RequestMapping("/create")
	public @ResponseBody SimpleJsonResponse list(SimpleJsonResponse jsonRes) throws Exception {
		// 아래는 향후 Multi RHEV-M을 컨트롤할 때 rhevmId를 입력받아 처리하도록 함
		//Assert.isTrue(!StringUtils.isEmpty(vms.getRhevmId()), "rhevmId must not be null.");
		s3Service.putObject("ienvyou", "temp/logo.png", "C:/OpenSourceConsulting/OSC-Logo.png");
		
		jsonRes.setData("Create Data Success");
		return jsonRes;
	}
	
}
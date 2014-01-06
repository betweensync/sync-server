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

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.amazonaws.services.s3.model.S3ObjectSummary;
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
	 * List objects specified bucket
	 * </pre>
	 * @param jsonRes
	 * @param bucketName
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/list")
	public @ResponseBody GridJsonResponse list(GridJsonResponse jsonRes, @RequestParam String bucketName, String key) throws Exception {
		Assert.isTrue(!StringUtils.isEmpty(bucketName), "bucketName must not be null.");
	
		logger.debug("Bucket Name: " + bucketName + ", key: " + key);
		
		List<S3Dto> list  = s3Service.listBucket(bucketName, key);
		
		
		jsonRes.setTotal(list.size());
		jsonRes.setList(list);
		return jsonRes;
	}
	
	
	
	public static void main(String [] args) throws Exception {
		
		//String [] keys = {"directory/", "directory/browse.png", "peacock.mp4", "temp/logo.png", "temp/subdir/", "temp/subdir/index.png"};
		//String [] keys = {"directory/", "directory/browse.png"};
		String [] keys = {"temp/logo.png", "temp/subdir/", "temp/subdir/index.png"};
		
		Map<String, S3Dto> map = new HashMap<String, S3Dto>();
		for( String key: keys) {
			//S3Dto dto = test(null, key);
			//S3Dto dto = test("directory", key);
			S3Dto dto = test("temp", key);
			if( dto != null) map.put(dto.getKey(), dto);
			
		}
		
		System.out.println(map);
	}
	
	public static S3Dto test(String prefix, String key) {
		String delimiter = "/";
	    if (prefix != null && !prefix.endsWith(delimiter)) {
	        prefix += delimiter;
	    }
	    S3Dto dto = new S3Dto();
	    
	    if( prefix == null ) {  //  root
	    	int pos = key.indexOf("/");
	    	System.out.println("Key: " + key + "," + pos);
	    	
	    	if( pos == -1) { // file
	    		dto.setDataType("file");
	    	} else { // folder
	    		key = key.substring(0, pos);
	    		dto.setDataType("folder");
	    	}
    		dto.setKey(key);
	    	
	    } else { // listing subdirectories
	    	int pos = key.lastIndexOf("/");
	    	System.out.println("Before Key: " + key + "," + pos);
	    	String current  = key.substring(pos+1);
	    	
	    	if( current.equals("")) { // Directory
	    		current = key.substring(0, pos);
	    		System.out.println("Directory: " + current );
	    		dto.setKey("..");
	    		dto.setDataType("folder");
	    		dto.setParent(current);
	    	} else { // file
	    	//	if( current.)
	    		dto.setKey(current);
	    		dto.setDataType("file");
	    		System.out.println("FileName: " + current );
	    	}
	    	
	    	//System.out.println("After Key: " + key + "," + pos);
	    }

		
		return dto;
		
//		
//		int pos = key.lastIndexOf("/");
//		if( pos == -1 ) { // root file
//			
//		} else { // This is directory or file. Apply filter
//			current = key.substring(0, pos);
//			key = key.substring(pos+1);
//			
//			if( key.equals("")) {
//				key = "..";
//				dataType = "folder";
//			}
//			
//			if( parent.length() != 0) parent = current.substring(0, current.lastIndexOf("/"));
//		}
//			
//		System.out.println(key.equals(""));
//		System.out.println("Key: " + key);
//		System.out.println("Current:" + current);
//		System.out.println("Parent:" + parent);
//		System.out.println("Type: " + dataType);
	}
	
	@RequestMapping("/get")
	public @ResponseBody SimpleJsonResponse get(SimpleJsonResponse jsonRes, @RequestParam String bucketName, @RequestParam String key) throws Exception {
		Assert.isTrue(!StringUtils.isEmpty(bucketName), "bucketName must not be null.");
		Assert.isTrue(!StringUtils.isEmpty(key), "key must not be null.");
		
		logger.info("Downloading an object");
        s3Service.getObject(bucketName, key);
		
		jsonRes.setData("Download File Success");
		return jsonRes;
	}
	
	
	/**
	 * <pre>
	 * Change Object ACL to public access
	 * </pre>
	 * @param jsonRes
	 * @param bucketName
	 * @param key
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/acl/public")
	public @ResponseBody SimpleJsonResponse aclPublic(SimpleJsonResponse jsonRes, @RequestParam String bucketName, @RequestParam String key) throws Exception {
		Assert.isTrue(!StringUtils.isEmpty(bucketName), "bucketName must not be null.");
		Assert.isTrue(!StringUtils.isEmpty(key), "key must not be null.");
		s3Service.changeAclToPublic(bucketName, key);
		
		jsonRes.setData("Change ACL Success for " + key);
		return jsonRes;
	}
	
	/**
	 * <pre>
	 * Change Object ACL to private access
	 * </pre>
	 * @param jsonRes
	 * @param bucketName
	 * @param key
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/acl/private")
	public @ResponseBody SimpleJsonResponse aclPrivate(SimpleJsonResponse jsonRes, @RequestParam String bucketName, @RequestParam String key) throws Exception {
		Assert.isTrue(!StringUtils.isEmpty(bucketName), "bucketName must not be null.");
		Assert.isTrue(!StringUtils.isEmpty(key), "key must not be null.");
		s3Service.changeAclToPrivate(bucketName, key);
		
		jsonRes.setData("Change ACL Success for " + key);
		return jsonRes;
	}
	
	/**
	 * Generates a signed download URL for key(file) that will work for 1 hour.
	 * @param jsonRes
	 * @param bucketName
	 * @param key
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/presigned")
	public @ResponseBody SimpleJsonResponse presignedUrl(SimpleJsonResponse jsonRes, @RequestParam String bucketName, @RequestParam String key) throws Exception {
		Assert.isTrue(!StringUtils.isEmpty(bucketName), "bucketName must not be null.");
		Assert.isTrue(!StringUtils.isEmpty(key), "key must not be null.");
		URL url = s3Service.presignedUrl(bucketName, key);
		
		jsonRes.setData(url);
		jsonRes.setMsg("Presigned URL creating success");
		return jsonRes;
	}
	
	

	
}
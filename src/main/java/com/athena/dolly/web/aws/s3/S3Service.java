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
 * Author     Date        Description
 * ---------------  ----------------  ------------
 * Ji-Woong Choi  2013. 12. 13.   First Draft.
 */
package com.athena.dolly.web.aws.s3;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;

@Service("s3Service")
public class S3Service {
	
	protected final Logger logger = LoggerFactory.getLogger(S3Service.class);

	@Value("#{contextProperties['temp.dir']}")
	private String tempDir;
	
	@Value("#{contextProperties['aws.access.key']}")
	private String accessKey;

	@Value("#{contextProperties['aws.secret.key']}")
	private String secretKey;

	private AmazonS3 s3;

	@PostConstruct
	public void init() {
		AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
		s3 = new AmazonS3Client(credentials);
	}

	/**
	 * Retrieve list of buckets
	 * 
	 * @return Bucket List
	 */
	public List<Bucket> listBuckets() {
		return s3.listBuckets();
	}
	
	/**
	 * Retrieve object summaries in specific bucket
	 * @param bucketName
	 * @return
	 */
	public List<S3ObjectSummary> listBucket(String bucketName) {
		ObjectListing objectListing = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName));
	    System.out.println("★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★");
	    System.out.println(objectListing.getCommonPrefixes());
		return objectListing.getObjectSummaries();
	}
	
	/**
	 * Retrieve object summaries in specific bucket
	 * @param bucketName
	 * @return
	 */
	public List<S3Dto> listBucket(String bucketName, String prefix) {
	    ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
	            .withBucketName(bucketName).withPrefix(prefix)
	            .withDelimiter(null);
	    ObjectListing objectListing = s3.listObjects(listObjectsRequest);
	    
	    List<S3Dto> list = new ArrayList<S3Dto>();
		
		for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
		    logger.info(" - " + objectSummary.getKey() + "  " +
		                       "(size = " + objectSummary.getSize() + ")");
		    list.add(makeDto(bucketName, objectSummary));
		}
		
	    return list;
	}

	/**
	 * Create a new bucket
	 * 
	 * @param bucketName
	 */
	public void createBucket(String bucketName) {
		s3.createBucket(bucketName);
	}

	/**
	 * Delete specified bucket
	 * 
	 * @param bucketName
	 */
	public void deleteBucket(String bucketName) {
		s3.deleteBucket(bucketName);
	}

	/**
	 * File upload to s3 using bucket name and key. The key is same as directory
	 * 
	 * @param bucketName
	 * @param key
	 * @param fileName
	 */
	public void putObject(String bucketName, String key, String fileName) {
		File file = new File(fileName);
		s3.putObject(bucketName, key, file);
	}
	

	/**
	 * Getting file from s3 using key. File will be saved in temporary directory you configured
	 * @param bucketName
	 * @param key
	 */
	public File getObject(String bucketName, String key) {
		
		String tempFileName = tempDir + File.separator + key.substring(key.lastIndexOf("/") + 1);
		File tempFile = new File(tempFileName);
		ObjectMetadata object = s3.getObject(new GetObjectRequest(bucketName, key),
											 tempFile
						                    );
        return tempFile;

	}
	
	/**
	 * Change object ACL to public read
	 * @param bucketName
	 * @param key
	 */
	public void changeAclToPublic(String bucketName, String key) {
		s3.setObjectAcl(bucketName, key, CannedAccessControlList.PublicRead);
	}
	
	/**
	 * Change object ACL to private
	 * @param bucketName
	 * @param key
	 */
	public void changeAclToPrivate(String bucketName, String key) {
		s3.setObjectAcl(bucketName, key, CannedAccessControlList.Private);
	}
	
	/**
	 *  This generates a signed download URL for key(file) that will work for 1 hour.
	 * @param bucketName
	 * @param key
	 * @return
	 */
	public URL presignedUrl(String bucketName, String key) {
		GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, key);
		URL url = s3.generatePresignedUrl(request);
		return url;
	}
	

	private S3Dto makeDto(String bucketName, S3ObjectSummary objectSummary) {
		S3Dto dto = new S3Dto();
		
		// Default value setting
		dto.setBucketName(bucketName);
	    dto.setLastModified(date2String(objectSummary.getLastModified(), "yyyy/MM/dd a KK:mm"));
	    dto.setSize((objectSummary.getSize() / 1024) + "K");
	    dto.setDataType(checkDataType(objectSummary.getKey()));
		
		// Caculate position
		
		String current = "";
		String dataType = "file";
		String parent = "";
		
		String key = objectSummary.getKey();
		
		dto.setUrl(presignedUrl(bucketName, key).toString());
		// 1. lastIndexOf("/") == -1 is root directory's file
		int pos = key.lastIndexOf("/");
		if( pos == -1 ) { // root file
			
		} else { // This is directory or file. Apply filter
			current = key.substring(0, pos);
			key = key.substring(pos+1);
			
			if( key.equals("")) {
				key = "..";
				dataType = "folder";
			}
			if( parent.length() != 0) parent = current.substring(0, current.lastIndexOf("/"));
		}
		
		dto.setKey(key);
		dto.setDataType(dataType);
		dto.setParent(parent);

		// 2. lastIndexOf("/") == 
		return dto;
	}
	
	private String sanitizeKeyName(String key) {
		int index = key.indexOf("/");
		if( index != -1) {
			// This if folder
			return key.substring(0, index);
		} else {
			return key;
		}
	}
	
	private String checkDataType(String key) {
		int index = key.indexOf("/");
		if( index != -1) {
			return "folder";
		} else {
			return "file";
		}
		
	}
	
	private String date2String( Date d, String format ) {
		SimpleDateFormat sdf = new SimpleDateFormat( format );
		return sdf.format( d );
	}
}

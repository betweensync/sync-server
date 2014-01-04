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
		return objectListing.getObjectSummaries();
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
	

	
}

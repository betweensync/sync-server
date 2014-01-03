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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;

@Service("s3Service")
public class S3Service {

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
	 * Creates a temporary file with text data to demonstrate uploading a file
	 * to Amazon S3
	 * 
	 * @return A newly created temporary file with text data.
	 * 
	 * @throws IOException
	 */
	private static File createSampleFile() throws IOException {
		File file = File.createTempFile("aws-java-sdk-", ".txt");
		file.deleteOnExit();

		Writer writer = new OutputStreamWriter(new FileOutputStream(file));
		writer.write("abcdefghijklmnopqrstuvwxyz\n");
		writer.write("01234567890112345678901234\n");
		writer.write("!@#$%^&*()-=[]{};':',.<>/?\n");
		writer.write("01234567890112345678901234\n");
		writer.write("abcdefghijklmnopqrstuvwxyz\n");
		writer.close();

		return file;
	}
}

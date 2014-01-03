package com.athena.dolly.cloudant.test;

import java.util.HashMap;
import java.util.Map;

import org.ektorp.CouchDbConnector;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbInstance;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ChangesFeeder {
	static String url = System.getProperty("url", "https://jerryj3.cloudant.com");

	
	public static void main(String args[]) {
		System.out.println("url = " + url);

		ChangesFeeder test = new ChangesFeeder();
		test.feed();
	}
	
	@Test
	public void feed() {
		try {
			HttpClient httpClient = new StdHttpClient.Builder().url(url)
					.username("ighlyesesedisentstoldneg")
					.password("cOlugjOsPXtkHyR1SPvNDYME")
					.connectionTimeout(5000)
					.socketTimeout(30000).build();

			StdCouchDbInstance dbInst = new StdCouchDbInstance(httpClient);
			
			System.out.println("*** Database connection is established");
			
			CouchDbConnector conn = dbInst.createConnector("a_samsung_file", true);
			
			ObjectMapper om = new ObjectMapper();
			
			while(true) {
				Map<String, Object> sFile = new HashMap<String, Object>();
				sFile.put("absolutePath", "/stroage/media/a.jpg");
				sFile.put("isDirectory", false);
				sFile.put("lastModified", System.currentTimeMillis());
				sFile.put("name", "a.jpg");
				conn.create(sFile);
				System.out.println(sFile);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				sFile.put("absolutePath", "/stroage/media/b.jpg");
				sFile.put("lastModified", System.currentTimeMillis());
				sFile.put("name", "b.jpg");
				conn.update(sFile);
				System.out.println(sFile);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				conn.delete(sFile);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
}

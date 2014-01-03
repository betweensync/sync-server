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
 * Jerry Jung		2013. 12. 30.		First Draft.
 */
package com.athena.dolly.cloudant;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ektorp.CouchDbConnector;
import org.ektorp.ViewQuery;
import org.ektorp.ViewResult;
import org.ektorp.ViewResult.Row;
import org.ektorp.changes.ChangesCommand;
import org.ektorp.changes.ChangesFeed;
import org.ektorp.changes.DocumentChange;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbInstance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CloudantEventListener {
	static String url = System.getProperty("url", "https://jerryj3.cloudant.com");
	CouchDbConnector conn;
	CouchDbConnector connSeq;
	ChangesCommand cmd;	
	
	public static void main(String args[]) {
		CloudantEventListener listener = new CloudantEventListener();
		listener.init();
		
		SFile sFile = listener.getSFileByAbsolutePath("/stroage/media/a.jpg");
		System.out.println("Query by absolutePath: " + sFile);
		
		while(true) {
			List<SFile> sFiles = listener.waitForMeta();
		}
	}
	
	public void init() {
		try {
			HttpClient httpClient = new StdHttpClient.Builder().url(url)
					.username("ighlyesesedisentstoldneg")
					.password("cOlugjOsPXtkHyR1SPvNDYME")
					.connectionTimeout(5000)
					.socketTimeout(30000).build();

			StdCouchDbInstance dbInst = new StdCouchDbInstance(httpClient);
			
			System.out.println("*** Database connection is established");
			
			conn = dbInst.createConnector("a_samsung_file", true);
			connSeq = dbInst.createConnector("a_seq", true);
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<SFile> waitForMeta() {
		List<SFile> result = new ArrayList<SFile>();

		JsonNode a_samsung_file = (JsonNode)connSeq.find(JsonNode.class, "a_samsung_file");
		if(a_samsung_file != null) {
			System.out.println("Since: " + a_samsung_file.get("seq").textValue());
			cmd = new ChangesCommand.Builder().since(a_samsung_file.get("seq").textValue()).includeDocs(true).build();
		}
		else {
			cmd = new ChangesCommand.Builder().includeDocs(true).build();
		}

		//	Event Process
		ChangesFeed feed = conn.changesFeed(cmd);
		while(feed.isAlive()) {
			DocumentChange change = null;
			try {
				System.out.println("Waiting...");
				
				change = feed.next();
//					change = feed.next(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(change != null) {
				DocumentChange newChange = null;
				do {
					String docId = change.getId();
					JsonNode changedDoc = change.getDocAsNode();
					ObjectMapper om = new ObjectMapper();
					SFile aFile = om.convertValue(changedDoc, SFile.class);
					result.add(aFile);

					System.out.println(changedDoc + ", seq=" + change.getStringSequence());
					try {
						newChange = feed.poll();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				while(newChange != null);

				//	ÃßÃâÇÑ µ¥ÀÌÅÍÀÇ ÃÖÁ¾ seq ¹øÈ£¸¦ ¾÷µ¥ÀÌÆ®
				if(a_samsung_file == null) {
					ObjectMapper om = new ObjectMapper();
					a_samsung_file = om.createObjectNode();
					((ObjectNode)a_samsung_file).put("_id", "a_samsung_file");
					((ObjectNode)a_samsung_file).put("seq", change.getStringSequence());
					connSeq.create(a_samsung_file);
				}
				else {
					((ObjectNode)a_samsung_file).put("seq", change.getStringSequence());
					connSeq.update(a_samsung_file);
				}
			}
		}
		
		return result;
	}
	
	public SFile getSFileByAbsolutePath(String absolutePath) {
		ObjectMapper om = new ObjectMapper();
		ViewQuery query = new ViewQuery().designDocId("_design/index").viewName("absPathView").key(absolutePath).includeDocs(true).limit(1).staleOk(false);
		ViewResult result = conn.queryView(query);
		Iterator<Row> it = result.iterator();
		while(it.hasNext()) {
			Row row = it.next();
//			System.out.println(row);
//			System.out.println(row.getDocAsNode());
			SFile sFile = om.convertValue(row.getDocAsNode(), SFile.class);
			return sFile;
		}
		
		return null;
	}
}

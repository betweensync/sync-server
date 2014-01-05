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
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.ektorp.CouchDbConnector;
import org.ektorp.changes.ChangesCommand;
import org.ektorp.changes.ChangesFeed;
import org.ektorp.changes.DocumentChange;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.athena.dolly.common.provider.AppContext;
import com.athena.dolly.websocket.server.WebSocketServerHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class ChangesEventListener {

	protected final Logger logger = LoggerFactory.getLogger(ChangesEventListener.class);

	@Value("#{contextProperties['cloudant.url']}")
	private String url;

	@Value("#{contextProperties['cloudant.username']}")
	private String username;

	@Value("#{contextProperties['cloudant.password']}")
	private String password;

	public static void main(String args[]) {
		ChangesEventListener test = new ChangesEventListener();
		test.attachment();
	}

	@PostConstruct
	public void attachment() {

		new Thread() {
			@Override
			public void run() {
				try {
					HttpClient httpClient = new StdHttpClient.Builder()
							.url(url).username(username).password(password)
							.connectionTimeout(5000).socketTimeout(30000)
							.build();

					StdCouchDbInstance dbInst = new StdCouchDbInstance(
							httpClient);

					logger.info("=============================================================");
					logger.info("Cloudant Database connection is established");
					logger.info("=============================================================");

					CouchDbConnector conn = dbInst.createConnector("a_samsung_file", true);
					CouchDbConnector connSeq = dbInst.createConnector("a_seq", true);

					JsonNode a_samsung_file = (JsonNode) connSeq.find(JsonNode.class, "a_samsung_file");
					ChangesCommand cmd = null;
					if (a_samsung_file != null) {
						logger.info("Since: " + a_samsung_file.get("seq").textValue());
						cmd = new ChangesCommand.Builder()
								.since(a_samsung_file.get("seq").textValue())
								.includeDocs(true).build();
					} else {
						cmd = new ChangesCommand.Builder().includeDocs(true).build();
					}

					int type = 2;

					if (type == 1) {
						// Capture changed document
						List<DocumentChange> changes = conn.changes(cmd);
						logger.debug(cmd.toString());

						for (DocumentChange change : changes) {
							logger.info(change.getId() + ", "
									+ change.getSequence());
						}
					} else if (type == 2) {
						// Event Catch
						ChangesFeed feed = conn.changesFeed(cmd);
						while (feed.isAlive()) {
							DocumentChange change = null;
							try {
								logger.debug("Waiting event from cloudant database");

								// Retry every 5 seconds
								change = feed.next(5, TimeUnit.SECONDS);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							if (change != null) {
								// Compare document is changed
								DocumentChange newChange = null;
								do {
									String docId = change.getId();
									JsonNode changedDoc = change.getDocAsNode();
									logger.info(changedDoc + ", seq=" + change.getStringSequence());
									
									/**********************************************************/
									// Send signal to client with Netty Web Socket
									/**********************************************************/
									WebSocketServerHandler handler = AppContext.getBean(WebSocketServerHandler.class);
									handler.sendMessageToClient(changedDoc);
									
									try {
										newChange = feed.poll();
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								} while (newChange != null);

								if (a_samsung_file == null) {
									ObjectMapper om = new ObjectMapper();
									a_samsung_file = om.createObjectNode();
									((ObjectNode) a_samsung_file).put("_id", "a_samsung_file");
									((ObjectNode) a_samsung_file).put("seq", change.getStringSequence());
									connSeq.create(a_samsung_file);
								} else {
									((ObjectNode) a_samsung_file).put("seq", change.getStringSequence());
									connSeq.update(a_samsung_file);
								}
							}
						}
						// end of if
						feed.cancel();
					} else if (type == 3) {
						ChangesFeed feed = conn.changesFeed(cmd);
						logger.debug("Waiting...");
						while (feed.isAlive()) {

						}
					}

				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
}

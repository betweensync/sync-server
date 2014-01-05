/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.athena.dolly.websocket.server;

/**
 *
 * @author jlee
 * @date Jan 5th, 2013
 * 
 */
public class WebSocketResource {
    public static final String OPCODE_CONNECT = "CONNECT";
    public static final String OPCODE_DISCONNECT = "DISCONNECT";
    public static final String OPCODE_TRANSFER_INSERT = "INSERT";
    public static final String OPCODE_TRANSFER_DELETE = "DELETE";
    public static final String JSON_USERID_KEY = "userid";
    public static final String JSON_OPCODE_KEY = "opcode";
}
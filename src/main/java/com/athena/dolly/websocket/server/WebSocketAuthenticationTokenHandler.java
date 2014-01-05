/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.athena.dolly.websocket.server;

/**
 *
 * @author jlee
 */
public class WebSocketAuthenticationTokenHandler {
    public WebSocketAuthenticationTokenHandler() {   
    }
    
    public String ticketGenerate(String remoteAddr, String userid, String macAddress) {
        return "";
    }
    
    // Todo : IP Spoofing??
    public boolean isValidTicket(String ticket) {
        return false;
    }
}

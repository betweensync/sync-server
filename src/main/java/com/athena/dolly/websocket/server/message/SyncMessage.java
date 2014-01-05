/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.athena.dolly.websocket.server.message;

import java.util.ArrayList;

/**
 *
 * @author jlee
 */
public class SyncMessage {
    private String userid;
    private String opcode;
    private ArrayList<String> sFileList;
    private String ret;

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getOpcode() {
        return opcode;
    }

    public void setOpcode(String opcode) {
        this.opcode = opcode;
    }

    public ArrayList<String> getsFileList() {
        return sFileList;
    }

    public void setsFileList(ArrayList<String> sFileList) {
        this.sFileList = sFileList;
    }

    public String getRet() {
        return ret;
    }

    public void setRet(String ret) {
        this.ret = ret;
    }
    
    @Override
    public String toString() {
        return "ClientMessage{" + "userid=" + userid + ", opcode=" + opcode + ", sFileList=" + sFileList + ", ret=" + ret + '}';
    }
}
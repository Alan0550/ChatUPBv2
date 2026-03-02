/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upb.chatupb_v2.model.entities;

import edu.upb.chatupb_v2.model.repository.Model;

import java.io.Serializable;

public class Contact implements Serializable, Model {
    public static final String ME_CODE = "af3bc20a-766c-4cd4-813d-b1067a01fa9a";


    public static final class Column{
        public static final String ID= "id";
        public static final String CODE ="code";
        public static final String NAME ="name";
        public static final String IP ="ip";

    }
    @Override
    public void setId(long id) {
        this.id = id;
    }
    @Override
    public long getId() {
        return id;
    }

    public Contact() {
    }

    public Contact(long id, String code, String name, String ip, boolean stateConnect) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.ip = ip;
        this.stateConnect = stateConnect;
    }

    private long id;
    private String code;
    private String name;
    private String ip;
    private boolean stateConnect = false;
    
    public String roomCode(){
        return ME_CODE + code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public boolean isStateConnect() {
        return stateConnect;
    }

    public void setStateConnect(boolean stateConnect) {
        this.stateConnect = stateConnect;
    }

}




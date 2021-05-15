package com.example.gianmarco.bluethings.ENTITY;

import android.graphics.Bitmap;

public class Contact {

    private int id = -1;
    private Bitmap avatar = null;
    private String nick_name;
    private String device_address;
    private String device_name;

    public Contact(String nick_name){
        this.nick_name = nick_name;
    }


    public Contact(Bitmap avatar, String nick_name){
        this.avatar = avatar;
        this.nick_name = nick_name;
    }


    //   GETTERS
    public Bitmap getAvatar() {
        return avatar;
    }

    public String getNick_name() {
        return nick_name;
    }

    public String getDevice_address() {
        return device_address;
    }

    public String getDevice_name() {
        return device_name;
    }

    public int getId() {
        return id;
    }

    //   SETTERS

    public void setAvatar(Bitmap avatar) {
        this.avatar = avatar;
    }

    public void setNick_name(String nick_name) {
        this.nick_name = nick_name;
    }

    public void setDevice_address(String device_address) {
        this.device_address = device_address;
    }

    public void setDevice_name(String device_name) {
        this.device_name = device_name;
    }

    public void setId(int id) {
        this.id = id;
    }
}

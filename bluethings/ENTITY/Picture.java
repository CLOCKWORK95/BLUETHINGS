package com.example.gianmarco.bluethings.ENTITY;

import android.graphics.Bitmap;

public class Picture {

    private int id;
    private Bitmap picture;


    public Picture(int id, Bitmap picture) {
        this.id = id;
        this.picture = picture;
    }


    //    GETTERS
    public int getId() {
        return id;
    }

    public Bitmap getPicture() {
        return picture;
    }


    //SETTERS

    public void setPicture(Bitmap picture) {
        this.picture = picture;
    }

    public void setId(int id) {
        this.id = id;
    }
}

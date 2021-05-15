package com.example.gianmarco.bluethings.ENTITY;

public class ChatRaw implements Comparable {

    private int position;
    private String text;

    public ChatRaw(int position, String text) {
        this.position = position;
        this.text = text;
    }

    //GETTERS
    public int getPosition() {
        return position;
    }

    public String getText() {
        return text;
    }


    //SETTERS
    public void setPosition(int position) {
        this.position = position;
    }

    public void setText(String text) {
        this.text = text;
    }




    @Override
    public int compareTo(Object compareRaw) {
        int compareage = ((ChatRaw) compareRaw).getPosition();
        /* For Ascending order*/
        return this.position - compareage;
    }

    @Override
    public String toString() {
        return "[ text=" + text + ", position=" + position + "]";
    }

}

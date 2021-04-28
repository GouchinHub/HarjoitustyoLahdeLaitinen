package com.example.harjoitustyo;

import java.io.Serializable;

public class User implements Serializable {

    private final String name;
    private final String password;
    private final byte[] salt;
    private final boolean guestFlag;
    private int viewState;
    private Profile profile;

    public User(String username, String hashedPassword, byte[] saltBytes, boolean flag, int viewState) {
        this.name = username;
        this.password = hashedPassword;
        this.salt = saltBytes;
        this.guestFlag = flag;
        this.viewState = viewState;
        //initialize user with default profile information
        this.profile = new Profile(name);
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public byte[] getSalt() {
        return salt;
    }

    public Profile getProfile(){
        return profile;
    }

    public boolean isGuestFlag() {
        return guestFlag;
    }

    public void saveProfile(Profile profileChanges){
        profile = profileChanges;
    }

    public int getViewState() {
        return viewState;
    }

    public void setViewState(int viewState) {
        this.viewState = viewState;
    }
}

package com.sebastiano.licciardello.justgonowtoadventure;

import java.util.HashMap;

public class User {

    private String email;
    private boolean isSuperuser;
    private HashMap<String, Item> favorites;

    public User() { }

    public User(String email, boolean isSuperuser) {
        this.email = email;
        this.isSuperuser = isSuperuser;
    }

    // getters and setters

    public void setFavorites(HashMap<String, Item> favorites) {
        this.favorites = favorites;
    }

    public HashMap<String, Item> getFavorites() {
        return favorites;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isSuperuser() {
        return isSuperuser;
    }

    public void setSuperuser(boolean superuser) {
        isSuperuser = superuser;
    }
}

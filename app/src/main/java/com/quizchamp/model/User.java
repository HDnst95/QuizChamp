package com.quizchamp.model;

public class User {
    public User(String uid, String email) {
        this.uid = uid;
        this.email = email;
        this.name = name;
    }
    private String uid;
    private String email;
    private String name;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
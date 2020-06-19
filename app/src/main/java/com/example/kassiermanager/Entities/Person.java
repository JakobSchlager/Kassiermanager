package com.example.kassiermanager.Entities;

import java.io.Serializable;

public class Person implements Serializable {

    int id;
    String name;
    int stammtsichID;
    boolean isAdmin;

    public Person(int id, String name, int stammtsichID, boolean isAdmin) {
        this.id = id;
        this.name = name;
        this.stammtsichID = stammtsichID;
        this.isAdmin = isAdmin;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getStammtsichID() {
        return stammtsichID;
    }

    public boolean isAdmin() {
        return isAdmin;
    }
}

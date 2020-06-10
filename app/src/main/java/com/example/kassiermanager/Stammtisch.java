package com.example.kassiermanager;

import java.io.Serializable;

public class Stammtisch implements Serializable
{
    private String name;
    private int id;

    public Stammtisch(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public Stammtisch() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

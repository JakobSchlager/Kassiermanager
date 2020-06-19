package com.example.kassiermanager.Entities;

import java.io.Serializable;

public class Drink implements Serializable {

    int id;
    String name;
    int stammtsichID;
    double price;

    public Drink(int id, String name, int stammtsichID, double price) {
        this.id = id;
        this.name = name;
        this.stammtsichID = stammtsichID;
        this.price = price;
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

    public double getPrice() {
        return price;
    }
}

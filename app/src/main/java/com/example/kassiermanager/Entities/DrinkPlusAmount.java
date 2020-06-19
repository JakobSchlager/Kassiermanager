package com.example.kassiermanager.Entities;

import java.io.Serializable;

public class DrinkPlusAmount implements Serializable {
    private int id;
    private int getraenkeID;
    private int personID;
    private String name;
    private Double price;
    private int amount;

    public DrinkPlusAmount(int id, int getraenkeID, int personID, String name, Double price, int amount) {
        this.id = id;
        this.getraenkeID = getraenkeID;
        this.personID = personID;
        this.name = name;
        this.price = price;
        this.amount = amount;
    }

    public DrinkPlusAmount() {
    }

    public void setPersonID(int personID) {
        this.personID = personID;
    }

    public int getPersonID() {
        return personID;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setGetraenkeID(int getraenkeID) {
        this.getraenkeID = getraenkeID;
    }

    public int getId() {
        return id;
    }

    public int getGetraenkeID() {
        return getraenkeID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}

package com.example.kassiermanager.Entities;

import java.io.Serializable;

public class DummyDrink implements Serializable {

    String name;
    Double price;

    public DummyDrink(String name, Double price) {
        this.name = name;
        this.price = price;
    }

    public DummyDrink(){}

    public String getName() {
        return name;
    }

    public Double getPrice() {
        return price;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}

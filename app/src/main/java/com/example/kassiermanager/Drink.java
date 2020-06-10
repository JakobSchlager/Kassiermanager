package com.example.kassiermanager;

import java.io.Serializable;

public class Drink implements Serializable {

    private String Name;
    private Double Price;

    public Drink(String name, Double price) {
        Name = name;
        Price = price;
    }

    public Drink() {
    }

    public String getName() {
        return Name;
    }

    public Double getPrice() {
        return Price;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setPrice(Double price) {
        Price = price;
    }
}

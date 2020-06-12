package com.example.kassiermanager;

import java.io.Serializable;

public class DrinkPlusAmount implements Serializable {
    private String name;
    private Double price;
    private int amount;

    public DrinkPlusAmount(String name, Double price, int amount) {
        this.name = name;
        this.price = price;
        this.amount = amount;
    }

    public DrinkPlusAmount() {
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

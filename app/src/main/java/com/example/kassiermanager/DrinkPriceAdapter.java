package com.example.kassiermanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DrinkPriceAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private int layoutID;
    private List<Drink> drinks = new ArrayList<>();

    public DrinkPriceAdapter(Context ctx, int layoutID, List<Drink> drinks) {
        this.inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this. layoutID = layoutID;
        this.drinks = drinks;
    }

    @Override
    public int getCount() {
        return drinks.size();
    }

    @Override
    public Object getItem(int position) {
        return drinks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Drink drink = drinks.get(position);

        View listItem = (convertView == null) ? inflater.inflate(this.layoutID, null) : convertView;
        ((TextView) listItem.findViewById(R.id.txt_drinkName)).setText(drink.getName());
        ((TextView) listItem.findViewById(R.id.txt_drinkPrice)).setText(String.valueOf(drink.getPrice()));

        return listItem;
    }
}

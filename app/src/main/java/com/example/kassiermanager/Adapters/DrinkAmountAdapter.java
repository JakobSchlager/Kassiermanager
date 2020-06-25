package com.example.kassiermanager.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.kassiermanager.Entities.DrinkPlusAmount;
import com.example.kassiermanager.R;

import java.util.List;

public class DrinkAmountAdapter extends BaseAdapter {

    LayoutInflater inflater;
    int layoutID;
    List<DrinkPlusAmount> drinksAmount;

    public DrinkAmountAdapter(Context ctx, int layoutID, List<DrinkPlusAmount> drinksAmount) {
        this.inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.layoutID = layoutID;
        this.drinksAmount = drinksAmount;
    }

    @Override
    public int getCount() {
        return drinksAmount.size();
    }

    @Override
    public Object getItem(int position) {
        return drinksAmount.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        DrinkPlusAmount drinkPlusAmount= drinksAmount.get(position);
        View listItem = (convertView == null) ? inflater.inflate(this.layoutID, null) : convertView;
        ((TextView) listItem.findViewById(R.id.txt_show_Drink_Name)).setText(drinkPlusAmount.getName());
        ((TextView) listItem.findViewById(R.id.txt_show_Drink_Price)).setText(String.valueOf(drinkPlusAmount.getPrice()));
        ((TextView) listItem.findViewById(R.id.txt_show_Drink_Amount)).setText(String.valueOf(drinkPlusAmount.getAmount()));

        return listItem;

    }
}

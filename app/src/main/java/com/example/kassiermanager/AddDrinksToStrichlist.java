package com.example.kassiermanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AddDrinksToStrichlist extends AppCompatActivity {

    Spinner spinnerDrinks;
    Button btn_plus_one;
    Button btn_sub_one;
    TextView txt_Amount;
    Button btn_Add;

    ArrayAdapter myAdapter;

    List<String> drinkName = new ArrayList<>();
    List<Drink> drinks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_drinks_to_strichlist);



        Bundle bundle = getIntent().getExtras();

        if(bundle != null)
        {
            drinks = (List<Drink>) bundle.getSerializable("drinks");

            drinkName = drinks.stream()
                    .map(a -> a.getName())
                    .collect(Collectors.toList());
        }

        spinnerDrinks = findViewById(R.id.drinks_spinner);
        myAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, drinkName);

        spinnerDrinks.setAdapter(myAdapter);
        spinnerDrinks.setSelection(0);
        myAdapter.notifyDataSetChanged();

        btn_plus_one = findViewById(R.id.btn_add_one);
        btn_sub_one = findViewById(R.id.btn_sub_one);
        txt_Amount = findViewById(R.id.txt_show_Amount);
        btn_Add = findViewById(R.id.btn_add_drink_to_strichlist);




        btn_plus_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = Integer.parseInt(txt_Amount.getText().toString()) + 1;
                txt_Amount.setText(String.valueOf(temp));
            }
        });

        btn_sub_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = Integer.parseInt(txt_Amount.getText().toString()) - 1;
                if(temp > 0)
                {
                    txt_Amount.setText(String.valueOf(temp));
                }
            }
        });

        btn_Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DrinkPlusAmount drink = new DrinkPlusAmount(spinnerDrinks.getSelectedItem().toString(),  drinks.get(spinnerDrinks.getSelectedItemPosition()).getPrice(),Integer.parseInt(txt_Amount.getText().toString()));

                Intent resultIntent = new Intent();
                resultIntent.putExtra("myReturnDrink", drink);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }
}

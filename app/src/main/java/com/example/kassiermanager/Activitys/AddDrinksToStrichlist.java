package com.example.kassiermanager.Activitys;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.kassiermanager.Entities.Drink;
import com.example.kassiermanager.Entities.DrinkPlusAmount;
import com.example.kassiermanager.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
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

        actionbarDesign();

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
                //alle ids 1, werden später verändert
                int personID = getIntent().getIntExtra("personID", 0);
                String choosenName = spinnerDrinks.getSelectedItem().toString();
                Drink choosenDrink = null;

                for (Drink drink : drinks) {
                    if(drink.getName() == choosenName) choosenDrink = drink;
                }

                DrinkPlusAmount drink = new DrinkPlusAmount(1, choosenDrink.getId(), personID, spinnerDrinks.getSelectedItem().toString(),  drinks.get(spinnerDrinks.getSelectedItemPosition()).getPrice(), Integer.parseInt(txt_Amount.getText().toString()));

                Intent resultIntent = new Intent();
                resultIntent.putExtra("myReturnDrink", drink);
                setResult(RESULT_OK, resultIntent);

                finish();
            }
        });
    }

    private void actionbarDesign(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String backgroundColour = prefs.getString("colour", "#6200EE");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(backgroundColour)));
    }

    private boolean deleteGetraenk(int id) {
        GetraenkDeleteEndpunkt getraenkDeleteEndpunkt = new GetraenkDeleteEndpunkt();
        try {
            String result = getraenkDeleteEndpunkt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(id)).get();
            return !result.contains("failed");
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private class GetraenkDeleteEndpunkt extends AsyncTask<String, Integer, String> {

        private String URL = "http://139.178.101.87/StammtischTest/api/functions/Getraenk/deleteGetraenk.php?<id>";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected String doInBackground(String... strings) {
            URL = URL.replace("<id>", strings[0]);
            String sJson = "";
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(URL).openConnection();
                connection.setRequestMethod("DELETE");
                connection.setRequestProperty("Content-Type", "application/json");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sJson;
        }

    }
}

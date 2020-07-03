package com.example.kassiermanager.Activitys;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.kassiermanager.Entities.Drink;
import com.example.kassiermanager.Adapters.DrinkPriceAdapter;
import com.example.kassiermanager.Entities.DummyDrink;
import com.example.kassiermanager.R;
import com.example.kassiermanager.Entities.Stammtisch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class AddTableandDrinks extends AppCompatActivity {

    static final int INTEND_DRINK_REQUEST = 69;
    static final int INTEND_DRINK_REQUEST_EDIT = 420;

    Button btn_submit;
    Button btn_add_Drink;
    ListView listView;
    EditText txt_Name;

    private List<DummyDrink> drinkList = new ArrayList<>();
    private DrinkPriceAdapter myAdapter;

    Stammtisch stammtisch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_table_and_drinks);

        actionbarDesign();

        btn_add_Drink = findViewById(R.id.btn_add_Drink);
        btn_submit = findViewById(R.id.btn_submit_table);
        txt_Name = findViewById(R.id.txt_TableName);

        listView = findViewById(R.id.drinks_listview);
        registerForContextMenu(listView);
        myAdapter = new DrinkPriceAdapter(this, R.layout.drinksintables_list_layout, drinkList);
        listView.setAdapter(myAdapter);

        Bundle bundle = getIntent().getExtras();

        if(bundle != null)
        {
            Stammtisch table = (Stammtisch) bundle.getSerializable("Stammtisch");

            // tabeles von DB und in Views einfügen.
            stammtisch = readOneStammtisch(table.getId());
            List<Drink> drinks = readDrinksFromStammtisch(table.getId());
            txt_Name.setText(stammtisch.getName());
            drinkList.addAll(drinks.stream().map(drink -> drink.getDummyDrink()).collect(Collectors.toList()));
            myAdapter.notifyDataSetChanged();
        }



        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = txt_Name.getText().toString();

                if(name.isEmpty() || drinkList.isEmpty())
                {
                    Toast toast=Toast.makeText(getApplicationContext(),"Add a least one drink and set a name!",Toast.LENGTH_SHORT);
                    toast.show();
                }
                else
                {
                    Intent resultIntent = new Intent();
                    if(stammtisch != null) resultIntent.putExtra("Stammtisch", stammtisch);
                    resultIntent.putExtra("Name", name);
                    resultIntent.putExtra("DrinkList", (Serializable) drinkList);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }
        });



        btn_add_Drink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), insertDrinkandPrice.class);
                startActivityForResult(intent, INTEND_DRINK_REQUEST);
            }
        });



    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.drinks_in_tables_edit)
        {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

            if(info != null)
            {
                int pos = info.position;

                DummyDrink drink = (DummyDrink) listView.getAdapter().getItem(pos);

                Intent intent = new Intent(getApplication(), insertDrinkandPrice.class);
                intent.putExtra("Drink", drink);
                startActivityForResult(intent, INTEND_DRINK_REQUEST_EDIT);
            }
            return true;
        }
        if(item.getItemId() == R.id.drinks_in_tables_delete)
        {

            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

            if(info != null)
            {
                int pos = info.position;

                DummyDrink drink = (DummyDrink) listView.getAdapter().getItem(pos);

                drinkList.remove(drink);
                myAdapter.notifyDataSetChanged();
            }

            return true;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        int viewID = v.getId();

        if(viewID == R.id.drinks_listview)
        {
            getMenuInflater().inflate(R.menu.context_drinks_table, menu);
        }

        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check which request we're responding to
        if (requestCode == INTEND_DRINK_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {


                    assert data != null;
                    DummyDrink drink = (DummyDrink) data.getSerializableExtra("Drink");

                    drinkList.add(drink);
                    myAdapter.notifyDataSetChanged();



            }
        }
        if(requestCode == INTEND_DRINK_REQUEST_EDIT)
        {
            if(resultCode == RESULT_OK)
            {

                assert data != null;
                DummyDrink drink = (DummyDrink) data.getSerializableExtra("Drink");
                DummyDrink oldDrink = (DummyDrink) data.getSerializableExtra("OldDrink");

                drinkList.add(drink);
                drinkList.remove(oldDrink);
                myAdapter.notifyDataSetChanged();
            }

        }
    }

    private Stammtisch readOneStammtisch(int id) {
        StammtischReadOneTask stammtischReadOneTask = new StammtischReadOneTask();
        try {
            JSONObject jsonObject = new JSONObject(stammtischReadOneTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(id)).get());
            Stammtisch newStammtisch = new Stammtisch(jsonObject.getString("name"), jsonObject.getInt("id"));

            return newStammtisch;
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private class StammtischReadOneTask extends AsyncTask<String, Integer, String> {

        private String URL = "http://139.178.101.87/StammtischTest/api/functions/Stammtisch/readOneStammtisch.php?id=<id>";

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
            String sJsonResponse = "";
            URL = URL.replace("<id>", strings[0]);

            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(URL).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    sJsonResponse = readResponseStream(reader);
                } else {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                    sJsonResponse = readResponseStream(reader);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sJsonResponse;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        private String readResponseStream(BufferedReader reader) throws IOException {
            StringBuilder stringBuilder = new StringBuilder();
            String line = "";
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        }
    }

    private List<Drink> readDrinksFromStammtisch(int stammtischID) {
        List<Drink> drinks = new ArrayList<>();

        StrichlistReadFromStammtischTask strichlistReadFromStammtischTask = new StrichlistReadFromStammtischTask();
        try {
            String jsonString = strichlistReadFromStammtischTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(stammtischID)).get();
            if(!jsonString.contains("No strichlistes found") && !jsonString.equals("")) {
                JSONArray jsonArray = new JSONObject(jsonString).getJSONArray("getraenke");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                    int getrankeID = jsonObject.getInt("id");
                    String getrankeNamen = jsonObject.getString("GetraenkeName");
                    double price = jsonObject.getDouble("Preis");

                    drinks.add(new Drink(getrankeID, getrankeNamen, stammtischID, price));
                }
            }
            return drinks;
        } catch (ExecutionException | InterruptedException | JSONException e){
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    private class StrichlistReadFromStammtischTask extends AsyncTask<String, Integer, String> {

        private String URL = "http://139.178.101.87/StammtischTest/api/functions/Getraenk/readGetraenkeFromStammtisch.php?id=<id>";

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
            String sJsonResponse = "";
            URL = URL.replace("<id>", strings[0]);

            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(URL).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    sJsonResponse = readResponseStream(reader);
                } else {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                    sJsonResponse = readResponseStream(reader);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sJsonResponse;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        private String readResponseStream(BufferedReader reader) throws IOException {
            StringBuilder stringBuilder = new StringBuilder();
            String line = "";
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        }

    }

    private void actionbarDesign(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String backgroundColour = prefs.getString("colour", "#6200EE");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(backgroundColour)));
    }
}

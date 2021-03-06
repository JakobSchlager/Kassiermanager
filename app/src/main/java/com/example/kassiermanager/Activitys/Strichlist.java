package com.example.kassiermanager.Activitys;

import android.app.AlertDialog;
import android.app.Notification;
import android.content.DialogInterface;
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
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.example.kassiermanager.Adapters.DrinkAmountAdapter;
import com.example.kassiermanager.Entities.Drink;
import com.example.kassiermanager.Entities.DrinkPlusAmount;
import com.example.kassiermanager.Entities.Person;
import com.example.kassiermanager.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class Strichlist extends AppCompatActivity {

    static final int INTENT_REQUEST_CODE_DRINKS_TO_ADD_TO_STRICHLIST = 1337;

    Person person;
    ListView drinkListView;
    TextView drinksSum;
    Button btn_Pay;
    Button btn_AddDrink;

    List<DrinkPlusAmount> drinksAndAmount = new ArrayList<>();
    DrinkAmountAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strichlist);

        actionbarDesign();

        Bundle bundle = getIntent().getExtras();
        person = (Person) bundle.getSerializable("Person");

        btn_Pay = findViewById(R.id.btn_pay);
        btn_AddDrink = findViewById(R.id.btn_add_drink);
        drinksSum = findViewById(R.id.txt_to_show_Sum);

        drinkListView = findViewById(R.id.strich_listview);
        myAdapter = new DrinkAmountAdapter(this, R.layout.my_drins_and_amount_list_layout, drinksAndAmount);
        drinkListView.setAdapter(myAdapter);
        registerForContextMenu(drinkListView);

        //strichlisten von der Person werden hinzugefügt
        drinksAndAmount.addAll(readStrichlisteFromPerson(person.getId()).stream().filter(strichlist -> strichlist.getAmount() != 0).collect(Collectors.toList()));

        updateSum();

        myAdapter.notifyDataSetChanged();


        btn_Pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    new AlertDialog.Builder(Strichlist.this)
                            .setMessage("Are you sure you want to Pay " + getSum() + "€ ?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    drinksAndAmount.forEach(drinks -> {
                                        updateStrichliste(drinks, 0);
                                    });

                                    drinksAndAmount.clear();
                                    updateSum();
                                    myAdapter.notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();


                /*drinksAndAmount.clear();
                updateSum();
                myAdapter.notifyDataSetChanged();*/
            }
        });

        btn_AddDrink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Drink> drinks = new ArrayList<>();
                drinks.addAll(readDrinksFromStammtisch(person.getStammtsichID()));

                Intent intent = new Intent(getApplicationContext(), AddDrinksToStrichlist.class);
                intent.putExtra("drinks", (Serializable) drinks);
                intent.putExtra("personID", person.getId());
                startActivityForResult(intent, INTENT_REQUEST_CODE_DRINKS_TO_ADD_TO_STRICHLIST);
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        int viewID = v.getId();

        if(viewID == R.id.strich_listview)
        {
            getMenuInflater().inflate(R.menu.context_edit, menu);
        }

        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.context_edit)
        {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

            int pos = 1;
            if(info != null)
            {
                 pos = info.position;

            }

        }
        return super.onContextItemSelected(item);
    }

    private DrinkPlusAmount updateStrichliste(DrinkPlusAmount oldStrichliste, int newAmount) {
        try {
            StrichlistenEditEndpunkt strichlistenEditEndpunkt = new StrichlistenEditEndpunkt();
            String jsonString = strichlistenEditEndpunkt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(oldStrichliste.getId()), String.valueOf(oldStrichliste.getPersonID()), String.valueOf(oldStrichliste.getGetraenkeID()), String.valueOf(newAmount)).get();

            if (jsonString.contains("strichliste was updated.")) {
                oldStrichliste.setAmount(newAmount);
                return oldStrichliste;
            }
        }
        catch (ExecutionException | InterruptedException  e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<DrinkPlusAmount> readStrichlisteFromPerson(int personID) {
        List<DrinkPlusAmount> strichlists = new ArrayList<>();

        StrichlistReadFromPersonTask strichlistReadFromPersonTask = new StrichlistReadFromPersonTask();
        try {
            String jsonString = strichlistReadFromPersonTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(personID)).get();
            if(!jsonString.contains("No strichlistes found") && !jsonString.equals("")) {
                JSONArray jsonArray = new JSONObject(jsonString).getJSONArray("strichlisten");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                    int id = jsonObject.getInt("strichlistenid");
                    int getraenkeID = jsonObject.getInt("getraenkeID");
                    String getrankeNamen = jsonObject.getString("getraenkeNamen");
                    double price = jsonObject.getDouble("price");
                    int anzahl = jsonObject.getInt("anzahl");

                    strichlists.add(new DrinkPlusAmount(id, getraenkeID, personID, getrankeNamen, price, anzahl));
                }
            }
            return strichlists;
        } catch (ExecutionException | InterruptedException | JSONException e){
            e.printStackTrace();
        }

        return new ArrayList<>();
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

    private class StrichlistReadFromPersonTask extends AsyncTask<String, Integer, String> {

        private String URL = "http://139.178.101.87/StammtischTest/api/functions/Strichliste/readStrichlisteFromPerson.php?id=<id>";

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode == INTENT_REQUEST_CODE_DRINKS_TO_ADD_TO_STRICHLIST)
        {
            if (resultCode == RESULT_OK)
            {
                boolean changed = false;

                DrinkPlusAmount drink = (DrinkPlusAmount) data.getSerializableExtra("myReturnDrink");

                for (int i = 0; i < drinksAndAmount.size(); i++) {
                    if(drinksAndAmount.get(i).getName().equals(drink.getName()))
                    {
                        int strichlistenID = drinksAndAmount.get(i).getId();
                        DrinkPlusAmount oldStrichliste = drinksAndAmount.get(i);
                        oldStrichliste.setId(strichlistenID);


                        DrinkPlusAmount newStrichliste = updateStrichliste(oldStrichliste, oldStrichliste.getAmount()+drink.getAmount());
                        drinksAndAmount.set(i, newStrichliste);
                        changed = true;
                    }
                }

                if(!changed)
                {
                    drinksAndAmount.add(createStrichliste(drink));
                }

                updateSum();
                myAdapter.notifyDataSetChanged();
            }
        }


        super.onActivityResult(requestCode, resultCode, data);

    }

    private DrinkPlusAmount createStrichliste(DrinkPlusAmount drink) {
        try {
            int personID = drink.getPersonID();
            int getraenkeID = drink.getGetraenkeID();
            int anzahl = drink.getAmount();
            StrichlisteCreateTask strichlisteCreateTask = new StrichlisteCreateTask();
            String jsonString = strichlisteCreateTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "AutoIncrement", String.valueOf(personID), String.valueOf(getraenkeID), String.valueOf(anzahl)).get();
            JSONObject jsonObject = new JSONObject(jsonString).getJSONObject("strichliste");

            int strichlistenID = jsonObject.getInt("id");
            personID = jsonObject.getInt("personId");
            getraenkeID = jsonObject.getInt("drinkId");
            anzahl = jsonObject.getInt("count");

            updateStrichliste(new DrinkPlusAmount(strichlistenID, getraenkeID, personID, drink.getName(), drink.getPrice(), anzahl), anzahl);

            return new DrinkPlusAmount(strichlistenID, getraenkeID, personID, drink.getName(), drink.getPrice(), anzahl);
        } catch (ExecutionException | InterruptedException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private class StrichlisteCreateTask extends AsyncTask<String, Integer, String> {

        private final String URL = "http://139.178.101.87/StammtischTest/api/functions/Strichliste/createStrichliste.php";

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
            String jsonString = "{ \"StrichlistenID\" : \"" + strings[0] + "\" , \"PersonID\" : \"" + strings[1] + "\" , \"GetraenkeID\" : \"" + strings[2] + "\" , \"Anzahl\" : \"" + strings[3] + "\" }";
            String sJsonResponse = "";

            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(URL).openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setFixedLengthStreamingMode(jsonString.getBytes().length);
                connection.getOutputStream().write(jsonString.getBytes());
                connection.getOutputStream().flush();

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

    private class StrichlistenEditEndpunkt extends AsyncTask<String, Integer, String> {

        private String URL = "http://139.178.101.87/StammtischTest/api/functions/Strichliste/updateStrichliste.php";

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
            String jsonString = "{ \"StrichlistenID\" : \"" + strings[0] + "\" , \"PersonID\" : \"" + strings[1] + "\" , \"GetraenkeID\" : \"" + strings[2] + "\" , \"Anzahl\" : \"" + strings[3] + "\" }";
            String sJson = "";

            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(URL).openConnection();
                connection.setRequestMethod("PUT");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setFixedLengthStreamingMode(jsonString.getBytes().length);
                connection.getOutputStream().write(jsonString.getBytes());
                connection.getOutputStream().flush();
                int responseCode = connection.getResponseCode();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                sJson = readResponseStream(reader);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sJson;
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


    private void updateSum()
    {
        Double sum = getSum();

        NumberFormat formater = new DecimalFormat("#0.00");
        drinksSum.setText(formater.format(sum) + " €");
    }

    private double getSum()
    {
        Double sum = drinksAndAmount.stream()
                .mapToDouble(a -> a.getPrice()*a.getAmount())
                .reduce(0, (b1, b2) -> b1+b2);

        return sum;
    }

    private void actionbarDesign(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String backgroundColour = prefs.getString("colour", "#6200EE");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(backgroundColour)));
    }
}

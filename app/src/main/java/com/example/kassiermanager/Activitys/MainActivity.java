package com.example.kassiermanager.Activitys;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.example.kassiermanager.Adapters.TableListAdapter;
import com.example.kassiermanager.CaptureAct;
import com.example.kassiermanager.Entities.Drink;
import com.example.kassiermanager.Entities.DummyDrink;
import com.example.kassiermanager.Entities.Person;
import com.example.kassiermanager.Entities.Stammtisch;
import com.example.kassiermanager.PreferenceActivity;
import com.example.kassiermanager.R;
import com.google.zxing.WriterException;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MAIN";
    static final int INTENT_REQUEST_CODE_DRINKS = 25518;
    static final int INTENT_CODE_EDIT_DRINKS = 6969;
    private static final int RQ_PREFERENCES = 911;
    private ListView myListview;
    private List<Stammtisch> tables = new ArrayList<>();
    private TableListAdapter myAdapter;
    IntentIntegrator integrator;

    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    private final String fileName = "Stammtische.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionbarDesign();

        myListview = findViewById(R.id.tablelistview);
        myAdapter = new TableListAdapter(this, R.layout.my_tables_list_layout, tables);
        myListview.setAdapter(myAdapter);
        registerForContextMenu(myListview);
        myListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), PersonListActivity.class);
                intent.putExtra("StammtischID", tables.get(position).getId());

                startActivity(intent);
                //PersonenID: 1, StammtischID: 5
            }
        });

        List<Integer> stammtischIds = readStammtischIDs();

        stammtischIds.forEach(id -> tables.add(readOneStammtisch(id)));
        myAdapter.notifyDataSetChanged();
    }

    private void actionbarDesign(){
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        preferenceChangeListener = ((sharedPrefs, key) -> preferenceChanged(sharedPrefs, key));
        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
        String backgroundColour = prefs.getString("colour", "#6200EE");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(backgroundColour)));
    }

    private void preferenceChanged(SharedPreferences sharedPreferences, String key) {
        String backgroundColour = prefs.getString("colour", "#6200EE");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(backgroundColour)));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        int viewId = v.getId();
        if(viewId == R.id.tablelistview)
        {
            getMenuInflater().inflate(R.menu.context_main_tables, menu);
        }


        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int pos = 0;
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if(info != null) pos = info.position;

        switch (item.getItemId()) {
            case R.id.context_showQRCode:
                showQRCode(tables.get(pos));
                return true;
            case R.id.context_edit_main:
                Stammtisch table = (Stammtisch) myListview.getAdapter().getItem(pos);

                Intent intent = new Intent(this, AddTableandDrinks.class);
                intent.putExtra("Stammtisch", table);
                startActivityForResult(intent, INTENT_CODE_EDIT_DRINKS);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_mainactivity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        Log.d(TAG, "onOptionsItemSelected " + id);

        switch (id)
        {
            case R.id.scanQR:
                scanCode();
                break;
            case R.id.newTable:
                createNewTable();
                break;
            case R.id.preferences_settings:
                Intent intent = new Intent(this, PreferenceActivity.class);
                startActivityForResult(intent, RQ_PREFERENCES);
        }


        return super.onOptionsItemSelected(item);
    }

    private void scanCode()
    {
        integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureAct.class);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scanning Code");
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case INTENT_REQUEST_CODE_DRINKS:
                        List<DummyDrink> drinks = new ArrayList<>();

                        assert data != null;
                        String name = data.getStringExtra("Name");
                        drinks = (List<DummyDrink>) data.getSerializableExtra("DrinkList");

                        Stammtisch newStammtisch = createStammtisch(name);
                        int stammtischID = newStammtisch.getId();

                        drinks.forEach(drink -> createDrink(stammtischID, drink.getName(), drink.getPrice()));

                        tables.add(newStammtisch);
                        myAdapter.notifyDataSetChanged();
                        break;

                    case IntentIntegrator.REQUEST_CODE:
                    IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

                    int id = Integer.valueOf(result.getContents());

                    tables.add(readOneStammtisch(id));
                    myAdapter.notifyDataSetChanged();
                    break;

                    case RQ_PREFERENCES:
                    System.out.println();
                    break;

                case INTENT_CODE_EDIT_DRINKS:
                    drinks = new ArrayList<>();

                    stammtischID = ((Stammtisch)data.getSerializableExtra("Stammtisch")).getId();
                    name = data.getStringExtra("Name");
                    drinks = (List<DummyDrink>) data.getSerializableExtra("DrinkList");

                    newStammtisch = updateStammtisch(stammtischID, name);

                    drinks.forEach(drink -> createDrink(stammtischID, drink.getName(), drink.getPrice()));

                    int pos = 0;
                    for (int i = 0; i < tables.size(); i++) {
                        if(tables.get(i).getId() == newStammtisch.getId()) pos = i;
                    }
                    tables.set(pos, newStammtisch);
                    myAdapter.notifyDataSetChanged();
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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

    private void createNewTable()
    {
        Intent intent = new Intent(this, AddTableandDrinks.class);
        startActivityForResult(intent, INTENT_REQUEST_CODE_DRINKS);
    }

    private void showQRCode(Stammtisch table)
    {
        Bitmap bitmap = null;
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smalldimension = width<height ? width:height;
        smalldimension = smalldimension*3/4;
        QRGEncoder qrgEncoder = new QRGEncoder(String.valueOf(table.getId()), null, QRGContents.Type.TEXT, smalldimension);
        try{
            bitmap = qrgEncoder.encodeAsBitmap();

        }
        catch (WriterException e) {
            Log.d(TAG, "showQRCode");
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();


   Intent intent = new Intent(this, QRCodeActivity.class);
        intent.putExtra("image", byteArray);
        intent.putExtra("name", table.getName());
        startActivity(intent);
    }

    private Stammtisch createStammtisch(String name) {
        StammtischCreateTask stammtischCreateTask = new StammtischCreateTask();
        try {
            String jsonString = stammtischCreateTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf("AutoIncrement"), name).get();
            JSONObject jsonObject = new JSONObject(jsonString);
            Stammtisch newStammtisch = new Stammtisch(jsonObject.getJSONObject("stammtisch").getString("name"), jsonObject.getJSONObject("stammtisch").getInt("id"));

            saveStammtischIDsLocal(newStammtisch.getId());
            return newStammtisch;
        } catch (ExecutionException | InterruptedException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    private class StammtischCreateTask extends AsyncTask<String, Integer, String> {

        private final String URL = "http://139.178.101.87/StammtischTest/api/functions/Stammtisch/createStammtisch.php";

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
            String jsonString = "{ \"id\" : \"" + strings[0] + "\" , \"name\" : \"" + strings[1] + "\" }";
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

    private class StammtischReadAllTask extends AsyncTask<String, Integer, String> {

        private final String URL = "http://139.178.101.87/StammtischTest/api/functions/Stammtisch/readStammtische.php";

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

    public Drink createDrink(int stammtischId, String name, double preis) {
        DrinkCreateTask PersonCreateTask = new DrinkCreateTask();
        try {
            String jsonString = PersonCreateTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "AutoIncrement", String.valueOf(stammtischId), name, String.valueOf(preis)).get();
            JSONObject jsonObject = new JSONObject(jsonString).getJSONObject("Drink");

            int id = jsonObject.getInt("id");
            name = jsonObject.getString("name");
            stammtischId = jsonObject.getInt("stammtischID");
            preis = jsonObject.getDouble("price");

            return new Drink(id, name, stammtischId, preis);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private class DrinkCreateTask extends AsyncTask<String, Integer, String> {

        private final String URL = "http://139.178.101.87/StammtischTest/api/functions/Getraenk/createGetraenk.php";

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
            String jsonString = "{ \"GetraenkeID\" : \"" + strings[0] + "\" , \"StammtischID\" : \"" + strings[1] + "\" , \"GetraenkeName\" : \"" + strings[2] + "\" , \"Preis\" : \"" + strings[3] + "\" }";
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

    private void saveStammtischIDsLocal(int stammtischId) {
        try {
            FileOutputStream fos = openFileOutput(fileName, MODE_PRIVATE | MODE_APPEND);
            PrintWriter out = new PrintWriter(new OutputStreamWriter(fos));
            out.println(stammtischId+";");
            out.flush();
            out.close();
        } catch (FileNotFoundException exp) {
            exp.printStackTrace();
        }
    }

    private List<Integer> readStammtischIDs() {
        try {
            FileInputStream fis = openFileInput(fileName);
            BufferedReader in = new BufferedReader(new InputStreamReader(fis));
            String line;
            String data = "";
            while ((line = in.readLine()) != null) {
                data += line;
            }
            in.close();

            List<Integer> stammtische = new ArrayList<>();

            Arrays.asList(data.split(";")).forEach(s -> stammtische.add(Integer.valueOf(s)));

            return stammtische;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private Stammtisch updateStammtisch(int newID, String newName) {
        try {
            StammtischEditEndpunkt stammtischEditEndpunkt = new StammtischEditEndpunkt();
            String jsonString = stammtischEditEndpunkt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(newID), newName).get();

            if (jsonString.contains("Stammtisch was updated.")) {
                return new Stammtisch(newName, newID);
            }
        }
        catch (ExecutionException | InterruptedException  e) {
            e.printStackTrace();
        }
        return null;
    }

    private class StammtischEditEndpunkt extends AsyncTask<String, Integer, String> {

        private String URL = "http://139.178.101.87/StammtischTest/api/functions/Stammtisch/updateStammtisch.php";

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
            String jsonString = "{ \"StammtischID\" : \"" + strings[0] + "\" , \"Name\" : \"" + strings[1] + "\" }";
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
}

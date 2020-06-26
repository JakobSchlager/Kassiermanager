package com.example.kassiermanager.Activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.app.AlertDialog;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.kassiermanager.Entities.DrinkPlusAmount;
import com.example.kassiermanager.Entities.Person;
import com.example.kassiermanager.Adapters.PersonListAdapter;
import com.example.kassiermanager.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class PersonListActivity extends AppCompatActivity {

    String fileName_IDS = "StammtischIDs.txt";

    private ListView myListview;
    private List<Person> persons = new ArrayList<>();
    private PersonListAdapter myAdapter;

    private int myID;
    int stammtischId;

    private final String fileName = "Stammtische.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_list);

        actionbarDesign();

        myListview = findViewById(R.id.persons_List);
        myAdapter = new PersonListAdapter(this, R.layout.my_persons_list_layout, persons);
        myListview.setAdapter(myAdapter);
        registerForContextMenu(myListview);

        this.stammtischId = getIntent().getExtras().getInt("StammtischID");
        persons.addAll(readPersonsFromStammtisch(stammtischId));

        Map<Integer, Integer> stammtischPersonen = readStammtischIDs();
        if(stammtischPersonen.isEmpty() || stammtischPersonen.get(stammtischId) == null) {
            showNewPersonDialog();
        } else {
            myID = stammtischPersonen.get(stammtischId);
        }

        myAdapter.notifyDataSetChanged();

        myListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Person p = persons.get(position);

                if(myID == p.getId()) {
                    Intent intent = new Intent(getApplicationContext(), Strichlist.class);
                    intent.putExtra("Person", p);
                    startActivity(intent);
                } else {
                    Toast.makeText(PersonListActivity.this, "Kein Zugriffsrecht!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        int viewId = v.getId();
        if (viewId == R.id.persons_List || viewId == R.id.persons_List) {
            getMenuInflater().inflate(R.menu.context_drinks_table, menu);
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        int position = info.position;
        switch(item.getItemId()) {
            case R.id.drinks_in_tables_edit:
                if(isOwnPerson(position)) showEditPersonDialog(position);
                break;
            case R.id.drinks_in_tables_delete:
                if(isOwnPerson(position)) {
                    new AlertDialog.Builder(PersonListActivity.this)
                            .setMessage("Are you sure you want to exit this Stammtisch?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(deletePerson(persons.get(position).getId())) persons.remove(position);
                                    myAdapter.notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
                break;
        }

        return super.onContextItemSelected(item);
    }

    private boolean isOwnPerson(int position) {
        return persons.get(position).getId() == myID;
    }

    private void showEditPersonDialog(int postition){
        final View vDialog = getLayoutInflater().inflate(R.layout.dialog_new_person, null );
        new AlertDialog.Builder(this)
                .setMessage("Name:")
                .setCancelable(false)
                .setView(vDialog)
                .setPositiveButton ("ok", (dialog , which) -> handleDialogEditPerson(vDialog, postition))
                .show();
    }

    private void handleDialogEditPerson(final View vDialog, int position) {
        EditText txtShot = vDialog.findViewById(R.id.name);
        String name = txtShot.getText().toString();

        Person oldPerson = persons.get(position);

        Person newPerson = updatePerson(oldPerson.getId(), oldPerson.getStammtsichID(), name, oldPerson.isAdmin());
        persons.set(position, newPerson);
        myAdapter.notifyDataSetChanged();
    }

    private void showNewPersonDialog(){
        final View vDialog = getLayoutInflater().inflate(R.layout.dialog_new_person, null );
        new AlertDialog.Builder(this)
                .setMessage("Name:")
                .setCancelable(false)
                .setView(vDialog)
                .setPositiveButton ("ok", (dialog , which) -> handleDialogNewPerson(vDialog))
                .show();
    }

    private void handleDialogNewPerson(final View vDialog) {
        EditText txtShot = vDialog.findViewById(R.id.name);
        String name = txtShot.getText().toString();

        Person newPerson = createPerson(name, stammtischId, false);
        saveStammtischIDsLocal(stammtischId, newPerson.getId());
        persons.add(newPerson);
        myAdapter.notifyDataSetChanged();
    }

    private Person createPerson(String name, int stammtischId, boolean isAdmin) {
        PersonCreateTask PersonCreateTask = new PersonCreateTask();
        try {
            String jsonString = PersonCreateTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "AutoIncrement", name, String.valueOf(stammtischId), String.valueOf(isAdmin)).get();
            JSONObject jsonObject = new JSONObject(jsonString).getJSONObject("person");

            int id = jsonObject.getInt("id");
            name = jsonObject.getString("name");
            stammtischId = jsonObject.getInt("stammtischID");
            isAdmin = jsonObject.getBoolean("isAdmin");

            return new Person(id, name, stammtischId, isAdmin);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private class PersonCreateTask extends AsyncTask<String, Integer, String> {

        private final String URL = "http://139.178.101.87/StammtischTest/api/functions/Person/createPerson.php";

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
            String jsonString = "{ \"PersonID\" : \"" + strings[0] + "\" , \"Name\" : \"" + strings[1] + "\" , \"StammtischID\" : \"" + strings[2] + "\" , \"isAdmin\" : \"" + strings[3] + "\" }";
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

    private class PersonReadFromStammtischTask extends AsyncTask<String, Integer, String> {

        private String URL = "http://139.178.101.87/StammtischTest/api/functions/Person/readPersonsFromStammtisch.php?id=<id>";

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



    private List<Person> readPersonsFromStammtisch(int stammtischId) {
        List<Person> persons = new ArrayList<>();

        PersonReadFromStammtischTask personReadFromStammtischTask = new PersonReadFromStammtischTask();
        try {
            String jsonString = personReadFromStammtischTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(stammtischId)).get();
            JSONArray jsonArray = new JSONObject(jsonString).getJSONArray("persons");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                int id = jsonObject.getInt("id");
                String name = jsonObject.getString("name");
                int stammtischID = jsonObject.getInt("stammtischID");
                boolean isAdmin = (jsonObject.getString("isAdmin").equals("0")) ? false : true;

                persons.add(new Person(id, name, stammtischID, isAdmin));
            }
            return persons;
        } catch (ExecutionException | InterruptedException | JSONException e){
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    private void saveStammtischIDsLocal(int stammtischId, int personId) {
        try {
            FileOutputStream fos = openFileOutput(fileName_IDS, MODE_PRIVATE | MODE_APPEND);
            PrintWriter out = new PrintWriter(new OutputStreamWriter(fos));
            out.println(stammtischId+","+personId+";");
            out.flush();
            out.close();
        } catch (FileNotFoundException exp) {
            exp.printStackTrace();
        }
    }

    private Map<Integer, Integer> readStammtischIDs() {
        try {
            FileInputStream fis = openFileInput(fileName_IDS);
            BufferedReader in = new BufferedReader(new InputStreamReader(fis));
            String line;
            String data = "";
            while ((line = in.readLine()) != null) {
                data += line;
            }
            in.close();

            Map<Integer, Integer> stammtischPersonen = new HashMap<>();
            String[] dataArr = data.split(";");
            for (String id : dataArr) {
                int stammtischId = Integer.valueOf(id.split(",")[0]);
                int personId = Integer.valueOf(id.split(",")[1]);

                stammtischPersonen.put(stammtischId, personId);
            }

            return stammtischPersonen;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }


    private void actionbarDesign(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String backgroundColour = prefs.getString("colour", "#6200EE");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(backgroundColour)));
    }

    private boolean deletePerson(int id) {
        PersonDeleteEndpunkt personDeleteEndpunkt = new PersonDeleteEndpunkt();
        try {
            String result = personDeleteEndpunkt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(id)).get();
            return !result.contains("failed");
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private class PersonDeleteEndpunkt extends AsyncTask<String, Integer, String> {

        private String URL = "http://139.178.101.87/StammtischTest/api/functions/Person/deletePerson.php?id=<id>";

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

    private Person updatePerson(int newID, int newStammtischID, String newName, boolean newIsAdmin) {
        try {
            PersonEditEndpunkt personEditEndpunkt = new PersonEditEndpunkt();
            String jsonString = personEditEndpunkt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(newID), String.valueOf(newStammtischID), newName, String.valueOf(newIsAdmin)).get();

            if (jsonString.contains("person was updated.")) {
                return new Person(newID, newName, newStammtischID, newIsAdmin);
            }
        }
        catch (ExecutionException | InterruptedException  e) {
            e.printStackTrace();
        }
        return null;

    }

    private class PersonEditEndpunkt extends AsyncTask<String, Integer, String> {

        private String URL = "http://139.178.101.87/StammtischTest/api/functions/Person/updatePerson.php";

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
            String jsonString = "{ \"PersonID\" : \"" + strings[0] + "\" , \"StammtischID\" : \"" + strings[1] + "\" , \"Name\" : \"" + strings[2] + "\" , \"isAdmin\" : \"" + strings[3] + "\" }";
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

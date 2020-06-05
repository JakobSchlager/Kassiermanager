package com.example.kassiermanager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MAIN";
    private ListView myListview;
    private List<Stammtisch> tables = new ArrayList<>();
    private TableListAdapter myAdapter;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myListview = findViewById(R.id.tablelistview);
        myAdapter = new TableListAdapter(this, R.layout.my_tables_list_layout, tables);
        myListview.setAdapter(myAdapter);
        registerForContextMenu(myListview);
        myListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
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

        if(item.getItemId() == R.id.context_showQRCode)
        {

            int pos = 0;

            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

            if(info != null)
            {
               pos = info.position;
            }



            showQRCode(tables.get(pos));
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
        }

        return super.onOptionsItemSelected(item);
    }

    private void scanCode()
    {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureAct.class);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scanning Code");
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);


        // hier bekommst du den Code des QR codes zurück, wenn dieser gescannt wurde !!!
        int id = Integer.valueOf(result.getContents());
        StammtischReadOneTask stammtischReadOneTask = new StammtischReadOneTask();
        try {
            JSONObject jsonObject = new JSONObject(stammtischReadOneTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(id)).get());
            Stammtisch newStammtisch = new Stammtisch(jsonObject.getString("name"), jsonObject.getInt("id"));
            tables.add(newStammtisch);
            myAdapter.notifyDataSetChanged();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    private void createNewTable()
    {
        final View vDialog = getLayoutInflater().inflate(R.layout.insert_tablename, null);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Name the Table")
                .setView(vDialog)
                .setNegativeButton("cancel", null)
                .setPositiveButton("ok", (dialog, which) -> handleDialog(vDialog))
                .show();
    }

    private void handleDialog(final View vDialog)
    {
        EditText txtName = vDialog.findViewById(R.id.txt_TableName);
        String tableName = txtName.getText().toString();


    // hier sollst du den Stammtisch an der Datenbank anlegen und die ID die du zurückbekommst in das Objekt speichern, diese Anlegen und in die Liste adden.



        tables.add(createStammtisch(tableName));
            myAdapter.notifyDataSetChanged();


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
            int newID = getNewID()+1;

            String jsonString = stammtischCreateTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(newID), name).get();
            JSONObject jsonObject = new JSONObject(jsonString);
            Stammtisch newStammtisch = new Stammtisch(jsonObject.getJSONObject("stammtisch").getString("name"), jsonObject.getJSONObject("stammtisch").getInt("id"));
            //PFUSCH:


            return newStammtisch;
        } catch (ExecutionException | InterruptedException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private int getNewID() throws JSONException, ExecutionException, InterruptedException {
        StammtischReadAllTask stammtischReadAllTask = new StammtischReadAllTask();
        String jsonString = stammtischReadAllTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();

        //getting all to a Object
        JSONObject jsonObject = new JSONObject(jsonString);
        //taking just the array of stammtische
        JSONArray jsonArray = jsonObject.getJSONArray("stammtische");
        //getting the lastindex of that array
        jsonObject = jsonArray.getJSONObject(jsonArray.length()-1);
        return jsonObject.getInt("id");
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
}

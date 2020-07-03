package com.example.kassiermanager.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.kassiermanager.Activitys.PersonListActivity;
import com.example.kassiermanager.Entities.Person;
import com.example.kassiermanager.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class NotificationService extends Service {

    final int notificationID = 1;

    List<Person> oldList;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags , int startId ) {
        Log.d("INFO:", "onStartCommand: Service: onStartCommand");

        createNotificationChannel();

        int stammtischID = intent.getIntExtra("stammtischID", 0);

        oldList = readPersonsFromStammtisch(stammtischID);

        new Thread(() -> {
            try {
                while(true) {
                    Thread.sleep(1000);
                    List<Person> newList = readPersonsFromStammtisch(stammtischID);
                    if (newList.size() > oldList.size()) {
                        Notification.Builder builder = new
                                Notification.Builder(this, "4911")
                                .setSmallIcon(android.R.drawable.star_big_on)
                                .setColor(Color.YELLOW)
                                .setContentTitle(getString(R.string.app_name))
                                .setContentText("NEW MEMBER!")
                                .setWhen(System.currentTimeMillis());

                        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        notificationManager.notify(notificationID, builder.build());

                        oldList = newList;
                    }

                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        return super.onStartCommand(intent, flags, startId );
    }
    @Override
    public void onCreate() {
        Log.d("INFO", "Service started ");
        super.onCreate();
    }
    @Override
    public void onDestroy() {
        Log.d("INFO", "Service destroyed");
        super.onDestroy();
    }

    private NotificationChannel createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //CharSequence name = getString(R.string.channel_name);
            //String description = getString(R.string.channel_description);
            CharSequence name = "JustANotification";
            String description = "JustANotificationDescription";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("4911", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager. class );
            notificationManager.createNotificationChannel (channel);

            return notificationManager.getNotificationChannel("4911");
        }
        return null;
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
}

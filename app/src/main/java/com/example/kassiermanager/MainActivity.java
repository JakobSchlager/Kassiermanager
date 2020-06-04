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
import android.os.Bundle;
import android.util.Log;
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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

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
        myListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showQRCode(tables.get(position));
            }
        });
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

        Stammtisch newStammtisch = new Stammtisch(tableName, 1);


        tables.add(newStammtisch);
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
}

package com.example.kassiermanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class QRCodeActivity extends AppCompatActivity {
    TextView tablename;
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_q_r_code);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String name = bundle.getString("name");
        byte[] byteArray = bundle.getByteArray("image");
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        tablename = findViewById(R.id.tableName);
        image = findViewById(R.id.imageView_QRCode);

        tablename.setText(name);
        image.setImageBitmap(bitmap);

    }
}

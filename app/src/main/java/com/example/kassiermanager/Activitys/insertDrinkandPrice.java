package com.example.kassiermanager.Activitys;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kassiermanager.Entities.DummyDrink;
import com.example.kassiermanager.R;

public class insertDrinkandPrice extends AppCompatActivity {

    Button btn1;
    Button btn2;
    Button btn3;
    Button btn4;
    Button btn5;
    Button btn6;
    Button btn7;
    Button btn8;
    Button btn9;
    Button btn0;
    Button btn_seperator;
    Button btn_check;
    Button btn_back;

    EditText txt_Name;
    TextView txt_Price;

    DummyDrink oldDrink = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_drinkand_price);

        actionbarDesign();

        btn1 = findViewById(R.id.btn_1);
        btn2 = findViewById(R.id.btn_2);
        btn3 = findViewById(R.id.btn_3);
        btn4 = findViewById(R.id.btn_4);
        btn5 = findViewById(R.id.btn_5);
        btn6 = findViewById(R.id.btn_6);
        btn7 = findViewById(R.id.btn_7);
        btn8 = findViewById(R.id.btn_8);
        btn9 = findViewById(R.id.btn_9);
        btn0 = findViewById(R.id.btn_0);
        btn_seperator = findViewById(R.id.btn_seperator);
        btn_check = findViewById(R.id.btn_check);
        btn_back = findViewById(R.id.btn_back);

        txt_Name = findViewById(R.id.txt_drink_name);
        txt_Price = findViewById(R.id.txt_drink_price);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null)
        {
            oldDrink = (DummyDrink) bundle.getSerializable("Drink");


            txt_Name.setText(oldDrink.getName());
            txt_Price.setText(oldDrink.getPrice().toString().replace(".", ","));


        }


        btn_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = txt_Name.getText().toString();
                String price = txt_Price.getText().toString();

                if(!name.isEmpty() && !price.isEmpty())
                {

                    DummyDrink drink = new DummyDrink(name, Double.valueOf(price.replace(",", ".")));

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("Drink", drink);
                    resultIntent.putExtra("OldDrink", oldDrink);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
                else{
                    Toast toast=Toast.makeText(getApplicationContext(),"Set a price and a name",Toast.LENGTH_SHORT);
                    toast.show();
                }

            }
        });




        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = txt_Price.getText().toString() + "1";

                if(shouldNext(temp))
                {
                    txt_Price.setText(temp);
                }

            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = txt_Price.getText().toString() + "2";

                if(shouldNext(temp))
                {
                    txt_Price.setText(temp);
                }

            }
        });
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = txt_Price.getText().toString() + "3";

                if(shouldNext(temp))
                {
                    txt_Price.setText(temp);
                }

            }
        });
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = txt_Price.getText().toString() + "4";

                if(shouldNext(temp))
                {
                    txt_Price.setText(temp);
                }

            }
        });
        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = txt_Price.getText().toString() + "5";

                if(shouldNext(temp))
                {
                    txt_Price.setText(temp);
                }

            }
        });
        btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = txt_Price.getText().toString() + "6";

                if(shouldNext(temp))
                {
                    txt_Price.setText(temp);
                }

            }
        });
        btn7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = txt_Price.getText().toString() + "7";

                if(shouldNext(temp))
                {
                    txt_Price.setText(temp);
                }

            }
        });
        btn8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = txt_Price.getText().toString() + "8";

                if(shouldNext(temp))
                {
                    txt_Price.setText(temp);
                }

            }
        });
        btn9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = txt_Price.getText().toString() + "9";

                if(shouldNext(temp))
                {
                    txt_Price.setText(temp);
                }

            }
        });
        btn0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = txt_Price.getText().toString() + "0";

                if(shouldNext(temp))
                {
                    txt_Price.setText(temp);
                }

            }
        });

        btn_seperator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = txt_Price.getText().toString();

                if(!temp.contains(",") && !temp.isEmpty())
                {
                    txt_Price.setText(temp + ",");
                }

            }
        });
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = txt_Price.getText().toString();

                if(temp.length() != 0)
                {
                    String result = temp.substring(0, temp.length()-1);
                    txt_Price.setText(result);
                }




            }
        });


    }

    private boolean shouldNext(String temp)
    {
        if(temp.contains(","))
        {
            String[] array = temp.split(",");

            if(array[1].length() > 2)
            {
                return false;
            }
            else
            {
                return true;
            }
        }
        else
        {
           return true;
        }
    }

    private void actionbarDesign(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String backgroundColour = prefs.getString("colour", "#6200EE");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(backgroundColour)));
    }
}

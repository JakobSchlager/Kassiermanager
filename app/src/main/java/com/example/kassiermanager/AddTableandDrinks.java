package com.example.kassiermanager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AddTableandDrinks extends AppCompatActivity {

    static final int INTEND_DRINK_REQUEST = 69;

    Button btn_submit;
    Button btn_add_Drink;
    ListView listView;
    EditText txt_Name;

    private List<Drink> drinkList = new ArrayList<>();
    private DrinkPriceAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_table_and_drinks);

        btn_add_Drink = findViewById(R.id.btn_add_Drink);
        btn_submit = findViewById(R.id.btn_submit_table);
        txt_Name = findViewById(R.id.txt_TableName);

        listView = findViewById(R.id.drinks_listview);
        myAdapter = new DrinkPriceAdapter(this, R.layout.drinksintables_list_layout, drinkList);
        listView.setAdapter(myAdapter);



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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check which request we're responding to
        if (requestCode == INTEND_DRINK_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {


                    assert data != null;
                    Drink drink = (Drink) data.getSerializableExtra("Drink");

                    drinkList.add(drink);
                    myAdapter.notifyDataSetChanged();



            }
        }
    }
}

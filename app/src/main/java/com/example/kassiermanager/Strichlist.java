package com.example.kassiermanager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Strichlist extends AppCompatActivity {

    static final int INTENT_REQUEST_CODE_DRINKS_TO_ADD_TO_STRICHLIST = 1337;

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

        Bundle bundle = getIntent().getExtras();
        Person person = (Person) bundle.getSerializable("Person");


        btn_Pay = findViewById(R.id.btn_pay);
        btn_AddDrink = findViewById(R.id.btn_add_drink);
        drinksSum = findViewById(R.id.txt_to_show_Sum);

        drinkListView = findViewById(R.id.strich_listview);
        myAdapter = new DrinkAmountAdapter(this, R.layout.my_drins_and_amount_list_layout, drinksAndAmount);
        drinkListView.setAdapter(myAdapter);

        drinksAndAmount.add(new DrinkPlusAmount("Bier", 3.50, 3));
        drinksAndAmount.add(new DrinkPlusAmount("Bier", 3.50, 3));
        drinksAndAmount.add(new DrinkPlusAmount("Bier", 3.50, 3));

        updateSum();

        myAdapter.notifyDataSetChanged();


        btn_Pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* new AlertDialog.Builder(getApplicationContext())
                        .setMessage("Are you sure you want to Pay " + getSum() + " €")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                drinksAndAmount.clear();
                                updateSum();
                                myAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();*/

                drinksAndAmount.clear();
                updateSum();
                myAdapter.notifyDataSetChanged();
            }
        });

        btn_AddDrink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<Drink> drinks = new ArrayList<>();
                drinks.add(new Drink(1, "Bier", 1, 3.5));
                //test weise

                Intent intent = new Intent(getApplicationContext(), AddDrinksToStrichlist.class);
                intent.putExtra("drinks", (Serializable) drinks);
                startActivityForResult(intent, INTENT_REQUEST_CODE_DRINKS_TO_ADD_TO_STRICHLIST);
            }
        });






    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode == INTENT_REQUEST_CODE_DRINKS_TO_ADD_TO_STRICHLIST)
        {
            if (resultCode == RESULT_OK)
            {

                DrinkPlusAmount drink = (DrinkPlusAmount) data.getSerializableExtra("myReturnDrink");

                drinksAndAmount.add(drink);
                updateSum();
                myAdapter.notifyDataSetChanged();
            }
        }


        super.onActivityResult(requestCode, resultCode, data);

    }

    private void updateSum()
    {
        Double sum = getSum();
        //String formatDouble = new DecimalFormat("#.#0 €").format(sum);
        drinksSum.setText(String.valueOf(sum) + " €");
    }

    private double getSum()
    {
        Double sum = drinksAndAmount.stream()
                .mapToDouble(a -> a.getPrice()*a.getAmount())
                .reduce(0, (b1, b2) -> b1+b2);

        return sum;
    }
}

package com.example.kassiermanager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AddTableandDrinks extends AppCompatActivity {

    static final int INTEND_DRINK_REQUEST = 69;
    static final int INTEND_DRINK_REQUEST_EDIT = 420;

    Button btn_submit;
    Button btn_add_Drink;
    ListView listView;
    EditText txt_Name;

    private List<DummyDrink> drinkList = new ArrayList<>();
    private DrinkPriceAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_table_and_drinks);

        btn_add_Drink = findViewById(R.id.btn_add_Drink);
        btn_submit = findViewById(R.id.btn_submit_table);
        txt_Name = findViewById(R.id.txt_TableName);

        listView = findViewById(R.id.drinks_listview);
        registerForContextMenu(listView);
        myAdapter = new DrinkPriceAdapter(this, R.layout.drinksintables_list_layout, drinkList);
        listView.setAdapter(myAdapter);



        Bundle bundle = getIntent().getExtras();

        if(bundle != null)
        {

            Stammtisch table = (Stammtisch) bundle.getSerializable("Stammtisch");

            // tabeles von DB und in Views einfügen.




        }



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
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.drinks_in_tables_edit)
        {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

            if(info != null)
            {
                int pos = info.position;

                DummyDrink drink = (DummyDrink) listView.getAdapter().getItem(pos);

                Intent intent = new Intent(getApplication(), insertDrinkandPrice.class);
                intent.putExtra("Drink", drink);
                startActivityForResult(intent, INTEND_DRINK_REQUEST_EDIT);
            }
            return true;
        }
        if(item.getItemId() == R.id.drinks_in_tables_delete)
        {

            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

            if(info != null)
            {
                int pos = info.position;

                DummyDrink drink = (DummyDrink) listView.getAdapter().getItem(pos);

                drinkList.remove(drink);
                myAdapter.notifyDataSetChanged();
            }




            return true;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        int viewID = v.getId();

        if(viewID == R.id.drinks_listview)
        {
            getMenuInflater().inflate(R.menu.context_drinks_table, menu);
        }

        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check which request we're responding to
        if (requestCode == INTEND_DRINK_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {


                    assert data != null;
                    DummyDrink drink = (DummyDrink) data.getSerializableExtra("Drink");

                    drinkList.add(drink);
                    myAdapter.notifyDataSetChanged();



            }
        }
        if(requestCode == INTEND_DRINK_REQUEST_EDIT)
        {
            if(resultCode == RESULT_OK)
            {

                assert data != null;
                DummyDrink drink = (DummyDrink) data.getSerializableExtra("Drink");
                DummyDrink oldDrink = (DummyDrink) data.getSerializableExtra("OldDrink");

                drinkList.add(drink);
                drinkList.remove(oldDrink);
                myAdapter.notifyDataSetChanged();
            }

        }
    }
}

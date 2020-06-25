package com.example.kassiermanager.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.kassiermanager.Entities.Person;
import com.example.kassiermanager.R;

import java.util.ArrayList;
import java.util.List;

public class PersonListAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private int layoutID;
    private List<Person> persons = new ArrayList<>();

    public PersonListAdapter(Context ctx, int layoutID, List<Person> persons) {
        this.inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.layoutID = layoutID;
        this.persons = persons;
    }

    @Override
    public int getCount() {
        return persons.size();
    }

    @Override
    public Object getItem(int position) {
        return persons.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Person person = persons.get(position);
        View listItem = (convertView == null) ? inflater.inflate(this.layoutID, null) : convertView;
        ((TextView) listItem.findViewById(R.id.personsName)).setText(person.getName());

        return listItem;
    }
}

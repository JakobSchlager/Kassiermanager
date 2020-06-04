package com.example.kassiermanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class TableListAdapter extends BaseAdapter {

   LayoutInflater inflater;
   int layoutId;
   List<Stammtisch> tables = new ArrayList<>();

    public TableListAdapter(Context context, int layoutId, List<Stammtisch> tables) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.layoutId = layoutId;
        this.tables = tables;
    }

    @Override
    public int getCount() {
        return tables.size();
    }

    @Override
    public Object getItem(int position) {
        return tables.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Stammtisch table = tables.get(position);
        View listItem = (convertView == null) ? inflater.inflate(this.layoutId, null) : convertView;
        ((TextView) listItem.findViewById(R.id.tableName)).setText(table.getName());

        return listItem;
    }
}

package com.example.khalifa.infractiontracker.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.khalifa.infractiontracker.InfractionDetailsActivity;
import com.example.khalifa.infractiontracker.R;
import com.squareup.picasso.Picasso;

import java.util.List;


public class GroupDetailsAdapter extends RecyclerView.Adapter<GroupDetailsAdapter.StoreViewHolder> {
    private List<User> users;
    private Activity activity;


    public GroupDetailsAdapter(List<User> users, Activity activity) {
        this.users = users;
        this.activity = activity;
    }

    @Override
    public StoreViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_item, parent, false);

        return new StoreViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(StoreViewHolder viewHolder, int position) {
        User model = users.get(position);
        viewHolder.setName(model.getName());
        viewHolder.position = position;
    }


    @Override
    public int getItemCount() {
        return users.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public class StoreViewHolder extends RecyclerView.ViewHolder {
        View mView;
        public int position;

        public StoreViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name) {
            TextView textView = (TextView) mView.findViewById(R.id.name);
            textView.setText(name);
        }

    }

}

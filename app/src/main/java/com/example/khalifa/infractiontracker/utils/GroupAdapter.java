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

import com.example.khalifa.infractiontracker.GroupDetailsActivity;
import com.example.khalifa.infractiontracker.InfractionDetailsActivity;
import com.example.khalifa.infractiontracker.R;
import com.squareup.picasso.Picasso;

import java.util.List;


public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.StoreViewHolder> {
    private List<Group> groups;
    private Activity activity;


    public GroupAdapter(List<Group> groups, Activity activity) {
        this.groups = groups;
        this.activity = activity;
    }

    @Override
    public StoreViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_item, parent, false);

        return new StoreViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(StoreViewHolder viewHolder, int position) {
        Group model = groups.get(position);
        viewHolder.setName(model.getName());
        viewHolder.setImage(activity, model.getImage());
        viewHolder.setTotalMembers(model.getTotalUsers());
        viewHolder.position = position;
    }


    @Override
    public int getItemCount() {
        return groups.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public class StoreViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View mView;
        public int position;

        public StoreViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name) {
            TextView textView = (TextView) mView.findViewById(R.id.name);
            textView.setOnClickListener(this);
            textView.setText(name);
        }


        public void setTotalMembers(int totalMembers) {
            TextView textView = (TextView) mView.findViewById(R.id.members);
            textView.setOnClickListener(this);
            textView.setText("Total Members: " + totalMembers);
        }


        public void setImage(Context ctx, String image) {
            ImageView imageView = (ImageView) mView.findViewById(R.id.image);
            imageView.setOnClickListener(this);
            Picasso.with(ctx).load(image).into(imageView);
        }


        @Override
        public void onClick(View v) {
            Intent intent = new Intent(activity, GroupDetailsActivity.class);
            intent.putExtra("group", groups.get(position));
            activity.startActivity(intent);
        }

    }

}

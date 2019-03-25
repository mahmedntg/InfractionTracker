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

/**
 * Created by mhamedsayed on 3/15/2019.
 */

public class InfractionAdapter extends RecyclerView.Adapter<InfractionAdapter.StoreViewHolder> {
    private List<Infraction> infractions;
    private Activity activity;


    public InfractionAdapter(List<Infraction> infractions, Activity activity) {
        this.infractions = infractions;
        this.activity = activity;
    }

    @Override
    public StoreViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.infraction_item, parent, false);

        return new StoreViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(StoreViewHolder viewHolder, int position) {
        Infraction model = infractions.get(position);
        viewHolder.setName(model.getName());
        viewHolder.setImage(activity, model.getImage());
        viewHolder.setStatus(model.getStatus());
        viewHolder.setCategory(model.getCategory());

        viewHolder.position = position;
    }


    @Override
    public int getItemCount() {
        return infractions.size();
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


        public void setStatus(String status) {
            TextView textView = (TextView) mView.findViewById(R.id.status);
            textView.setOnClickListener(this);
            textView.setText(status);
        }

        public void setCategory(String category) {
            TextView textView = (TextView) mView.findViewById(R.id.category);
            textView.setOnClickListener(this);
            textView.setText(category);
        }


        public void setImage(Context ctx, String image) {
            ImageView imageView = (ImageView) mView.findViewById(R.id.image);
            imageView.setOnClickListener(this);
            Picasso.with(ctx).load(image).into(imageView);
        }


        @Override
        public void onClick(View v) {
            Intent intent = new Intent(activity, InfractionDetailsActivity.class);
            intent.putExtra("infraction", infractions.get(position));
            activity.startActivity(intent);
        }

    }

}

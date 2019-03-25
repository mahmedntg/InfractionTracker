package com.example.khalifa.infractiontracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.khalifa.infractiontracker.utils.Group;
import com.example.khalifa.infractiontracker.utils.GroupAdapter;
import com.example.khalifa.infractiontracker.utils.GroupDetailsAdapter;
import com.example.khalifa.infractiontracker.utils.Infraction;
import com.example.khalifa.infractiontracker.utils.Reference;
import com.example.khalifa.infractiontracker.utils.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupDetailsActivity extends AppCompatActivity implements ValueEventListener {
    private ImageView imageView;
    private TextView descriptionTV;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private FirebaseDatabase database;
    private DatabaseReference userReference;
    private Group group;
    private List<User> users;
    private GroupDetailsAdapter mAdapter;
    private boolean allowedToJoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        imageView = findViewById(R.id.image);
        descriptionTV = findViewById(R.id.description);
        Bundle extras = getIntent().getExtras();
        group = (Group) extras.get("group");
        descriptionTV.setText("Description: " + group.getDescription());
        Picasso.with(this).load(group.getImage()).into(imageView);
        firebaseAuth.getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();
        userReference = database.getReference(Reference.USERS);
        DatabaseReference databaseReference = database.getReference(Reference.USER_GROUPS);
        databaseReference.child(group.getKey()).addListenerForSingleValueEvent(this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.myRecyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        users = new ArrayList<>();
        mAdapter = new GroupDetailsAdapter(users, GroupDetailsActivity.this);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        users.clear();
        for (DataSnapshot data : dataSnapshot.getChildren()) {
            userReference.child(data.getKey().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    users.add(dataSnapshot.getValue(User.class));
                    mAdapter.notifyDataSetChanged();
                    isAllowedToJoin();
                    invalidateOptionsMenu();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

            });


        }
        progressDialog.hide();

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        progressDialog.hide();
    }

    public boolean isAllowedToJoin() {
        if (firebaseAuth.getCurrentUser().getUid().equals(group.getUserId())) {
            allowedToJoin = false;
        } else if (users.contains(new User(firebaseAuth.getCurrentUser().getEmail()))) {
            allowedToJoin = false;
        } else {
            allowedToJoin = true;
        }

        return allowedToJoin;
    }

    public void setAllowedToJoin(boolean allowedToJoin) {
        this.allowedToJoin = allowedToJoin;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        if (isAllowedToJoin()) menu.findItem(R.id.joinInGroupItemMenu).setVisible(true);
        MenuBuilder menuBuilder = (MenuBuilder) menu;
        menuBuilder.setOptionalIconsVisible(true);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.signOutItemMenu:
                firebaseAuth.signOut();
                startActivity(new Intent(this, LoginActivity.class));
                return true;
            case R.id.joinInGroupItemMenu:
                joinGroup();
                startActivity(new Intent(this, GroupActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void joinGroup() {
        DatabaseReference reference = database.getReference(Reference.USER_GROUPS + "/" + group.getKey());
        Map<String, Object> data = new HashMap<>();
        data.put(firebaseAuth.getCurrentUser().getUid(), firebaseAuth.getCurrentUser().getEmail());
        reference.updateChildren(data);
        database.getReference(Reference.GROUPS + "/" + group.getKey()).child("totalUsers").setValue(group.getTotalUsers() + 1);
        sendNotification();
    }

    private void sendNotification() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.cancel();
        }
    }
}

package com.example.khalifa.infractiontracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.khalifa.infractiontracker.utils.Group;
import com.example.khalifa.infractiontracker.utils.GroupAdapter;
import com.example.khalifa.infractiontracker.utils.Infraction;
import com.example.khalifa.infractiontracker.utils.InfractionAdapter;
import com.example.khalifa.infractiontracker.utils.Reference;
import com.example.khalifa.infractiontracker.utils.SharedUtils;
import com.example.khalifa.infractiontracker.utils.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupActivity extends AppCompatActivity implements ValueEventListener {
    ArrayList<Group> groupList;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private GroupAdapter mAdapter;
    private ProgressDialog progressDialog;
    private boolean adminUser = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        databaseReference = FirebaseDatabase.getInstance().getReference(Reference.GROUPS);
        groupList = new ArrayList<>();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.myRecyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new GroupAdapter(groupList, GroupActivity.this);
        recyclerView.setAdapter(mAdapter);
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(GroupActivity.this, CreateGroupActivity.class);
                startActivity(newIntent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayGroups();
    }

    private void displayGroups() {
        Query groupQuery = databaseReference;
        groupQuery.addListenerForSingleValueEvent(this);

    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        groupList.clear();
        for (DataSnapshot data : dataSnapshot.getChildren()) {
            Group group = data.getValue(Group.class);
            group.setKey(data.getKey());
            groupList.add(group);

        }
        mAdapter.notifyDataSetChanged();
        progressDialog.hide();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        progressDialog.hide();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.cancel();
        }
    }
}

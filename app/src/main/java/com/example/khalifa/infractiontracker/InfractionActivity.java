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
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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
import java.util.List;

public class InfractionActivity extends AppCompatActivity implements ValueEventListener, AdapterView.OnItemSelectedListener {
    ArrayList<Infraction> infractionList;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private InfractionAdapter mAdapter;
    private ProgressDialog progressDialog;
    private boolean adminUser = true;
    private Spinner categorySpinner;
    List<String> categoryList = new ArrayList<>();
    private String categoryName;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infraction);
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        databaseReference = FirebaseDatabase.getInstance().getReference(Reference.INFRACTIONS);
        categorySpinner = (Spinner) findViewById(R.id.category);
        final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, categoryList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(dataAdapter);
        categoryList.add(getString(R.string.all));
        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference(Reference.CATEGORY);
        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    categoryList.add(postSnapshot.child("name").getValue(String.class));
                }
                dataAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        categorySpinner.setOnItemSelectedListener(this);
        infractionList = new ArrayList<>();
         recyclerView = (RecyclerView) findViewById(R.id.myRecyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new InfractionAdapter(infractionList, InfractionActivity.this);
        recyclerView.setAdapter(mAdapter);
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        String email = firebaseAuth.getCurrentUser().getEmail();
        if (!email.equals(SharedUtils.email)) {
            fab.setVisibility(View.VISIBLE);
            adminUser = false;
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(InfractionActivity.this, AddInfractionActivity.class);
                startActivity(newIntent);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && fab.getVisibility() == View.VISIBLE) {
                    fab.hide();
                } else if (dy < 0 && fab.getVisibility() != View.VISIBLE) {
                    fab.show();
                }
            }
        });
    }

    private void displayInfractions() {
        Query infractionsQuery = databaseReference;
        if (adminUser) {
            if (!categoryName.equalsIgnoreCase(getString(R.string.all))) {
                infractionsQuery = databaseReference.orderByChild("category").equalTo(categoryName);
            }
        } else {
            infractionsQuery = databaseReference.orderByChild("userId").equalTo(firebaseAuth.getCurrentUser().getUid());
            if (!categoryName.equalsIgnoreCase(getString(R.string.all))) {
                infractionsQuery = databaseReference.orderByChild("userid_category").equalTo(firebaseAuth.getCurrentUser().getUid() + "__" + categoryName);
            }
        }
        infractionsQuery.addListenerForSingleValueEvent(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        if (!adminUser) menu.findItem(R.id.GroupItemMenu).setVisible(true);
        MenuBuilder menuBuilder = (MenuBuilder) menu;
        menuBuilder.setOptionalIconsVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.signOutItemMenu:
                firebaseAuth.signOut();
                startActivity(new Intent(this, MainActivity.class));
                return true;
            case R.id.GroupItemMenu:
                startActivity(new Intent(this, GroupActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        infractionList.clear();

        Log.w("TodoApp", "getUser:onCancelled " + dataSnapshot.toString());
        Log.w("TodoApp", "count = " + String.valueOf(dataSnapshot.getChildrenCount()) + " values " + dataSnapshot.getKey());
        for (DataSnapshot data : dataSnapshot.getChildren()) {
            Infraction infraction = data.getValue(Infraction.class);
            if (adminUser && infraction.getStatus().equals(Status.PENDING.getValue())) {
                infraction.setKey(data.getKey());
                infractionList.add(infraction);
            } else if (!adminUser) {
                infraction.setKey(data.getKey());
                infractionList.add(infraction);
            }
        }
        LayoutAnimationController layout_animation =
                AnimationUtils.loadLayoutAnimation(getApplicationContext(), R.anim.layout_animation);

        recyclerView.setLayoutAnimation(layout_animation);
        mAdapter.notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
        progressDialog.hide();

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.w("TodoApp", "getUser:onCancelled", databaseError.toException());
        progressDialog.hide();

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        categoryName = parent.getItemAtPosition(position).toString();
        displayInfractions();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        categoryName = getString(R.string.all);
        displayInfractions();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.cancel();
        }
    }
}

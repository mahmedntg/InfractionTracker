package com.example.khalifa.infractiontracker;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.khalifa.infractiontracker.utils.Infraction;
import com.example.khalifa.infractiontracker.utils.Reference;
import com.example.khalifa.infractiontracker.utils.SharedUtils;
import com.example.khalifa.infractiontracker.utils.Status;
import com.example.khalifa.infractiontracker.utils.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class InfractionDetailsActivity extends AppCompatActivity implements View.OnClickListener, ValueEventListener {
    private ImageView imageView;
    private TextView descriptionTV, userNameTV, userEmailTV, userPhoneTV, commentTV, solutionTV;
    private EditText commentET;
    private Button approve, reject, openLocation;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    int fromYear, fromMonth, fromDay;
    int toYear, toMonth, toDay;
    int year, month, day;
    private DatabaseReference databaseReference;
    private Infraction infraction;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infraction_details);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = database.getReference(Reference.INFRACTIONS);
        DatabaseReference userReference = database.getReference(Reference.USERS);
        userReference.child(firebaseAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        imageView = findViewById(R.id.image);
        userEmailTV = findViewById(R.id.user_email);
        userPhoneTV = findViewById(R.id.user_phone);
        userNameTV = findViewById(R.id.user_name);
        descriptionTV = findViewById(R.id.description);
        solutionTV = findViewById(R.id.solution);
        commentET = findViewById(R.id.commentET);
        commentTV = findViewById(R.id.commentTV);
        approve = findViewById(R.id.approve);
        reject = findViewById(R.id.reject);
        openLocation = findViewById(R.id.openLocation);
        openLocation.setOnClickListener(this);

        Bundle extras = getIntent().getExtras();
        infraction = (Infraction) extras.get("infraction");
        solutionTV.setText("Solution: " + infraction.getSolution());
        descriptionTV.setText("Description: " + infraction.getDescription());

        if (!infraction.getImage().contains("http")) {
            try {
                Bitmap imageBitmap = SharedUtils.decodeFromFirebaseBase64(infraction.getImage());
                imageView.setImageBitmap(imageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

            Picasso.with(this).load(infraction.getImage()).into(imageView);
        }
        if (infraction.getSolution() == null || infraction.getSolution().equals("")) {
            solutionTV.setVisibility(View.GONE);
        }
        String email = firebaseAuth.getCurrentUser().getEmail();
        if (email.equals(SharedUtils.email)) {
            approve.setVisibility(View.VISIBLE);
            reject.setVisibility(View.VISIBLE);
            approve.setOnClickListener(this);
            reject.setOnClickListener(this);
            commentTV.setVisibility(View.GONE);

        } else {
            commentET.setVisibility(View.GONE);
            findViewById(R.id.commentView).setVisibility(View.GONE);
            userNameTV.setVisibility(View.GONE);
            userEmailTV.setVisibility(View.GONE);
            userPhoneTV.setVisibility(View.GONE);
            if (infraction.getAdminComment() == null || infraction.getAdminComment().equals("")) {
                commentTV.setVisibility(View.GONE);
            }
            commentTV.setText("Admin Comments: " + infraction.getAdminComment());
        }
    }

    @Override
    public void onClick(View view) {

        if (view == approve) {
            updateStatus(Status.APPROVED);

        } else if (view == reject) {
            updateStatus(Status.REJECTED);
        } else {
            openLocation();
        }
    }

    private void updateStatus(Status status) {
        progressDialog.setMessage("Update Infraction...");
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        Map<String, Object> data = new HashMap<>();
        data.put("status", status.getValue());
        data.put("adminComment", commentET.getText().toString());
        databaseReference.child(infraction.getKey()).updateChildren(data);
        progressDialog.hide();
        startActivity(new Intent(this, InfractionActivity.class));
        sendNotification();
    }

    private void openLocation() {
        String strUri = "http://maps.google.com/maps?q=loc:" + infraction.getLatitude() + "," + infraction.getLongitude() + " (" + infraction.getName() + ")";
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));

        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");

        startActivity(intent);
    }

    private void sendNotification() {

    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        user = dataSnapshot.getValue(User.class);
        userNameTV.setText("user Name: " + user.getName());
        userEmailTV.setText("user Email: " + user.getEmail());
        userPhoneTV.setText("user Phone: " + user.getPhone());
        progressDialog.hide();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        progressDialog.hide();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.cancel();
        }
    }
}

package com.example.khalifa.infractiontracker;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.khalifa.infractiontracker.utils.GPS_Service;
import com.example.khalifa.infractiontracker.utils.Infraction;
import com.example.khalifa.infractiontracker.utils.Reference;
import com.example.khalifa.infractiontracker.utils.Status;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class AddInfractionActivity extends AppCompatActivity {
    private ImageButton imageBtn;
    private EditText nameET, descriptionET, solutionET;
    private Spinner categorySpinner;
    private Uri uri = null;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    List<String> categoryList = new ArrayList<>();
    private ProgressDialog progressDialog;
    GPS_Service gps;
    private double latitude, longitude;
    private String imageEncoded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_infraction);
        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference(Reference.INFRACTIONS);
        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference(Reference.CATEGORY);
        nameET = (EditText) findViewById(R.id.name);
        descriptionET = (EditText) findViewById(R.id.description);
        solutionET = (EditText) findViewById(R.id.solution);
        categorySpinner = (Spinner) findViewById(R.id.category);
        final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                R.layout.item_spinner, categoryList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(dataAdapter);
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
        findViewById(R.id.addStore_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!nameET.getText().toString().equals("") && !descriptionET.getText().toString().equals("")) {
                    addInfraction(nameET.getText().toString(), descriptionET.getText().toString(), solutionET.getText().toString());
                } else {
                    Toast.makeText(getApplicationContext(), "Please fill all boxes", Toast.LENGTH_LONG).show();
                }
            }
        });


    }

    public void addInfraction(final String name, final String description, final String solution) {

        progressDialog.setMessage("Adding Infraction ...");
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        // get location
        getLocation();
        final DatabaseReference ref = databaseReference.push();
        Infraction infraction = new Infraction(firebaseAuth.getCurrentUser().getUid(), name, solution, imageEncoded, categorySpinner.getSelectedItem().toString(), description, latitude, longitude);
        infraction.setStatus(Status.PENDING.getValue());
        infraction.setUserid_category(infraction.getUserId() + "__" + infraction.getCategory());
        ref.setValue(infraction);
        progressDialog.dismiss();
        startActivity(new Intent(AddInfractionActivity.this, InfractionActivity.class));
        /*
        StorageReference filePath = storageReference.child(uri.getLastPathSegment());
        final String userUid = firebaseAuth.getCurrentUser().getUid();
        filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                final DatabaseReference ref = databaseReference.push();
                Infraction infraction = new Infraction(firebaseAuth.getCurrentUser().getUid(), name, solution, downloadUrl.toString(), categorySpinner.getSelectedItem().toString(), description, latitude, longitude);
                infraction.setStatus(Status.PENDING.getValue());
                infraction.setUserid_category(infraction.getUserId() + "__" + infraction.getCategory());
                ref.setValue(infraction);
                progressDialog.dismiss();
                startActivity(new Intent(AddInfractionActivity.this, InfractionActivity.class));
            }
        });*/
    }

    public void imageBtnClicked(View view) {
        Intent galleryIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(galleryIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            imageBtn = (ImageButton) findViewById(R.id.storeImageBtn);
            imageBtn.setImageBitmap(imageBitmap);
            imageEncoded = encodeBitmapAndSaveToFirebase(imageBitmap);
        }
    }

    public String encodeBitmapAndSaveToFirebase(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    private void getLocation() {
        String tim = "0";
        gps = new GPS_Service(this, tim);
        startService(new Intent(this, GPS_Service.class));
        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

        } else {
            gps.showSettingsAlert();
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

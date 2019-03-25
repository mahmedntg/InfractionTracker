package com.example.khalifa.infractiontracker;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.khalifa.infractiontracker.utils.GPS_Service;
import com.example.khalifa.infractiontracker.utils.Group;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CreateGroupActivity extends AppCompatActivity {

    private ImageButton imageBtn;
    private EditText nameET, descriptionET;
    private Uri uri = null;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private AlertDialog alertDialog;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference(Reference.GROUPS);
        nameET = (EditText) findViewById(R.id.name);
        descriptionET = (EditText) findViewById(R.id.description);
        findViewById(R.id.createGroupBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!nameET.getText().toString().equals("") && !descriptionET.getText().toString().equals("")) {
                    createGroup(nameET.getText().toString(), descriptionET.getText().toString());
                } else {
                    Toast.makeText(getApplicationContext(), "Please fill all boxes", Toast.LENGTH_LONG).show();
                }
            }
        });


    }

    public void createGroup(final String name, final String description) {

        progressDialog.setMessage("Creating Group ...");
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        StorageReference filePath = storageReference.child(uri.getLastPathSegment());
        final String userUid = firebaseAuth.getCurrentUser().getUid();
        filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                final DatabaseReference ref = databaseReference.push();
                Group group = new Group(name, description, firebaseAuth.getCurrentUser().getUid(), downloadUrl.toString());
                ref.setValue(group);
                progressDialog.dismiss();
                startActivity(new Intent(CreateGroupActivity.this, GroupActivity.class));
            }
        });
    }

    public void imageBtnClicked(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            uri = data.getData();
            imageBtn = (ImageButton) findViewById(R.id.storeImageBtn);
            imageBtn.setImageURI(uri);
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

package com.example.khalifa.infractiontracker;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.khalifa.infractiontracker.utils.Reference;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    EditText email, password;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private AlertDialog alertDialog;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        runtime_permission();
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);
        alertDialogBuilder.setTitle("data not valid");
        alertDialogBuilder
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        alertDialog = alertDialogBuilder.create();
        email = findViewById(R.id.user_email);
        password = findViewById(R.id.user_password);
        findViewById(R.id.login_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!email.getText().toString().equals("") && !password.getText().toString().equals("")) {
                    login(email.getText().toString(), password.getText().toString());
                } else {
                    Toast.makeText(getApplicationContext(), "Please fill all boxes", Toast.LENGTH_LONG).show();
                }
            }
        });

        findViewById(R.id.register_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToRegisterAC();
            }
        });
    }

    public void login(String email, String pass) {
        progressDialog.setMessage("Login, Please Wait...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String userId = firebaseAuth.getCurrentUser().getUid();
                    firebaseDatabase.getReference(Reference.USERS).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            startActivity(new Intent(LoginActivity.this, InfractionActivity

                                    .class));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    progressDialog.hide();
                } else {
                    progressDialog.hide();
                    alertDialog.setMessage(task.getException().getMessage());
                    alertDialog.show();
                }

            }
        });

    }

    private boolean runtime_permission() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 123);
            return true;
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                return;
            } else {
                runtime_permission();
            }
        }
    }

    private void navigateToRegisterAC() {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.cancel();
        }
    }
}


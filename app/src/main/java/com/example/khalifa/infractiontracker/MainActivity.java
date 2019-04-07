package com.example.khalifa.infractiontracker;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.khalifa.infractiontracker.utils.Reference;
import com.example.khalifa.infractiontracker.utils.SharedUtils;
import com.example.khalifa.infractiontracker.utils.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import it.sephiroth.android.library.easing.Back;
import it.sephiroth.android.library.easing.EasingManager;

public class MainActivity extends AppCompatActivity {
    private ViewGroup rootLayout;
    EditText nameS, email, password, emailS, passwordS, passwordC;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private AlertDialog alertDialog;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootLayout = (ViewGroup) findViewById(R.id.main_container);

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);

        nameS = (EditText) findViewById(R.id.name);
        emailS = (EditText) findViewById(R.id.user_email);
        passwordS = (EditText) findViewById(R.id.user_password);
        passwordC = (EditText) findViewById(R.id.user_confirm_password);


        runtime_permission();
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference(Reference.USERS);
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
                if (!nameS.getText().toString().equals("") && !emailS.getText().toString().equals("") && !passwordS.getText().toString().equals("") &&
                        !passwordC.getText().toString().equals("")) {
                    if (!passwordS.getText().toString().equals(passwordC.getText().toString())) {
                        Toast.makeText(getApplicationContext(), "Password and Confirm Password do not match", Toast.LENGTH_LONG).show();
                        return;
                    }
                    register(emailS.getText().toString(), passwordS.getText().toString(), nameS.getText().toString());
                } else {
                    Toast.makeText(getApplicationContext(), "Please fill all boxes", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void register(final String email, String pass, final String name) {
        progressDialog.setMessage("Registering User...");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String userId = firebaseAuth.getCurrentUser().getUid();
                    DatabaseReference ref = databaseReference.child(userId);
                    User user = new User(name, email, null);
                    ref.setValue(user);
                    showLogIn(findViewById(R.id.login_button));
                   // Toast.makeText(getApplicationContext(), "User Registered Successfully", Toast.LENGTH_LONG).show();
                    //startActivity(new Intent(MainActivity.this, LoginActivity.class));
                } else {
                    alertDialog.setMessage(task.getException().getMessage());
                    alertDialog.show();
                }
                progressDialog.hide();

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.cancel();
        }
    }

    public void login(String email, String pass) {
        progressDialog.setMessage("Login, Please Wait...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    final String userId = firebaseAuth.getCurrentUser().getUid();
                    firebaseDatabase.getReference(Reference.USERS).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                            String token = preferences.getString(SharedUtils.PREF_FCM_TOKEN, null);
                            dataSnapshot.getRef().child("token").setValue(token);
                            startActivity(new Intent(MainActivity.this, InfractionActivity
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

    public void showSingUp(View view) {
        final CardView animationCircle = (CardView) findViewById(R.id.animation_circle);
        final View animationFirstArist = findViewById(R.id.animation_first_arist);
        final View animationSecondArist = findViewById(R.id.animation_second_arist);
        final View animationSquare = findViewById(R.id.animation_square);
        final LinearLayout squareParent = (LinearLayout) animationSquare.getParent();
        final TextView animationTV = (TextView) findViewById(R.id.animation_tv);

        final View singupFormContainer = findViewById(R.id.signup_form_container);
        final View loginFormContainer = findViewById(R.id.login_form_container);
        final int backgroundColor = ContextCompat.getColor(this, R.color.bg);

        final float scale = getResources().getDisplayMetrics().density;

        final int circle_curr_margin = (int) (82 * scale + 0.5f);
        final int circle_target_margin = rootLayout.getWidth() - ((int) (70 * scale + 0.5f));

        final int first_curr_width = (int) (120 * scale + 0.5f);
        final int first_target_width = (int) (rootLayout.getHeight() * 1.3);

        final int first_curr_height = (int) (70 * scale + 0.5f);
        final int first_target_height = rootLayout.getWidth();

        final int first_curr_margin = (int) (40 * scale + 0.5f);
        final int first_target_margin = (int) (35 * scale + 0.5f);
        final int first_expand_margin = (first_curr_margin - first_target_height);

        final int square_target_width = rootLayout.getWidth();
        final int square_target_height = (int) (80 * scale + 0.5f);

        final float tv_curr_x = findViewById(R.id.singup_tv).getX() + findViewById(R.id.singup_button).getX();
        final float tv_curr_y = findViewById(R.id.singup_tv).getY() + findViewById(R.id.buttons_container).getY() + findViewById(R.id.singup_container).getY();


        final float tv_curr_size = 16;
        final float tv_target_size = 56;

        final int tv_curr_color = Color.parseColor("#ffffff");
        final int tv_target_color = Color.parseColor("#5cffffff");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        squareParent.setGravity(Gravity.END);
        animationTV.setText("register");

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                int diff_margin = circle_curr_margin - circle_target_margin;
                int margin = circle_target_margin + (int) (diff_margin - (diff_margin * interpolatedTime));

                RelativeLayout.LayoutParams params_circle = (RelativeLayout.LayoutParams) animationCircle.getLayoutParams();
                params_circle.setMargins(0, 0, margin, (int) (40 * scale + 0.5f));
                animationCircle.requestLayout();


                int diff_width = first_curr_width - first_target_width;
                int width = first_target_width + (int) (diff_width - (diff_width * interpolatedTime));

                int diff_height = first_curr_height - first_target_height;
                int height = first_target_height + (int) (diff_height - ((diff_height - first_target_margin) * interpolatedTime));

                diff_margin = first_curr_margin - first_expand_margin;
                margin = first_expand_margin + (int) (diff_margin - (diff_margin * interpolatedTime));
                int margin_r = (int) (-(first_target_width - rootLayout.getWidth()) * interpolatedTime);

                RelativeLayout.LayoutParams params_first = (RelativeLayout.LayoutParams) animationFirstArist.getLayoutParams();
                params_first.setMargins(0, 0, margin_r, margin);
                params_first.width = width;
                params_first.height = height;
                animationFirstArist.requestLayout();

                animationFirstArist.setPivotX(0);
                animationFirstArist.setPivotY(0);
                animationFirstArist.setRotation(-90 * interpolatedTime);


                margin = first_curr_margin + (int) (first_target_margin * interpolatedTime);
                RelativeLayout.LayoutParams params_second = (RelativeLayout.LayoutParams) animationSecondArist.getLayoutParams();
                params_second.setMargins(0, 0, margin_r, margin);
                params_second.width = width;
                animationSecondArist.requestLayout();

                animationSecondArist.setPivotX(0);
                animationSecondArist.setPivotY(animationSecondArist.getHeight());
                animationSecondArist.setRotation(90 * interpolatedTime);

                animationSquare.getLayoutParams().width = (int) (square_target_width * interpolatedTime);
                animationSquare.requestLayout();
                animationTV.requestLayout();


                singupFormContainer.setAlpha(interpolatedTime);
                loginFormContainer.setAlpha(1 - interpolatedTime);

            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation arg0) {
                findViewById(R.id.singup_container).setVisibility(View.INVISIBLE);
                animationCircle.setVisibility(View.VISIBLE);
                animationFirstArist.setVisibility(View.VISIBLE);
                animationSecondArist.setVisibility(View.VISIBLE);
                animationSquare.setVisibility(View.VISIBLE);
                animationTV.setVisibility(View.VISIBLE);
                singupFormContainer.setVisibility(View.VISIBLE);

                animationFirstArist.bringToFront();
                squareParent.bringToFront();
                animationSecondArist.bringToFront();
                animationCircle.bringToFront();
                findViewById(R.id.buttons_container).bringToFront();
                singupFormContainer.bringToFront();
                animationTV.bringToFront();

                animationFirstArist.setBackgroundColor(backgroundColor);
                animationSecondArist.setBackgroundColor(backgroundColor);
                animationCircle.setCardBackgroundColor(backgroundColor);
                animationSquare.setBackgroundColor(backgroundColor);
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        animationCircle.setVisibility(View.GONE);
                        animationFirstArist.setVisibility(View.GONE);
                        animationSecondArist.setVisibility(View.GONE);
                        animationTV.setVisibility(View.GONE);
                        animationSquare.setVisibility(View.GONE);
                    }
                }, 100);
                rootLayout.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.bg));
                ((View) animationSquare.getParent()).setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary));
                findViewById(R.id.login_form_container).setVisibility(View.GONE);
                showLoginButton();

            }
        });

        Animation a2 = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                animationSquare.getLayoutParams().height = (int) (square_target_height * interpolatedTime);
                animationSquare.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        ValueAnimator a3 = ValueAnimator.ofFloat(tv_curr_size, tv_target_size);
        a3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                animationTV.setTextSize(animatedValue);
            }
        });

        ValueAnimator a4 = ValueAnimator.ofInt(tv_curr_color, tv_target_color);
        a4.setEvaluator(new ArgbEvaluator());
        a4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedValue = (int) animation.getAnimatedValue();
                animationTV.setTextColor(animatedValue);
            }
        });

        a.setDuration(400);
        a2.setDuration(172);
        a3.setDuration(400);
        a4.setDuration(400);

        a4.start();
        a3.start();
        animationSquare.startAnimation(a2);
        animationCircle.startAnimation(a);
        // animationFirstArist.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_first_arist));
        //animationSecondArist.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_second_arist));
        singupFormContainer.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_form));
    }

    public void showLogIn(View view) {
        final CardView animationCircle = (CardView) findViewById(R.id.animation_circle);
        final View animationFirstArist = findViewById(R.id.animation_first_arist);
        final View animationSecondArist = findViewById(R.id.animation_second_arist);
        final View animationSquare = findViewById(R.id.animation_square);
        final LinearLayout squareParent = (LinearLayout) animationSquare.getParent();
        final TextView animationTV = (TextView) findViewById(R.id.animation_tv);
        final View singupFormContainer = findViewById(R.id.signup_form_container);
        final View loginFormContainer = findViewById(R.id.login_form_container);
        final int backgrounColor = ContextCompat.getColor(this, R.color.bg);


        final float scale = getResources().getDisplayMetrics().density;

        final int circle_curr_margin = rootLayout.getWidth() - (int) (view.getWidth() - view.getX() - animationCircle.getWidth());
        final int circle_target_margin = 0;

        final int first_curr_width = (int) (108 * scale + 0.5f);
        final int first_target_width = (rootLayout.getHeight() * 2);

        final int first_curr_height = (int) (70 * scale + 0.5f);
        final int first_target_height = rootLayout.getWidth();

        final int first_curr_margin = (int) (40 * scale + 0.5f);
        final int first_target_margin = (int) (35 * scale + 0.5f);
        final int first_expand_margin = (first_curr_margin - first_target_height);
        final int first_curr_margin_r = rootLayout.getWidth() - first_curr_width;


        final int square_target_width = rootLayout.getWidth();
        final int square_target_height = (int) (80 * scale + 0.5f);

        final float tv_curr_x = findViewById(R.id.login_small_tv).getX() + findViewById(R.id.login_button).getX();
        final float tv_curr_y = findViewById(R.id.login_small_tv).getY() + findViewById(R.id.buttons_container).getY() + findViewById(R.id.login_container).getY();


        final float tv_curr_size = 16;
        final float tv_target_size = 56;

        final int tv_curr_color = Color.parseColor("#ffffff");
        final int tv_target_color = Color.parseColor("#5cffffff");


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        squareParent.setGravity(Gravity.START);
        animationTV.setText("Login");

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                int diff_margin = circle_curr_margin - circle_target_margin;
                int margin = circle_target_margin + (int) (diff_margin - (diff_margin * interpolatedTime));

                RelativeLayout.LayoutParams params_circle = (RelativeLayout.LayoutParams) animationCircle.getLayoutParams();
                params_circle.setMargins(0, 0, margin, (int) (40 * scale + 0.5f));
                animationCircle.requestLayout();


                int diff_width = first_curr_width - first_target_width;
                int width = first_target_width + (int) (diff_width - (diff_width * interpolatedTime));

                int diff_height = first_curr_height - first_target_height;
                int height = first_target_height + (int) (diff_height - ((diff_height - first_target_margin) * interpolatedTime));

                diff_margin = first_curr_margin - first_expand_margin;
                margin = first_expand_margin + (int) (diff_margin - (diff_margin * interpolatedTime));
                int margin_r = first_curr_margin_r - (int) (first_curr_margin_r * interpolatedTime);
                int margin_l = rootLayout.getWidth() - width < 0 ? rootLayout.getWidth() - width : 0;

                RelativeLayout.LayoutParams params_first = (RelativeLayout.LayoutParams) animationFirstArist.getLayoutParams();
                params_first.setMargins(margin_l, 0, margin_r, margin);
                params_first.width = width;
                params_first.height = height;
                animationFirstArist.requestLayout();

                animationFirstArist.setPivotX(animationFirstArist.getWidth());
                animationFirstArist.setPivotY(0);
                animationFirstArist.setRotation(90 * interpolatedTime);

                margin = first_curr_margin + (int) (first_target_margin * interpolatedTime);
                RelativeLayout.LayoutParams params_second = (RelativeLayout.LayoutParams) animationSecondArist.getLayoutParams();
                params_second.setMargins(0, 0, margin_r, margin);
                params_second.width = width;
                animationSecondArist.requestLayout();

                animationSecondArist.setPivotX(animationSecondArist.getWidth());
                animationSecondArist.setPivotY(animationSecondArist.getHeight());
                animationSecondArist.setRotation(-(90 * interpolatedTime));

                animationSquare.getLayoutParams().width = (int) (square_target_width * interpolatedTime);
                animationSquare.requestLayout();

                animationTV.requestLayout();


                loginFormContainer.setAlpha(interpolatedTime);
                singupFormContainer.setAlpha(1 - interpolatedTime);
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation arg0) {
                animationFirstArist.setBackgroundColor(backgrounColor);
                animationSecondArist.setBackgroundColor(backgrounColor);
                animationCircle.setCardBackgroundColor(backgrounColor);
                animationSquare.setBackgroundColor(backgrounColor);

                animationFirstArist.setVisibility(View.VISIBLE);
                findViewById(R.id.login_container).setVisibility(View.INVISIBLE);
                animationSecondArist.setVisibility(View.VISIBLE);
                animationCircle.setVisibility(View.VISIBLE);
                animationSquare.setVisibility(View.VISIBLE);
                animationTV.setVisibility(View.VISIBLE);
                loginFormContainer.setVisibility(View.VISIBLE);

                animationFirstArist.bringToFront();
                squareParent.bringToFront();
                animationSecondArist.bringToFront();
                animationCircle.bringToFront();
                findViewById(R.id.buttons_container).bringToFront();
                loginFormContainer.bringToFront();

                animationTV.bringToFront();
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        animationCircle.setVisibility(View.GONE);
                        animationFirstArist.setVisibility(View.GONE);
                        animationSecondArist.setVisibility(View.GONE);
                        animationTV.setVisibility(View.GONE);
                        animationSquare.setVisibility(View.GONE);

                    }
                }, 100);
                rootLayout.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.bg));
                ((View) animationSquare.getParent()).setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary));
                findViewById(R.id.signup_form_container).setVisibility(View.GONE);
                showSingupButton();
            }
        });

        Animation a2 = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                animationSquare.getLayoutParams().height = (int) (square_target_height * interpolatedTime);
                animationSquare.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        ValueAnimator a3 = ValueAnimator.ofFloat(tv_curr_size, tv_target_size);
        a3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                animationTV.setTextSize(animatedValue);
            }
        });

        ValueAnimator a4 = ValueAnimator.ofInt(tv_curr_color, tv_target_color);
        a4.setEvaluator(new ArgbEvaluator());
        a4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedValue = (int) animation.getAnimatedValue();
                animationTV.setTextColor(animatedValue);
            }
        });

        a.setDuration(400);
        a2.setDuration(172);
        a3.setDuration(400);
        a4.setDuration(400);

        a4.start();
        a3.start();
        animationSquare.startAnimation(a2);
        animationCircle.startAnimation(a);
        loginFormContainer.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_form_reverse));
    }

    private void showLoginButton() {
        final CardView singupButton = (CardView) findViewById(R.id.singup_button);
        final View loginButton = findViewById(R.id.login_button);

        loginButton.setVisibility(View.VISIBLE);
        loginButton.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        findViewById(R.id.login_container).setVisibility(View.VISIBLE);

        final float scale = getResources().getDisplayMetrics().density;
        final int curr_singup_margin = (int) (-35 * scale + 0.5f);
        final int target_singup_margin = -singupButton.getWidth();

        final int curr_login_margin = -loginButton.getMeasuredWidth();
        final int target_login_margin = (int) (-35 * scale + 0.5f);

        EasingManager manager = new EasingManager(new EasingManager.EasingCallback() {

            @Override
            public void onEasingValueChanged(double value, double oldValue) {
                int diff_margin = curr_singup_margin - target_singup_margin;
                int margin = target_singup_margin + (int) (diff_margin - (diff_margin * value));

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) singupButton.getLayoutParams();
                layoutParams.setMargins(0, 0, margin, 0);
                singupButton.requestLayout();

                diff_margin = curr_login_margin - target_login_margin;
                margin = target_login_margin + (int) (diff_margin - (diff_margin * value));

                layoutParams = (LinearLayout.LayoutParams) loginButton.getLayoutParams();
                layoutParams.leftMargin = margin;
                loginButton.requestLayout();
            }

            @Override
            public void onEasingStarted(double value) {
                int diff_margin = curr_singup_margin - target_singup_margin;
                int margin = target_singup_margin + (int) (diff_margin - (diff_margin * value));

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) singupButton.getLayoutParams();
                layoutParams.setMargins(0, 0, margin, 0);
                singupButton.requestLayout();

                diff_margin = curr_login_margin - target_login_margin;
                margin = target_login_margin + (int) (diff_margin - (diff_margin * value));

                layoutParams = (LinearLayout.LayoutParams) loginButton.getLayoutParams();
                layoutParams.setMargins(margin, 0, 0, 0);
                loginButton.requestLayout();
            }

            @Override
            public void onEasingFinished(double value) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) singupButton.getLayoutParams();
                layoutParams.setMargins(0, 0, target_singup_margin, 0);
                singupButton.requestLayout();


                layoutParams = (LinearLayout.LayoutParams) loginButton.getLayoutParams();
                layoutParams.setMargins(target_login_margin, 0, 0, 0);
                loginButton.requestLayout();

                singupButton.setVisibility(View.GONE);
            }
        });

        manager.start(Back.class, EasingManager.EaseType.EaseOut, 0, 1, 600);
    }

    private void showSingupButton() {
        final CardView singupButton = (CardView) findViewById(R.id.singup_button);
        final View loginButton = findViewById(R.id.login_button);

        singupButton.setVisibility(View.VISIBLE);
        singupButton.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        findViewById(R.id.singup_container).setVisibility(View.VISIBLE);

        final float scale = getResources().getDisplayMetrics().density;
        final int curr_singup_margin = -singupButton.getWidth();
        final int target_singup_margin = (int) (-35 * scale + 0.5f);

        final int curr_login_margin = (int) (-35 * scale + 0.5f);
        final int target_login_margin = -loginButton.getMeasuredWidth();

        EasingManager manager = new EasingManager(new EasingManager.EasingCallback() {

            @Override
            public void onEasingValueChanged(double value, double oldValue) {
                int diff_margin = curr_singup_margin - target_singup_margin;
                int margin = target_singup_margin + (int) (diff_margin - (diff_margin * value));

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) singupButton.getLayoutParams();
                layoutParams.setMargins(0, 0, margin, 0);
                singupButton.requestLayout();

                diff_margin = curr_login_margin - target_login_margin;
                margin = target_login_margin + (int) (diff_margin - (diff_margin * value));

                layoutParams = (LinearLayout.LayoutParams) loginButton.getLayoutParams();
                layoutParams.leftMargin = margin;
                loginButton.requestLayout();
            }

            @Override
            public void onEasingStarted(double value) {
                int diff_margin = curr_singup_margin - target_singup_margin;
                int margin = target_singup_margin + (int) (diff_margin - (diff_margin * value));

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) singupButton.getLayoutParams();
                layoutParams.setMargins(0, 0, margin, 0);
                singupButton.requestLayout();

                diff_margin = curr_login_margin - target_login_margin;
                margin = target_login_margin + (int) (diff_margin - (diff_margin * value));

                layoutParams = (LinearLayout.LayoutParams) loginButton.getLayoutParams();
                layoutParams.setMargins(margin, 0, 0, 0);
                loginButton.requestLayout();
            }

            @Override
            public void onEasingFinished(double value) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) singupButton.getLayoutParams();
                layoutParams.setMargins(0, 0, target_singup_margin, 0);
                singupButton.requestLayout();


                layoutParams = (LinearLayout.LayoutParams) loginButton.getLayoutParams();
                layoutParams.setMargins(target_login_margin, 0, 0, 0);
                loginButton.requestLayout();
                loginButton.setVisibility(View.GONE);
            }
        });

        manager.start(Back.class, EasingManager.EaseType.EaseOut, 0, 1, 600);
    }


}

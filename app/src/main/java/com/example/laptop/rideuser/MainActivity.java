package com.example.laptop.rideuser;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import Common.Common;
import com.example.laptop.rideuser.Model.Rider;
import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    Button Signin, Register;
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;
    RelativeLayout mainLayout;
    private final static int PERMISSION = 1000;

    @Override
    protected void attachBaseContext(Context newBase) {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        //Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference(Common.user_rider_tbl);

        Signin = (Button)findViewById(R.id.signinbtn);
        Register =(Button)findViewById(R.id.regbtn  );
        mainLayout = (RelativeLayout)findViewById(R.id.mainLayout);

        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRegisterDialog();
            }
        });

        Signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoginDialog();
            }
        });
    }

    private void showRegisterDialog() {


        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("REGISTER");
        dialog.setMessage("Please use Email to register");
        LayoutInflater inflater = LayoutInflater.from(this);
        View register_layout = inflater.inflate(R.layout.register_layout, null);
        final MaterialEditText editemail = register_layout.findViewById(R.id.editemail);
        final MaterialEditText editname = register_layout.findViewById(R.id.editName);
        final MaterialEditText editpass = register_layout.findViewById(R.id.editpassword);
        final MaterialEditText editphone = register_layout.findViewById(R.id.editPhone);
        dialog.setView(register_layout);
        //Set Button for registration

        dialog.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


                dialogInterface.dismiss();
                //Check validation
                if (TextUtils.isEmpty(editemail.getText().toString())) {

                    Snackbar.make(mainLayout, "Please Enter Your Email", Snackbar.LENGTH_SHORT)
                            .show();
                    return;

                    //Register new User

                }

                if (TextUtils.isEmpty(editname.getText().toString())) {

                    Snackbar.make(mainLayout, "Please Enter Your Name", Snackbar.LENGTH_SHORT)
                            .show();
                    return;


                }
                if (TextUtils.isEmpty(editphone.getText().toString())) {

                    Snackbar.make(mainLayout, "Please Enter Your Phone Number", Snackbar.LENGTH_SHORT)
                            .show();
                    return;


                }
                if (editpass.getText().toString().length() < 6) {

                    Snackbar.make(mainLayout, "Password Too Short", Snackbar.LENGTH_SHORT)
                            .show();
                    return;


                }


                auth.createUserWithEmailAndPassword(editemail.getText().toString(), editpass.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                //Save user to database

                                Rider rider = new Rider();
                                rider.setEmail(editemail.getText().toString());
                                rider.setPhone(editphone.getText().toString());
                                rider.setName(editname.getText().toString());
                                rider.setPassword(editpass.getText().toString());

                                //Use UId for Key
                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(rider)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                Snackbar.make(mainLayout, "Registerd Succesfully!!", Snackbar.LENGTH_SHORT)
                                                        .show();

                                            }

                                        })

                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                Snackbar.make(mainLayout, "Failed To Register!!", Snackbar.LENGTH_SHORT)
                                                        .show();
                                            }
                                        });

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(mainLayout, "Registerd Succesfully!!", Snackbar.LENGTH_SHORT)
                                        .show();
                            }
                        });
            }


        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.show();
    }

    private void showLoginDialog() {


        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("SIGN IN");
        dialog.setMessage("Please use Email to signin");
        LayoutInflater inflater = LayoutInflater.from(this);
        View login_yout = inflater.inflate(R.layout.layout_signin, null);
        final MaterialEditText editemail = login_yout.findViewById(R.id.editemail);
        final MaterialEditText editname = login_yout.findViewById(R.id.editName);
        final MaterialEditText editpass = login_yout.findViewById(R.id.editpassword);
        final MaterialEditText editphone = login_yout.findViewById(R.id.editPhone);
        dialog.setView(login_yout);
        //Set Button for SignIn

        dialog.setPositiveButton("SIGN IN", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


                dialogInterface.dismiss();


                //set disable button
                Signin.setEnabled(false);
                //Check validation


                if (TextUtils.isEmpty(editemail.getText().toString())) {

                    Snackbar.make(mainLayout, "Please Enter Your email", Snackbar.LENGTH_SHORT)
                            .show();
                    return;



                }
                if (editpass.getText().toString().length() < 6) {

                    Snackbar.make(mainLayout, "Password Too Short", Snackbar.LENGTH_SHORT)
                            .show();
                    return;

                }


                final SpotsDialog waitingdialog = new SpotsDialog(MainActivity.this);
                waitingdialog.show();


                //Login
                auth.signInWithEmailAndPassword(editemail.getText().toString(),editpass.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                waitingdialog.dismiss();
                                startActivity(new Intent(MainActivity.this,Home.class));
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        waitingdialog.dismiss();
                        Snackbar.make(mainLayout,"Failed to Login"+e.getMessage(),Snackbar.LENGTH_SHORT)
                                .show();

                        //Active button

                        Signin.setEnabled(true);
                    }
                });

            }
        });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });





        dialog.show();

    }



    }







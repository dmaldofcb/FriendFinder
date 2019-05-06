package com.e.friendfinder.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
//import com.google.firebase.auth.FirebaseUser;

import com.e.friendfinder.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "Check";

    private EditText userName;
    private EditText email;
    private EditText pass1;
    private EditText pass2;
    private String lat = "latitude";
    private String longi = "longitude";
    private String time = "timestamp";
    private Button login;

    private Button register;
    private ProgressBar loadingBar;
    private FirebaseAuth mAuth;

    private FirebaseDatabase mfirebaseDatabase;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Calling Create RegisterActivity");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userName = (EditText) findViewById(R.id.username);
        email = (EditText) findViewById(R.id.email);
        pass1 = (EditText) findViewById(R.id.passwordRegister);
        pass2 = (EditText) findViewById(R.id.passwordRegister2);
        register = (Button) findViewById(R.id.createReg);
        loadingBar = (ProgressBar) findViewById(R.id.progressBar);
        login = (Button) findViewById(R.id.loginReg);



        mAuth = FirebaseAuth.getInstance();
        mfirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mfirebaseDatabase.getReference().child("Users");
        if(mAuth.getCurrentUser() != null){
            Log.d(TAG, "Calling Logged In RegisterActivity");

            startActivity(new Intent(RegisterActivity.this,FriendMap.class));
        }

        loadingBar.setVisibility(View.INVISIBLE);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register.setVisibility(View.INVISIBLE);
                login.setVisibility(View.INVISIBLE);
                loadingBar.setVisibility(View.VISIBLE);
                final String username = userName.getText().toString();
                final String password = pass1.getText().toString();
                final String password2 = pass2.getText().toString();
                final String mail = email.getText().toString();

                if(username.isEmpty() || mail.isEmpty() || password.isEmpty() || password2.isEmpty()){
                    //did not fill out all data
                    Toast.makeText(getApplicationContext(),"Please verify fields are filled",Toast.LENGTH_LONG).show();
                    register.setVisibility(View.VISIBLE);
                    login.setVisibility(View.VISIBLE);
                    loadingBar.setVisibility(View.INVISIBLE);

                } else if (!password.equals(password2)) {
                    Toast.makeText(getApplicationContext(),"Passwords do not match",Toast.LENGTH_LONG).show();
                    register.setVisibility(View.VISIBLE);
                    login.setVisibility(View.VISIBLE);
                    loadingBar.setVisibility(View.INVISIBLE);
                } else {
                    //create user account
                    createUserAccount(username, mail,password);
                }

            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent log = new Intent(RegisterActivity.this, LoginAccount.class);
                startActivity(log);
            }
        });
    }


    //handles user account creation for username and password
    private void createUserAccount(final String username, String mail, String password) {

        Log.d(TAG, "Calling Create User RegisterActivity");

        mAuth.createUserWithEmailAndPassword(mail,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    String userID = mAuth.getUid();
                    myRef = mfirebaseDatabase.getReference().child("Users").child(userID);
                    myRef.child("username").setValue(username);
                    myRef.child(lat).setValue(0.0);
                    myRef.child(longi).setValue(0.0);
                    myRef.child(time).setValue("0.0");
                    Toast.makeText(getApplicationContext(),"Account Created!",Toast.LENGTH_LONG).show();

                    Intent map = new Intent(RegisterActivity.this, FriendMap.class);
                    startActivity(map);
                    register.setVisibility(View.VISIBLE);
                    login.setVisibility(View.VISIBLE);
                    loadingBar.setVisibility(View.INVISIBLE);

                } else {
                    //account creation failed
                    Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_LONG).show();
                    register.setVisibility(View.VISIBLE);
                    login.setVisibility(View.VISIBLE);
                    loadingBar.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

}



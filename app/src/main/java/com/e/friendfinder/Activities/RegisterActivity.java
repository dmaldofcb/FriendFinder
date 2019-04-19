package com.e.friendfinder.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class RegisterActivity extends AppCompatActivity {

    private EditText userName;
    private EditText email;
    private EditText pass1;
    private EditText pass2;
    private Button login;

    private Button register;
    private ProgressBar loadingBar;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userName = (EditText) findViewById(R.id.username);
        email = (EditText) findViewById(R.id.email);
        pass1 = (EditText) findViewById(R.id.passwordRegister);
        pass2 = (EditText) findViewById(R.id.passwordRegister2);
        register = (Button) findViewById(R.id.createReg);
        loadingBar = (ProgressBar) findViewById(R.id.progressBar);
        login = (Button) findViewById(R.id.loginReg);
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        updateUI(currentUser);

        mAuth = FirebaseAuth.getInstance();



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

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            startActivity(new Intent(RegisterActivity.this,FriendMap.class));
        }
    }


    //handles user account creation for username and password
    private void createUserAccount(final String username, String mail, String password) {
        mAuth.createUserWithEmailAndPassword(mail,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
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



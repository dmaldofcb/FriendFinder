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

import com.e.friendfinder.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginAccount extends AppCompatActivity {
    private static final String TAG = "Check";

    private EditText email;
    private EditText password;
    private Button login;
    private ProgressBar loginBar;
    private FirebaseAuth mAuth;
    private Button reg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_account);
        Log.d(TAG, "Calling onCreate LoginActivity");

        email = (EditText) findViewById(R.id.emailLogin);
        password = (EditText) findViewById(R.id.passwordLogin);
        login = (Button) findViewById(R.id.loginAccount);
        reg = (Button) findViewById(R.id.regButton);
        loginBar = (ProgressBar) findViewById(R.id.progressBar3);
        loginBar.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser() != null){
            Log.d(TAG, "Calling Logged In RegisterActivity");

            startActivity(new Intent(LoginAccount.this,FriendMap.class));
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail = email.getText().toString();
                String passw = password.getText().toString();
                if (mail.isEmpty() || passw.isEmpty()){
                    Toast.makeText(getApplicationContext(),"Please verify fields are filled",Toast.LENGTH_LONG).show();

                } else {
                    loginBar.setVisibility(View.VISIBLE);
                    signIn(mail,passw);
                }
            }
        });

        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginAccount.this,RegisterActivity.class));
            }
        });
    }

    private void signIn(String mail, String passw) {
        mAuth.signInWithEmailAndPassword(mail, passw)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(),"Sign In Succesfully!",Toast.LENGTH_LONG).show();
                            Intent map = new Intent(LoginAccount.this, FriendMap.class);
                            startActivity(map);
                            loginBar.setVisibility(View.INVISIBLE);

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_LONG).show();
                            loginBar.setVisibility(View.INVISIBLE);

                        }
                    }
                });
    }


}

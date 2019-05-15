package com.example.farooq.firebaseblogapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private EditText mUser;
    private EditText mPass;
    private Button mLogin;
    private Button mCreate;
    private ProgressBar mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mUser = (EditText) findViewById(R.id.login_email);
        mPass = (EditText) findViewById(R.id.login_pass);
        mLogin = (Button) findViewById(R.id.login_log_btn);
        mCreate = (Button) findViewById(R.id.login_create_btn);
        mProgress = (ProgressBar) findViewById(R.id.login_progress_bar);

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: button click");
                String em=mUser.getText().toString();
                String pass = mPass.getText().toString();
                if(!em.equals("")&&!pass.equals("")){
                    mProgress.setVisibility(View.VISIBLE);
                    Log.d(TAG, "onClick: match ");
                    login(em,pass);
                }else {
                    Toast.makeText(LoginActivity.this, "Please enter ", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
    public void login(String email,String password)
    {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Sucessfuld", Toast.LENGTH_SHORT).show();
                            mProgress.setVisibility(View.INVISIBLE);
                            FirebaseUser user = mAuth.getCurrentUser();
                            sendToMain();
                        } else {
                            String errorMessage= task.getException().getMessage();
                            Toast.makeText(LoginActivity.this, "Error : "+errorMessage, Toast.LENGTH_SHORT).show();
                        }
                        mProgress.setVisibility(View.INVISIBLE);
                    }
                });
    }
    private void sendToMain(){
        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}

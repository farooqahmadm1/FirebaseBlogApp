package com.example.farooq.firebaseblogapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.PublicKey;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private EditText mUser;
    private EditText mPass;
    private EditText mPassConfirm;
    private Button mReg;
    private Button mRegLogin;
    private ProgressBar mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mUser = (EditText) findViewById(R.id.reg_email);
        mPass = (EditText) findViewById(R.id.reg_pass);
        mPassConfirm = (EditText) findViewById(R.id.reg_pass_confirm);
        mReg = (Button) findViewById(R.id.reg_btn);
        mRegLogin = (Button) findViewById(R.id.reg_login_btn);
        mProgress = (ProgressBar) findViewById(R.id.reg_progress_bar);

        mReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String em=mUser.getText().toString();
                String pass = mPass.getText().toString();
                String confirmPass = mPassConfirm.getText().toString();
                if(!em.equals("")&&!pass.equals("")&&!confirmPass.equals("")){
                    if(pass.equals(confirmPass)){
                        mProgress.setVisibility(View.VISIBLE);
                        Create(em,pass);
                    }else {
                        Toast.makeText(RegisterActivity.this, "Match the password", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(RegisterActivity.this, "Please enter ", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mRegLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
    private void Create(String email,String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            sendToRegister();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                        mProgress.setVisibility(View.INVISIBLE);
                    }
                });
    }

    private void sendToRegister() {
        Intent intent= new Intent(RegisterActivity.this,AccountActivity.class);
        startActivity(intent);
        finish();
    }

    private void sendToMain() {
        Intent intent= new Intent(RegisterActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user!=null){
            sendToMain();
        }
    }
}

package com.example.farooq.firebaseblogapp;

import android.content.Intent;
import android.os.Binder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.farooq.firebaseblogapp.Fragments.AccountFragment;
import com.example.farooq.firebaseblogapp.Fragments.HomeFragment;
import com.example.farooq.firebaseblogapp.Fragments.NotificationFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private BottomNavigationView bottomNavigationView;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Photo Blog");

        replaceFragment(HomeFragment.getInstance(getApplicationContext()));
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.main_bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.bottom_navigation_home:
                        replaceFragment(HomeFragment.getInstance(getApplicationContext()));
                        return true;
                    case R.id.bottom_navigation_account:
                        replaceFragment(AccountFragment.getInstance());
                        return true;
                    case R.id.bottom_navigation_notification:
                        replaceFragment(NotificationFragment.getInstance());
                        return true;
                    default:
                        return false;
                }
            }
        });

        //firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        firestore = FirebaseFirestore.getInstance();
        if (currentUser!=null){
            firestore.collection("Users").document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        if (!task.getResult().exists()){
                            Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }else {
                        Toast.makeText(MainActivity.this, "Error :"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }
    @Override
    protected void onStart() {
        super.onStart();

        if(FirebaseAuth.getInstance().getCurrentUser()==null){
            sendToActivity();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add_button:
                Intent add_post =new Intent(MainActivity.this,AddNewPostActivity.class);
                startActivity(add_post);
                return true;
            case R.id.action_logout_button:
                logout();
                return true;
            case R.id.action_setting_button:
                Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_search_button:
                return true;
            default:
                return false;
        }
    }
    private void logout() {
        mAuth.signOut();
        sendToActivity();
    }
    private void sendToActivity() {
        Intent intent= new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void replaceFragment(Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_container,fragment);
        transaction.commit();
    }

}
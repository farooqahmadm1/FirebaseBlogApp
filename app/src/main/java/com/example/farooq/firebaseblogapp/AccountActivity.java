package com.example.farooq.firebaseblogapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;

public class AccountActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "AccountActivity";
    private static final int REQUEST_GET_SINGLE_FILE =1000;
    private static final int REQUEST_GET_PERMISSION =1001;
    private Toolbar toolbar;
    private CircleImageView mCircleImage;
    private EditText mNameText;
    private Button mSaveBtn;
    private ProgressBar aProgressBar;
    private boolean isProfileChanged=false;

    private Uri mAccountImageUri=null;
    //FireBase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    String user_id;

    @Override
    protected void onStart() {
        super.onStart();
        if(user_id == null){
            sendToLogin();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        toolbar = (Toolbar) findViewById(R.id.account_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account Setup");

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        mCircleImage = (CircleImageView) findViewById(R.id.account_profile_image);
        mNameText = (EditText) findViewById(R.id.account_name);
        mSaveBtn = (Button) findViewById(R.id.account_save_btn);
        aProgressBar = (ProgressBar) findViewById(R.id.account_progress_bar);

        aProgressBar.setVisibility(View.VISIBLE);
        mSaveBtn.setEnabled(false);
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().exists()){
                        String name = task.getResult().getString("name");
                        String url = task.getResult().getString("image");
                        mAccountImageUri = Uri.parse(url);
                        mNameText.setText(name);
                        Picasso.get().load(url).placeholder(R.drawable.profile).into(mCircleImage);
                    }
                }else {
                    String error= task.getException().getMessage();
                    Toast.makeText(AccountActivity.this, "Error :" + error, Toast.LENGTH_SHORT).show();
                }
                aProgressBar.setVisibility(View.INVISIBLE);
                mSaveBtn.setEnabled(true);
            }
        });
        mCircleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (ContextCompat.checkSelfPermission(AccountActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(AccountActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_GET_PERMISSION);
                    }else {
                        getImage();
                    }
                }
                else {
                    getImage();
                }
            }
        });
        mSaveBtn.setOnClickListener(this);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ContextCompat.checkSelfPermission(AccountActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "Permission Allow", Toast.LENGTH_SHORT).show();
            getImage();
        }
    }
    private void getImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),REQUEST_GET_SINGLE_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try{
            if(requestCode==REQUEST_GET_SINGLE_FILE && resultCode==RESULT_OK){
                mAccountImageUri = data.getData();
                mCircleImage.setImageURI(mAccountImageUri);
                isProfileChanged = true;
            }
        }catch (Exception e){
            Toast.makeText(this, "Here is some error in picking image", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.account_save_btn){
                final String name = mNameText.getText().toString();
                if (!TextUtils.isEmpty(name) && mAccountImageUri!=null){
                    aProgressBar.setVisibility(View.VISIBLE);
                    if (isProfileChanged==true) {
                        final StorageReference path = storageReference.child("profile_images").child(user_id+".jpeg");
                        path.putFile(mAccountImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                // Continue with the task to get the download URL
                                return path.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()){
                                    Uri download_uri= task.getResult();
                                    Log.d(TAG, "onComplete: " + download_uri.toString());
                                    Map<String, String> map = new HashMap<String, String>();
                                    map.put("name",mNameText.getText().toString());
                                    map.put("image",download_uri.toString());
                                    storeFirestore(map);
                                }else {
                                    String error= task.getException().getMessage().toString();
                                    Toast.makeText(AccountActivity.this, "Error Image :" +error, Toast.LENGTH_SHORT).show();
                                    aProgressBar.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }else {
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("name",mNameText.getText().toString());
                        map.put("image",mAccountImageUri.toString());
                        storeFirestore(map);
                    }
                }
        }
    }
    private void storeFirestore(Map<String,String> map){
        firebaseFirestore.collection("Users").document(user_id).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(AccountActivity.this, "The User Setting Are Updated", Toast.LENGTH_SHORT).show();
                    aProgressBar.setVisibility(View.INVISIBLE);
                    sendToMain();
                }else {
                    aProgressBar.setVisibility(View.INVISIBLE);
                    String error = task.getException().getMessage();
                    Toast.makeText(AccountActivity.this, "Firestore error :"+error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendToLogin() {
        Intent intent = new Intent(AccountActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }
    private void sendToMain(){
        Intent intent = new Intent(AccountActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}

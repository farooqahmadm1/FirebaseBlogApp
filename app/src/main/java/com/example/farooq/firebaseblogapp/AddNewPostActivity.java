package com.example.farooq.firebaseblogapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class AddNewPostActivity extends AppCompatActivity {

    public static final int IMAGE_REQUEST_CODE = 1000;
    private static final String TAG = "AddNewPostActivity";

    private Toolbar toolbar;
    private ImageView mPostImage;
    private EditText mPostDescription;
    private Button mPostButton;
    private ProgressBar mProgressBar;

    private FirebaseUser currentUser;
    private StorageReference storage;
    private FirebaseFirestore firestore;
    private String mUserId;

    private Uri mAccountImageUri=null;
    private Uri mAccountImageThumbUri;
    private Bitmap compressedImageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_post);

        toolbar = (Toolbar) findViewById(R.id.add_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mUserId = currentUser.getUid();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance().getReference();

        mProgressBar = (ProgressBar) findViewById(R.id.add_progress_bar);
        mPostImage = (ImageView) findViewById(R.id.add_image);
        mPostDescription = (EditText) findViewById(R.id.add_description);
        mPostButton = (Button) findViewById(R.id.add_button);

        mPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImage();
            }
        });
        mPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postSave();
            }
        });
    }

    private void getImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),IMAGE_REQUEST_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try{
            if(requestCode==IMAGE_REQUEST_CODE && resultCode==RESULT_OK){
                mAccountImageUri = data.getData();
                mPostImage.setImageURI(mAccountImageUri);
            }
        }catch (Exception e){
            Toast.makeText(this, "Here is some error in picking image", Toast.LENGTH_SHORT).show();
        }
    }
    private void postSave() {
        final String description = mPostDescription.getText().toString();
        if (!TextUtils.isEmpty(description) && mAccountImageUri!=null) {
            mProgressBar.setVisibility(View.VISIBLE);
            final String random = UUID.randomUUID().toString();
            Log.d(TAG, "onComplete: postsave start ");
            final StorageReference path = storage.child("post_images").child(random+ ".jpg");
            path.putFile(mAccountImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    Log.d(TAG, "onComplete: first image ");
                    if (!task.isSuccessful()) { throw task.getException(); }
                    return path.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        final Uri download_uri = task.getResult();
                        Log.d(TAG, "onComplete: image" + download_uri.toString());
                        try {
                            compressedImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mAccountImageUri);
                            Log.d(TAG, "onComplete: succesful");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
                        byte[] data = baos.toByteArray();
                        final StorageReference path_thumb = storage.child("post_images/thumbs").child(random+ ".jpg");
                        final UploadTask uploadTask = path_thumb.putBytes(data);
                        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                Log.d(TAG, "onComplete: second image ");
                                if (!task.isSuccessful()) { throw task.getException(); }
                                return path_thumb.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()){
                                    Uri download_thumb_uri = task.getResult();
                                    Log.d(TAG, "onComplete: "+download_thumb_uri.toString());
                                    Map<String, String> map = new HashMap<String, String>();
                                    map.put("description", description);
                                    map.put("image", download_uri.toString());
                                    map.put("thumb_image",download_thumb_uri.toString());
                                    map.put("user_id",mUserId);
                                    map.put("timestamp", new SimpleDateFormat("dd/MM/yyyy").format(new Date())+"");
                                    storeFirestore(map);
                                }
                                else {
                                    String error = task.getException().getMessage();
                                    Toast.makeText(AddNewPostActivity.this, "Error Image :" + error, Toast.LENGTH_SHORT).show();
                                    mProgressBar.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    } else {
                        String error = task.getException().getMessage().toString();
                        Toast.makeText(AddNewPostActivity.this, "Error Image :" + error, Toast.LENGTH_SHORT).show();
                        mProgressBar.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }
    }
    private void storeFirestore(Map<String,String> map){
        firestore.collection("Posts").add(map).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()){
                    Toast.makeText(AddNewPostActivity.this, "Post is Done", Toast.LENGTH_SHORT).show();
                    mProgressBar.setVisibility(View.INVISIBLE);
                    sendToMain();
                }else {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    String error = task.getException().getMessage();
                    Toast.makeText(AddNewPostActivity.this, "Firestore error :"+error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void sendToMain(){
        Intent intent = new Intent(AddNewPostActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}

package com.example.plus.socialnetwork;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ProgressDialog loadingBar;

    private ImageButton selectPostImage;
    private EditText postDescription;
    private Button updatePostButton;

    private static final int Gallery_Pick=1;
    private Uri imageUri;

    private String description;

    private StorageReference postsImagesRefrence;

    private String saveCurrentDate, saveCurrentTime, postRandomName,downloadUrl,currentUserID;

    private DatabaseReference usersRef,postsRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);



        loadingBar=new ProgressDialog(this);


        postsImagesRefrence= FirebaseStorage.getInstance().getReference();


        selectPostImage=findViewById(R.id.select_post_image);
        postDescription=findViewById(R.id.post_description);
        updatePostButton=findViewById(R.id.update_post_button);


        usersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        postsRef= FirebaseDatabase.getInstance().getReference().child("Posts");
        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();




        mToolbar=findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Update Post");



        selectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenGallery();
            }
        });

        updatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValidatePostInfo();
            }
        });
    }

    private void ValidatePostInfo()
    {

        description=postDescription.getText().toString();

        if(imageUri==null)
        {
            Toast.makeText(this, "Please select an image...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(description))
        {
            Toast.makeText(this, "Please enter an comment about your image", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Add New Post");
            loadingBar.setMessage("Please wait,while we are updating your new post...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            StoringImageToStorage();
        }

    }

    private void StoringImageToStorage()
    {


        Calendar calForDate=Calendar.getInstance();
        SimpleDateFormat currentDate=new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate=currentDate.format(calForDate.getTime());



        Calendar calForTime=Calendar.getInstance();
        SimpleDateFormat currentTime=new SimpleDateFormat("HH:mm");
        saveCurrentTime=currentTime.format(calForTime.getTime());



        postRandomName=saveCurrentDate+saveCurrentTime;


        StorageReference filePath=postsImagesRefrence.child("Post Images").child(imageUri.getLastPathSegment()+postRandomName+".jpg");

        filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if(task.isSuccessful())
                {
                    downloadUrl=task.getResult().getDownloadUrl().toString();
                    Toast.makeText(PostActivity.this, "Image uploaded successfully...", Toast.LENGTH_SHORT).show();

                    SavingPostInformationToDatabase();
                }
                else
                {
                    String message=task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "Error occured : "+message, Toast.LENGTH_SHORT).show();
                }


            }
        });


    }

    private void SavingPostInformationToDatabase() //postu paylaşan kişinin ıd sini kullanarak ismini çektik..
    {

        usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {
                    String userFullname=dataSnapshot.child("fullname").getValue().toString();
                    String userProfileImage=dataSnapshot.child("profileimage").getValue().toString();

                    HashMap postsMap=new HashMap();
                    postsMap.put("uid",currentUserID);
                    postsMap.put("date",saveCurrentDate);
                    postsMap.put("time",saveCurrentTime);
                    postsMap.put("description",description);
                    postsMap.put("postimage",downloadUrl);
                    postsMap.put("profileimage",userProfileImage);
                    postsMap.put("fullname",userFullname);

                    postsRef.child(currentUserID+postRandomName).updateChildren(postsMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {

                            if(task.isSuccessful())
                            {
                                SendUserToMainActivity();
                                Toast.makeText(PostActivity.this, "Post is updated successfully...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else
                            {
                                String message=task.getException().getMessage();
                                Toast.makeText(PostActivity.this, "Error occured : "+message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void OpenGallery()
    {
        Intent galleryIntent=new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null)
        {
            imageUri=data.getData();
            selectPostImage.setImageURI(imageUri);


        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id=item.getItemId();
        if(id==android.R.id.home)
        {
            SendUserToMainActivity();
        }


        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent=new Intent(PostActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}

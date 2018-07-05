package com.example.plus.socialnetwork;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;


public class SettingsActivity extends AppCompatActivity {


    private EditText userName,userProfName, userCountry,userStatus,userGender,userRelations,userDOB;
    private Button updateAccountSettingsButton;
    private CircleImageView userProfImage;

    private Toolbar mToolbar;


    private DatabaseReference settingsUserRef,settingsPostsRef,refimRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    final static int Gallery_Pick=1;
    private ProgressDialog loadingBar;
    private StorageReference userProfileImageRef;

    private String downloadUrl;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        loadingBar = new ProgressDialog(this);
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");


        mAuth=FirebaseAuth.getInstance();

        currentUserID=mAuth.getCurrentUser().getUid();

        settingsUserRef= FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        settingsPostsRef= FirebaseDatabase.getInstance().getReference().child("Posts");






        mToolbar=findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //geri ok için...


        userName=findViewById(R.id.settings_username);
        userProfName=findViewById(R.id.setting_profile_fullname);
        userCountry=findViewById(R.id.settings_country);
        userStatus=findViewById(R.id.settings_status);
        userGender=findViewById(R.id.settings_gender);
        userRelations=findViewById(R.id.settings_relationship_status);
        userDOB=findViewById(R.id.settings_dob);


        userProfImage=findViewById(R.id.settings_profile_image);

        updateAccountSettingsButton=findViewById(R.id.update_account_settings_button);




        settingsUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {
                    String myProfileImage=dataSnapshot.child("profileimage").getValue().toString();
                    String myUsername=dataSnapshot.child("username").getValue().toString();
                    String myProfileName=dataSnapshot.child("fullname").getValue().toString();
                    String myProfileStatus=dataSnapshot.child("status").getValue().toString();
                    String myDOB=dataSnapshot.child("dob").getValue().toString();
                    String myCountry=dataSnapshot.child("country").getValue().toString();
                    String myGender=dataSnapshot.child("gender").getValue().toString();
                    String myRelationStatus=dataSnapshot.child("relationship").getValue().toString();


                    Picasso.with(SettingsActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(userProfImage);
                    userName.setText(myUsername);
                    userProfName.setText(myProfileName);
                    userStatus.setText(myProfileStatus);
                    userCountry.setText(myCountry);
                    userGender.setText(myGender);
                    userDOB.setText(myDOB);
                    userRelations.setText(myRelationStatus);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        updateAccountSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                validateAccountInfo();


            }
        });


        userProfImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });



    }


    private void postGuncelle()
    {
        //////////////Çıkmadan öncee
        refimRef= FirebaseDatabase.getInstance().getReference().child("Posts");

        refimRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                System.out.println("1EEEEEEEEEEEEEEEEEEEEEEEEEEEEEE"+dataSnapshot.child("uid").getValue().toString());
                //refimRef.child(dataSnapshot.getKey().toString())

                if(currentUserID.equals(dataSnapshot.child("uid").getValue().toString()))
                {
                    refimRef.child(dataSnapshot.getKey().toString()).child("fullname").setValue(userProfName.getText().toString());
                    refimRef.child(dataSnapshot.getKey().toString()).child("profileimage").setValue(downloadUrl);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {




            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null)
        {
            Uri imageUri=data.getData();

            CropImage.activity() //resmi kırpma kütüphanesi ekledik. kırpma işlemi..
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);


        }
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result=CropImage.getActivityResult(data);

            if(resultCode==RESULT_OK)
            {
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait,while we are updating your profile image...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();



                final Uri resultUri=result.getUri();

                StorageReference filePath=userProfileImageRef.child(currentUserID+".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful())
                        {


                             downloadUrl =task.getResult().getDownloadUrl().toString();




                            settingsUserRef.child("profileimage").setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful())
                                            {
                                                Picasso.with(SettingsActivity.this).load(downloadUrl).placeholder(R.drawable.profile).into(userProfImage);
                                                Toast.makeText(SettingsActivity.this, "Profile image changed successfully", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                            else
                                            {
                                                String message=task.getException().getMessage();
                                                Toast.makeText(SettingsActivity.this, "Error occured : "+message, Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }

                                        }
                                    });
                        }

                    }
                });
            }
            else
            {
                Toast.makeText(this, "Error occured : Image can not be cropped. Try Again.", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }

    }

    private void validateAccountInfo()
    {
        String username=userName.getText().toString();
        String profilename=userProfName.getText().toString();
        String status=userStatus.getText().toString();
        String gender=userGender.getText().toString();
        String dob=userDOB.getText().toString();
        String country=userCountry.getText().toString();
        String relation=userRelations.getText().toString();


        if(TextUtils.isEmpty(username))
        {
            Toast.makeText(this, "Enter your username", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(profilename))
        {
            Toast.makeText(this, "Enter your Full Name", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(status))
        {
            Toast.makeText(this, "Enter your status", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(gender))
        {
            Toast.makeText(this, "Enter your gender", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(dob))
        {
            Toast.makeText(this, "Enter your Date of Birth", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(country))
        {
            Toast.makeText(this, "country", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(relation))
        {
            Toast.makeText(this, "Enter your relation", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Profile Image");
            loadingBar.setMessage("Please wait,while we are updating your profile...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            updateAccountInfo(username,profilename,status,gender,dob,country,relation);
            postGuncelle();
        }



    }

    private void updateAccountInfo(String username, String profilename, String status, String gender, String dob, String country, String relation)
    {


        HashMap userMap=new HashMap();
        userMap.put("username",username);
        userMap.put("fullname",profilename);
        userMap.put("status",status);
        userMap.put("gender",gender);
        userMap.put("dob",dob);
        userMap.put("country",country);
        userMap.put("relationship",relation);

        settingsUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {

                if(task.isSuccessful())
                {
                    sendUserToMainActivity();
                    Toast.makeText(SettingsActivity.this, "Account settings updated successfully.", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
                else
                {
                    Toast.makeText(SettingsActivity.this, "Error occured : while updating settings", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }


            }
        });




    }


    private void sendUserToMainActivity()
    {
        Intent mainIntent=new Intent(SettingsActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}

package com.example.plus.socialnetwork;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {


    private TextView userName,userProfName, userCountry,userStatus,userGender,userRelations,userDOB;
    private CircleImageView userProfileImage;


    private DatabaseReference profileUserRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);



        userName=findViewById(R.id.my_username);
        userProfName=findViewById(R.id.my_profile_full_name);
        userCountry=findViewById(R.id.my_country);
        userStatus=findViewById(R.id.my_profile_status);
        userGender=findViewById(R.id.my_gender);
        userRelations=findViewById(R.id.my_relationship_status);
        userDOB=findViewById(R.id.my_dob);
        userProfileImage=findViewById(R.id.my_profile_pic);


        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        profileUserRef= FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);


        profileUserRef.addValueEventListener(new ValueEventListener() {
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


                    Picasso.with(ProfileActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);

                    userName.setText("@"+myUsername);
                    userProfName.setText(myProfileName);
                    userStatus.setText(myProfileStatus);
                    userCountry.setText("Country: "+myCountry);
                    userGender.setText("Gender: "+myGender);
                    userDOB.setText("Date of Birth:"+myDOB);
                    userRelations.setText("Relationship: "+myRelationStatus);

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}

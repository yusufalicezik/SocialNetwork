package com.example.plus.socialnetwork;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {

    private TextView userName,userProfName, userCountry,userStatus,userGender,userRelations,userDOB;
    private CircleImageView userProfileImage;
    Button sendFriendRequestButton, declineFriendRequestButton;


    private DatabaseReference profileUserRef,usersRef;
    private FirebaseAuth mAuth;
    private String senderUserID, receiverUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);


        mAuth=FirebaseAuth.getInstance();
        receiverUserID=getIntent().getExtras().getString("visit_user_id").toString();
        senderUserID=mAuth.getCurrentUser().getUid();
        usersRef=FirebaseDatabase.getInstance().getReference().child("Users");

        intializeFields();


        usersRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {

                    if(dataSnapshot.hasChild("profileimage")) { //profil resmi default olarak vermedim. eğer pp yoksa crash etmesin diye kontrol ettim. pp yoksa bu kısmı atlayıp devam et.

                        String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(PersonProfileActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);

                    }

                        String myUsername = dataSnapshot.child("username").getValue().toString();
                        String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                        String myProfileStatus = dataSnapshot.child("status").getValue().toString();
                        String myDOB = dataSnapshot.child("dob").getValue().toString();
                        String myCountry = dataSnapshot.child("country").getValue().toString();
                        String myGender = dataSnapshot.child("gender").getValue().toString();
                        String myRelationStatus = dataSnapshot.child("relationship").getValue().toString();




                        userName.setText("@" + myUsername);
                        userProfName.setText(myProfileName);
                        userStatus.setText(myProfileStatus);
                        userCountry.setText("Country: " + myCountry);
                        userGender.setText("Gender: " + myGender);
                        userDOB.setText("Date of Birth:" + myDOB);
                        userRelations.setText("Relationship: " + myRelationStatus);


                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    private void intializeFields()
    {
        userName=findViewById(R.id.person_username);
        userProfName=findViewById(R.id.person_full_name);
        userCountry=findViewById(R.id.person_country);
        userStatus=findViewById(R.id.person_status);
        userGender=findViewById(R.id.person_gender);
        userRelations=findViewById(R.id.person_relationship_status);
        userDOB=findViewById(R.id.person_dob);
        userProfileImage=findViewById(R.id.person_profile_pic);

        sendFriendRequestButton=findViewById(R.id.person_send_friend_request_btn);
        declineFriendRequestButton=findViewById(R.id.person_decline_friend_request_btn);
    }
}

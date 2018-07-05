package com.example.plus.socialnetwork;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {


    private ImageView postImage;
    private TextView postDescription;
    private Button deletePostButton, editPostButton;

    private String postKey;
    private DatabaseReference clickPostRef;
    private FirebaseAuth mAuth;
    private String currentUserID,databaseUserID;

   private String description;
   private String image;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        postImage=findViewById(R.id.click_postimage);
        postDescription=findViewById(R.id.click_postdescription);
        deletePostButton=findViewById(R.id.deletePostButton);
        editPostButton=findViewById(R.id.editPostButton);




        mAuth= FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        postKey=getIntent().getExtras().get("postKey").toString();
        clickPostRef= FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey);


        deletePostButton.setVisibility(View.INVISIBLE);
        editPostButton.setVisibility(View.INVISIBLE);

        clickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


           if (dataSnapshot.exists())
           {
               databaseUserID=dataSnapshot.child("uid").getValue().toString();

               description=dataSnapshot.child("description").getValue().toString();
               image=dataSnapshot.child("postimage").getValue().toString();

               postDescription.setText(description);
               Picasso.with(ClickPostActivity.this).load(image).into(postImage);




               if (currentUserID.equals(databaseUserID))
               {
                   deletePostButton.setVisibility(View.VISIBLE);
                   editPostButton.setVisibility(View.VISIBLE);
               }


               editPostButton.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View view) {

                       editCurrentPost(description);

                   }
               });

           }

         }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        deletePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteCurrentPost();
            }
        });




    }

    private void editCurrentPost(String description)
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post");


        final EditText inputField=new EditText(ClickPostActivity.this);
        inputField.setText(description);
        builder.setView(inputField);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                clickPostRef.child("description").setValue(inputField.getText().toString());
                Toast.makeText(ClickPostActivity.this, "Post has been updated successfully", Toast.LENGTH_SHORT).show();

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.cancel();

            }
        });

        Dialog dialog=builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(R.color.MyWhite);
    }

    private void deleteCurrentPost()
    {
        clickPostRef.removeValue();
        sendUserToMainActivity();
        Toast.makeText(this, "Post has been deleted", Toast.LENGTH_SHORT).show();
    }

    private void sendUserToMainActivity()
    {
        Intent mainIntent=new Intent(ClickPostActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}

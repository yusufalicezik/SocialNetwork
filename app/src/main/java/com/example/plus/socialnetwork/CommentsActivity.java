package com.example.plus.socialnetwork;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity {

    private RecyclerView commentsList;
    private ImageButton postCommentButton;
    private EditText commentInputText;

    private String postKey,currentUserID;


    private DatabaseReference usersRef,postsRef;
    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);




        postKey=getIntent().getExtras().get("postKey").toString();


        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postsRef= FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey).child("Comments");


        commentsList=findViewById(R.id.comments_list);
        commentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        commentsList.setLayoutManager(linearLayoutManager);


        postCommentButton=findViewById(R.id.post_comment_button);
        commentInputText=findViewById(R.id.comment_input);



        postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists())
                        {
                            String userName=dataSnapshot.child("username").getValue().toString();

                            validateComment(userName);

                            commentInputText.setText("");
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        });



    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Comments,CommentsViewHolder> firebaseRecyclerAdapter=
                new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>
                        (
                            Comments.class,
                             R.layout.all_comments_layout,
                             CommentsViewHolder.class,
                             postsRef
                        )
                {
                    @Override
                    protected void populateViewHolder(CommentsViewHolder viewHolder, Comments model, int position) {

                        viewHolder.setUsername(model.getUsername());
                        viewHolder.setComment(model.getComment());
                        viewHolder.setDate(model.getDate());
                        viewHolder.setTime(model.getTime());


                    }
                };

        commentsList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public CommentsViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
        }

        public void setUsername(String username) {

            TextView myUsername=mView.findViewById(R.id.comment_username);
            myUsername.setText("@"+username+" ");

        }

        public void setTime(String time) {

            TextView myTime=mView.findViewById(R.id.comment_time);
            myTime.setText(" Time:"+time);

        }

        public void setDate(String date) {

            TextView myDate=mView.findViewById(R.id.comment_date);
            myDate.setText(" Date:"+date);
        }

        public void setComment(String comment) {

            TextView myComment=mView.findViewById(R.id.comment_text);
            myComment.setText(comment);

        }

    }









    private void validateComment(String userName)
    {
        String commentText=commentInputText.getText().toString();
        if (TextUtils.isEmpty(commentText))
        {
            Toast.makeText(this, "Enter a comment", Toast.LENGTH_SHORT).show();
        }
        else
        {

            Calendar calForDate=Calendar.getInstance();
            SimpleDateFormat currentDate=new SimpleDateFormat("dd-MMMM-yyyy");
            final String saveCurrentDate=currentDate.format(calForDate.getTime());

            Calendar calForTime=Calendar.getInstance();
            SimpleDateFormat currentTime=new SimpleDateFormat("HH:mm");
            final String saveCurrentTime=currentTime.format(calForTime.getTime());


            final String randomKey=currentUserID+saveCurrentDate+saveCurrentTime;

            HashMap commentsMap=new HashMap();

            commentsMap.put("uid",currentUserID);
            commentsMap.put("comment",commentText);
            commentsMap.put("date",saveCurrentDate);
            commentsMap.put("time",saveCurrentTime);
            commentsMap.put("username",userName);


            postsRef.child(randomKey).updateChildren(commentsMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if (task.isSuccessful())
                    {
                        Toast.makeText(CommentsActivity.this, "You have commented successfully.", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(CommentsActivity.this, "Error,Try again", Toast.LENGTH_SHORT).show();
                    }

                }
            });





        }
    }
}

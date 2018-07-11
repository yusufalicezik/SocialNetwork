package com.example.plus.socialnetwork;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity {


    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private RecyclerView postList;
    private Toolbar mToolbar;


    private FirebaseAuth mAuth;
    private DatabaseReference usersRef,postsRef,likesRef;

    private CircleImageView navProfileImage;
    private TextView navProfileUsername;



    boolean likeChecker=false;



    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth=FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()==null)
        {
            sendUserToLoginActivity();
        }
        else {
            currentUserID = mAuth.getCurrentUser().getUid();

            usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
            postsRef=FirebaseDatabase.getInstance().getReference().child("Posts");
            likesRef=FirebaseDatabase.getInstance().getReference().child("Likes");


            mToolbar = findViewById(R.id.main_page_toolbar);
            setSupportActionBar(mToolbar);
            getSupportActionBar().setTitle("Home");

            drawerLayout = findViewById(R.id.drawable_layout);
            actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
            drawerLayout.addDrawerListener(actionBarDrawerToggle);
            actionBarDrawerToggle.syncState();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            navigationView = findViewById(R.id.navigation_view);





            //
            postList=findViewById(R.id.all_users_post_list);
            postList.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this); //verileri tersten almak için. en son yüklenen ilk.
            linearLayoutManager.setReverseLayout(true);
            linearLayoutManager.setStackFromEnd(true);
            postList.setLayoutManager(linearLayoutManager);



            View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
            navProfileImage = navView.findViewById(R.id.nav_profile_image);
            navProfileUsername = navView.findViewById(R.id.nav_user_fullname);


            usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if(dataSnapshot.exists()) {

                        if (dataSnapshot.hasChild("fullname")) {
                            String fullname = dataSnapshot.child("fullname").getValue().toString();

                            navProfileUsername.setText(fullname);
                        }
                        if (dataSnapshot.hasChild("profileimage")) {
                            String image = dataSnapshot.child("profileimage").getValue().toString();
                            Picasso.with(MainActivity.this).load(image).placeholder(R.drawable.profile).into(navProfileImage);
                        }
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    UserMenuSelector(item);
                    return false;
                }
            });

           /* addNewPostButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SendUserToPostActivity();
                }
            });
            */


            DisplayAllUsersPosts();

        }
    }

    private void DisplayAllUsersPosts()
    {


        Query sortPostsInDecendingOrder=postsRef.orderByChild("counter");







        //eklediğimiz firebase ui database kütüphanesi ile.. ;
        FirebaseRecyclerAdapter<Posts,PostsViewHolder> firebaseRecyclerAdapter=
                new FirebaseRecyclerAdapter<Posts, PostsViewHolder>
                        (
                            Posts.class,
                            R.layout.all_posts_layout,
                            PostsViewHolder.class,
                           // değil, eski sıralamaydı bu, yanlıs calısıyordu. postsRef
                            sortPostsInDecendingOrder
                        )
                {
                    @Override
                    protected void populateViewHolder(PostsViewHolder viewHolder, Posts model, int position)
                    {

                        //to click
                        final String postKey=getRef(position).getKey();
                        //to click end



                        viewHolder.setFullname(model.getFullname());
                        viewHolder.setTime(model.getTime());
                        viewHolder.setDate(model.getDate());
                        viewHolder.setDescription(model.getDescription());
                        viewHolder.setProfileimage(getApplicationContext(),model.getProfileimage());
                        viewHolder.setPostimage(getApplicationContext(),model.getPostimage());


                        viewHolder.setLikeButtonStatus(postKey);


                        //to click
                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent clickPostIntent=new Intent(MainActivity.this,ClickPostActivity.class);
                                clickPostIntent.putExtra("postKey",postKey);
                                startActivity(clickPostIntent);
                            }
                        });
                        //end click


                        viewHolder.commentPostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent commentsIntent=new Intent(MainActivity.this,CommentsActivity.class);
                                commentsIntent.putExtra("postKey",postKey);
                                startActivity(commentsIntent);
                            }
                        });


                        viewHolder.likePostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                likeChecker=true;

                                likesRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {


                                       if(likeChecker==true)
                                       {
                                           if (dataSnapshot.child(postKey).hasChild(currentUserID))
                                           {

                                               likesRef.child(postKey).child(currentUserID).removeValue();
                                               likeChecker=false;

                                           }
                                           else
                                           {
                                               likesRef.child(postKey).child(currentUserID).setValue(true);
                                               likeChecker=false;
                                           }
                                       }


                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }
                        });

                    }
                };
        postList.setAdapter(firebaseRecyclerAdapter);

    }


    public static class PostsViewHolder extends RecyclerView.ViewHolder
    {

        View mView;


        ImageButton likePostButton, commentPostButton;
        TextView displayNoOfLikes;

        int countLikes;
        String currentUserID;
        DatabaseReference likesRef;


        public PostsViewHolder(View itemView) {
            super(itemView);
            mView=itemView;


            likePostButton=mView.findViewById(R.id.like_button);
            commentPostButton=mView.findViewById(R.id.comment_button);
            displayNoOfLikes=mView.findViewById(R.id.display_no_of_likes);


            likesRef=FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserID=FirebaseAuth.getInstance().getCurrentUser().getUid();

        }



        public void setLikeButtonStatus(final String postKey)
        {

            likesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(postKey).hasChild(currentUserID))
                    {
                        countLikes=(int)dataSnapshot.child(postKey).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.like);
                        displayNoOfLikes.setText(String.valueOf(countLikes+" Likes"));
                    }
                    else
                    {
                        countLikes=(int)dataSnapshot.child(postKey).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.dislike);
                        displayNoOfLikes.setText(String.valueOf(countLikes+" Likes"));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }


        public void setFullname(String fullname)
        {
         TextView username=mView.findViewById(R.id.post_user_name);
         username.setText(fullname);
        }
        public void setProfileimage(Context ctx,String profileimage)
        {
            CircleImageView image=mView.findViewById(R.id.post_profile_image);
          //  Picasso.get().load(profileimage).placeholder(R.drawable.profile).into(image);
            Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(image);
        }

        public void setTime(String time)
        {
            TextView postTime=mView.findViewById(R.id.post_time);
            postTime.setText("   "+time);
        }

        public void setDate(String date)
        {
            TextView postDate=mView.findViewById(R.id.post_date);
            postDate.setText("   "+date);
        }
        public void setDescription(String description)
        {
            TextView postDescription=mView.findViewById(R.id.post_description);
            postDescription.setText(description);
        }
        public void setPostimage(Context ctx,String postimage)
        {
            ImageView postImage=mView.findViewById(R.id.post_image);
            //  Picasso.get().load(profileimage).placeholder(R.drawable.profile).into(image);
            Picasso.with(ctx).load(postimage).into(postImage);
        }
    }



    private void SendUserToPostActivity()
    {
        Intent postIntent=new Intent(MainActivity.this,PostActivity.class);
        startActivity(postIntent);
        finish();
    }


    //ilk açılışta kullanıcı girişi yapılmış mı diye kontrol ve eksik bilgiler girilmiş mi
    @Override
    protected void onStart() {
        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser==null)
            sendUserToLoginActivity();
        else
            checkUserExistence();

        super.onStart();
    }

    private void checkUserExistence()
    {

        final String currentUser_id=mAuth.getCurrentUser().getUid();

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(currentUser_id))
                {
                    Toast.makeText(MainActivity.this, "You have an Account but you have to give us more information about yourself", Toast.LENGTH_LONG).show();
                    SendUserToSetupActivity();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void SendUserToSetupActivity()
    {
        Intent setupIntent=new Intent(MainActivity.this,SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();

    }

    private void sendUserToLoginActivity()
    {
        Intent loginIntent=new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(actionBarDrawerToggle.onOptionsItemSelected(item))
        {
         return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.nav_post:
               SendUserToPostActivity();
                break;
            case R.id.nav_profile:
                sendUserToProfileActivity();
                break;
            case R.id.nav_find_friends:
                sendUserToFindFriendsActivity();
                break;
            case R.id.nav_friends:
                Toast.makeText(this, "Friend List", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_message:
                Toast.makeText(this, "Messages", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_settings:
                sendUserToSettingsActivity();
                break;
            case R.id.nav_Logout:
                mAuth.signOut();
                Toast.makeText(this, "Log Out", Toast.LENGTH_SHORT).show();
                sendUserToLoginActivity();

                break;


        }
    }

    private void sendUserToFindFriendsActivity()
    {
        Intent findFriendsIntent=new Intent(MainActivity.this,FindFriendsActivity.class);
        startActivity(findFriendsIntent);
    }

    private void sendUserToProfileActivity()
    {
        Intent profileIntent=new Intent(MainActivity.this,ProfileActivity.class);
        startActivity(profileIntent);
    }

    private void sendUserToSettingsActivity()
    {
        Intent settingsIntent=new Intent(MainActivity.this,SettingsActivity.class);
        startActivity(settingsIntent);

    }


}

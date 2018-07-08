package com.example.plus.socialnetwork;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton searchButton;
    private EditText searchInputText;
    private RecyclerView searchResultList;

    private DatabaseReference allUsersDatabaseRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        mToolbar=findViewById(R.id.find_friends_app_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Find People");

        allUsersDatabaseRef= FirebaseDatabase.getInstance().getReference().child("Users");


        searchResultList=findViewById(R.id.search_result_list);
        searchResultList.setHasFixedSize(true);
        searchResultList.setLayoutManager(new LinearLayoutManager(this));


        searchButton=findViewById(R.id.search_people_friends_button);
        searchInputText=findViewById(R.id.search_box_input);


        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                String searchBoxInput=searchInputText.getText().toString();

                searchPeopleAndFriends(searchBoxInput);

            }
        });


    }

    private void searchPeopleAndFriends(String searchBoxInput)
    {

        Toast.makeText(this, "Searching...", Toast.LENGTH_LONG).show();

        Query searchPeopleandFriendsQuery=allUsersDatabaseRef.orderByChild("fullname")
                .startAt(searchBoxInput).endAt(searchBoxInput+"\uf8ff");


        FirebaseRecyclerAdapter<FindFriends,FindFriendsViewOrder> firebaseRecyclerAdapter=
                new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewOrder>
                        (
                                FindFriends.class,
                                R.layout.all_users_display_layout,
                                FindFriendsViewOrder.class,
                               // değil artık query ye göre veri alsın. allUsersDatabaseRef
                                searchPeopleandFriendsQuery

                        )
                {
                    @Override
                    protected void populateViewHolder(FindFriendsViewOrder viewHolder, FindFriends model, final int position)
                    {
                        viewHolder.setFullname(model.getFullname());
                        viewHolder.setStatus(model.getStatus());
                        viewHolder.setProfileimage(getApplicationContext(),model.getProfileimage());


                        ///tıklandığında kişinin profiline gitmek için... personprofile..
                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                String visit_user_id=getRef(position).getKey(); //hangi kullanıcıya tıklamışız onun key ini alma

                                Intent profileIntent=new Intent(FindFriendsActivity.this,PersonProfileActivity.class);
                                profileIntent.putExtra("visit_user_id",visit_user_id);
                                startActivity(profileIntent);


                            }
                        });
                        //person profile end

                    }
                };

        searchResultList.setAdapter(firebaseRecyclerAdapter);
    }


    public static class FindFriendsViewOrder extends RecyclerView.ViewHolder
    {
        View mView;

        public FindFriendsViewOrder(View itemView)
        {
            super(itemView);
            mView=itemView;
        }

        public void setProfileimage(Context ctx,String profileimage)
        {

            CircleImageView myImage=mView.findViewById(R.id.all_users_profile_image);
            Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(myImage);
        }
        public void setFullname(String fullname)
        {
            TextView myName=mView.findViewById(R.id.all_users_profile_full_name);
            myName.setText(fullname);
        }
        public void setStatus(String status)
        {
            TextView myStatus=mView.findViewById(R.id.all_users_status);
            myStatus.setText(status);
        }


    }


}

package com.example.mychat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mychat.model.Users;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUserActivity extends AppCompatActivity {

    private static final String TAG = "AllUserActivity";

    private Toolbar mToolbar;
    private RecyclerView mUserList;

    private DatabaseReference mUserDatabaseRef;
    FirebaseRecyclerOptions<Users> options;
    FirebaseRecyclerAdapter<Users, UserViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_user);

        mToolbar = (Toolbar) findViewById(R.id.users_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUserDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mUserList = (RecyclerView) findViewById(R.id.users_list);
        mUserList.setHasFixedSize(true);
        mUserList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        options = new FirebaseRecyclerOptions.Builder<Users>().setQuery(mUserDatabaseRef,Users.class).build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull Users model) {
                holder.setDisplayName(model.getName());
                holder.setStatus(model.getStatus());
                holder.setUserImage(model.getThumb_img(),getApplicationContext());

                final String user_id = getRef(position).getKey();

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileIntent = new Intent(AllUserActivity.this,ProfileActivity.class);
                        profileIntent.putExtra("user_id",user_id);
                        startActivity(profileIntent);
                    }
                });
                Log.d(TAG, "onBindViewHolder: Counted");
            }
            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_single_layout,parent,false);
                Log.d(TAG, "onCreateViewHolder: create view");
                return new UserViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.startListening();
        mUserList.setAdapter(firebaseRecyclerAdapter);
        Log.d(TAG, "onCreate: created");
        
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: start");
        if(firebaseRecyclerAdapter!=null)
            firebaseRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: stop");
        if(firebaseRecyclerAdapter!=null)
            firebaseRecyclerAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: resume");
        if(firebaseRecyclerAdapter!=null)
            firebaseRecyclerAdapter.startListening();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        View mView;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDisplayName(String name) {
            TextView userNameView = (TextView) mView.findViewById(R.id.single_user_name);
            userNameView.setText(name);
        }

        public void setStatus(String status) {
            TextView statusView = (TextView) mView.findViewById(R.id.single_user_status);
            statusView.setText(status);
        }

        public void setUserImage(String thumb_img, Context ctx) {
            CircleImageView thumbImageView = (CircleImageView) mView.findViewById(R.id.single_user_thumb);

            Picasso.get().load(thumb_img).placeholder(R.drawable.default_avtar).into(thumbImageView);
        }
    }
}

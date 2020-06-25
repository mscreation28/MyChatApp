package com.example.mychat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String mChatUser;

    private Toolbar mChatToolbar;

    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;

    private TextView mTitleView;
    private TextView mLastSeenView;
    private CircleImageView mProfileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        mChatUser = getIntent().getStringExtra("user_id");
        String chatUserName = getIntent().getStringExtra("user_name");

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mChatToolbar = (Toolbar) findViewById(R.id.chat_appbar);
        setSupportActionBar(mChatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar,null);

        actionBar.setCustomView(action_bar_view);

        //Custom Bar Item
        mTitleView = (TextView) action_bar_view.findViewById(R.id.chat_user_name);
        mLastSeenView = (TextView) action_bar_view.findViewById(R.id.chat_user_lastseen);
        mProfileImageView = (CircleImageView) action_bar_view.findViewById(R.id.chat_user_thumb);

        mTitleView.setText(chatUserName);

        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String onlineStatus = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("thumb_img").toString();

                if(onlineStatus.equals("true")) {
                    mLastSeenView.setText("Online");
                }
                else {
                    mLastSeenView.setText(onlineStatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}

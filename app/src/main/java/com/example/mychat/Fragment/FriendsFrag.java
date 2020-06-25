package com.example.mychat.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mychat.AllUserActivity;
import com.example.mychat.ChatActivity;
import com.example.mychat.ProfileActivity;
import com.example.mychat.R;
import com.example.mychat.model.Friends;
import com.example.mychat.model.Users;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFrag extends Fragment {

    private static final String TAG = "FriendsFrag";

    private RecyclerView mFriendsList;

    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;

    private String mCurrentUserId;

    private View mMainView;

    FirebaseRecyclerOptions<Friends> friendOptions;
    FirebaseRecyclerAdapter<Friends,FriendsViewHolder> friendsRecycleAdapter;

    public FriendsFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);

        mFriendsList = (RecyclerView) mMainView.findViewById(R.id.friends_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUserId);
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mFriendsDatabase.keepSynced(true);

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;

    }

    @Override
    public void onStart() {
        super.onStart();

        friendOptions = new FirebaseRecyclerOptions.Builder<Friends>().setQuery(mFriendsDatabase,Friends.class).build();

        friendsRecycleAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(friendOptions) {
            @Override
            protected void onBindViewHolder(final FriendsViewHolder friendsViewHolder, int i, Friends friends) {
                friendsViewHolder.setDate(friends.getDate());

                final String list_user_id = getRef(i).getKey();
                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_img").getValue().toString();

                        if(dataSnapshot.hasChild("online")) {
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            friendsViewHolder.setUserOnline(userOnline);
                        }
                        friendsViewHolder.setName(userName);
                        friendsViewHolder.setUserImage(userThumb,getContext());

                        friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                 CharSequence options[] = new CharSequence[]{"Open Profile","Send Message"};

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        if(which==0) {
                                            Intent profileIntent = new Intent(getContext() , ProfileActivity.class);
                                            profileIntent.putExtra("user_id",list_user_id);
                                            startActivity(profileIntent);
                                        }
                                        if(which==1) {
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("user_id",list_user_id);
                                            chatIntent.putExtra("user_name",userName);
                                            startActivity(chatIntent);
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_single_layout,parent,false);
                Log.d(TAG, "onCreateViewHolder: create view");
                return new FriendsViewHolder(view);
            }
        };

        mFriendsList.setAdapter(friendsRecycleAdapter);
        Log.d(TAG, "onStart: start");
        if(friendsRecycleAdapter!=null)
            friendsRecycleAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: stop");
        if(friendsRecycleAdapter!=null)
            friendsRecycleAdapter.stopListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: resume");
        if(friendsRecycleAdapter!=null)
            friendsRecycleAdapter.startListening();
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;
        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDate(String date) {
            TextView statusView = (TextView) mView.findViewById(R.id.single_user_status);
            statusView.setText(date);
        }

        public void setName(String userName) {
            TextView statusView = (TextView) mView.findViewById(R.id.single_user_name);
            statusView.setText(userName);
        }

        public void setUserImage(String thumb_img, Context ctx) {
            CircleImageView thumbImageView = (CircleImageView) mView.findViewById(R.id.single_user_thumb);

            Picasso.get().load(thumb_img).placeholder(R.drawable.default_avtar).into(thumbImageView);
        }

        public void setUserOnline(String userOnline) {
            ImageView userOnlineView = (ImageView) mView.findViewById(R.id.single_onine_icon);
            if(userOnline.equals("true")) {
                userOnlineView.setVisibility(View.VISIBLE);
            }
            else {
                userOnlineView.setVisibility(View.GONE);
            }
        }
    }
}

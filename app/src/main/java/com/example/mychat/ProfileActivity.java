package com.example.mychat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextView mUserDisplayName,mUserStatus,mProfileFriendCount;
    private ImageView mUserProfileImg;
    private Button mSendReqBtn,mDeclineReqBtn;

    private DatabaseReference mUserDatabse;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mRootRef;

    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgressDialog;

    private int mCurrentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String userId = getIntent().getStringExtra("user_id");

        mUserDatabse = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notification");
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mUserDisplayName = (TextView) findViewById(R.id.profileview_name);
        mUserStatus = (TextView) findViewById(R.id.profileview_status);
        mProfileFriendCount = (TextView) findViewById(R.id.profileview_friend_count);
        mUserProfileImg = (ImageView) findViewById(R.id.profileview_image);
        mSendReqBtn = (Button) findViewById(R.id.send_request_btn);
        mDeclineReqBtn = (Button) findViewById(R.id.decline_friend_req);
        mDeclineReqBtn.setVisibility(View.GONE);
        mDeclineReqBtn.setEnabled(false);

        mCurrentState = 0;

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Load User Data");
        mProgressDialog.setMessage("Please wait while we load the user data.");
        mProgressDialog.setCanceledOnTouchOutside(true);
        mProgressDialog.show();

        mUserDatabse.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String displayName = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mUserDisplayName.setText(displayName);
                mUserStatus.setText(status);

                Picasso.get().load(image).placeholder(R.drawable.default_avtar).into(mUserProfileImg);

                //-----------FRIEND LIST / REQUEST FEATURE----------//
                mFriendReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(userId)) {
                            int reqType = Integer.parseInt(dataSnapshot.child(userId).child("RequestType").getValue().toString());
                            if(reqType==2) {
                                mCurrentState = 2;
                                mSendReqBtn.setText("Accept Friend Request");
                                mDeclineReqBtn.setVisibility(View.VISIBLE);
                                mDeclineReqBtn.setEnabled(true);
                            }
                            else if(reqType==1) {
                                mCurrentState = 1;
                                mSendReqBtn.setText("Cancel Friend Request");
                            }
                            mProgressDialog.dismiss();

                        }
                        else {
                            mFriendsDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(userId)) {
                                        mCurrentState=3;
                                        mSendReqBtn.setText("UnFriend");
                                    }
                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        /*
        Not Friend : 0;
        Friend Req Sent : 1;
        Friend Req Recieved : 2;
        Friends : 3;
         */
        mSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mSendReqBtn.setEnabled(false);
                //------------- SEND FRIEND REQUEST------------//
                if(mCurrentState==0) {

                    DatabaseReference newNotificationRef = mRootRef.child("Notification").child(userId).push();
                    String newNotificationId = newNotificationRef.getKey();

                    HashMap<String,String> notificationData = new HashMap<>();
                    notificationData.put("from",mCurrentUser.getUid());
                    notificationData.put("type","request");

                    Map requestMap =new HashMap<>();
                    requestMap.put("Friend_req/"+mCurrentUser.getUid()+"/"+userId+"/RequestType",1);
                    requestMap.put("Friend_req/"+userId+"/"+mCurrentUser.getUid()+"/RequestType",2);
                    requestMap.put("Notification/"+userId+"/"+newNotificationId,notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    if(databaseError!=null) {
                                        Toast.makeText(ProfileActivity.this, "Failed Sending Request", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        mCurrentState = 1;
                                        mSendReqBtn.setText("Cancel Friend Request");
                                    }
                                    mSendReqBtn.setEnabled(true);
                                }
                            });
                }

                //------------- CANCEL FRIEND REQUEST------------//
                if(mCurrentState==1) {

                    Map cancleFriendMap = new HashMap();
                    cancleFriendMap.put("Friend_req/"+mCurrentUser.getUid()+"/"+userId,null);
                    cancleFriendMap.put("Friend_req/"+userId+"/"+mCurrentUser.getUid(),null);

                    mRootRef.updateChildren(cancleFriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError==null) {
                                mSendReqBtn.setEnabled(true);
                                mCurrentState=0;
                                mSendReqBtn.setText("Send Friend Request");
                            }
                            else {
                                Toast.makeText(ProfileActivity.this, "Failed to Cancle Request", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                //--------------REQUEST RECEIVED STATE-----------------//
                if(mCurrentState==2) {
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map acceptFriendMap = new HashMap();
                    acceptFriendMap.put("Friend_req/"+mCurrentUser.getUid()+"/"+userId,null);
                    acceptFriendMap.put("Friend_req/"+userId+"/"+mCurrentUser.getUid(),null);
                    acceptFriendMap.put("Friends/"+mCurrentUser.getUid()+"/"+userId+"/date",currentDate);
                    acceptFriendMap.put("Friends/"+userId+"/"+mCurrentUser.getUid()+"/date",currentDate);

                    mRootRef.updateChildren(acceptFriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError==null) {
                                mSendReqBtn.setEnabled(true);
                                mDeclineReqBtn.setEnabled(false);
                                mDeclineReqBtn.setVisibility(View.GONE);
                                mCurrentState=3;
                                mSendReqBtn.setText("UnFriend");
                            }
                            else {
                                Toast.makeText(ProfileActivity.this, "Failed to Accept Request", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                //----------UNFRIEND FRIEND----------//
                if(mCurrentState==3) {
                    mSendReqBtn.setEnabled(false);
                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/"+mCurrentUser.getUid()+"/"+userId,null);
                    unfriendMap.put("Friends/"+userId+"/"+mCurrentUser.getUid(),null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError==null) {
                                mSendReqBtn.setEnabled(true);
                                mCurrentState=0;
                                mSendReqBtn.setText("Send Friend Request");
                            }
                            else {
                                Toast.makeText(ProfileActivity.this, "Failed Unfriend", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        mDeclineReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map DecReqdMap = new HashMap();
                DecReqdMap.put("Friend_req/"+mCurrentUser.getUid()+"/"+userId,null);
                DecReqdMap.put("Friend_req/"+userId+"/"+mCurrentUser.getUid(),null);

                mRootRef.updateChildren(DecReqdMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if(databaseError==null) {
                            mSendReqBtn.setEnabled(true);
                            mDeclineReqBtn.setVisibility(View.GONE);
                            mDeclineReqBtn.setEnabled(false);
                            mCurrentState=0;
                            mSendReqBtn.setText("Send Friend Request");
                        }
                        else {
                            Toast.makeText(ProfileActivity.this, "Failed to Decline Request", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

}

package com.example.mychat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private TextInputLayout mDisplayName;
    private TextInputLayout mEmail;
    private TextInputLayout mRegPwd;
    private Button mRegBtn;

    private Toolbar mToolbar;
    private ProgressDialog mRegProgress;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mDisplayName = (TextInputLayout) findViewById(R.id.reg_name);
        mEmail = (TextInputLayout) findViewById(R.id.reg_email);
        mRegPwd = (TextInputLayout) findViewById(R.id.reg_pwd);
        mRegBtn = (Button) findViewById(R.id.reg_submit_btn);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mRegProgress = new ProgressDialog(this);

        mToolbar = findViewById(R.id.register_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String displayName = mDisplayName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String pwd = mRegPwd.getEditText().getText().toString();

                if(!TextUtils.isEmpty(displayName) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(pwd)) {
                    mRegProgress.setTitle("Registering User");
                    mRegProgress.setMessage("Please wait while we create your account !");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();
                    registerUser(displayName, email, pwd);
                }
            }
        });
    }

    private void registerUser(final String displayName, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            String uid = user.getUid();
                            String deviceTokenId = FirebaseInstanceId.getInstance().getToken();

                            mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                            HashMap<String,String> userMap = new HashMap<>();
                            userMap.put("name",displayName);
                            userMap.put("status","Hey there, I am using MyChatApp");
                            userMap.put("image","default");
                            userMap.put("thumb_img","default");
                            userMap.put("device_token",deviceTokenId);

                            mDatabaseRef.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        mRegProgress.dismiss();
                                        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();
                                    }
                                }
                            });
                        } else {
                            mRegProgress.hide();
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}

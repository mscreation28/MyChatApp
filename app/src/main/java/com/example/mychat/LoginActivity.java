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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private TextInputLayout mEmail;
    private TextInputLayout mLoginPwd;
    private Button mLoginBtn;

    private Toolbar mToolbar;
    private ProgressDialog mLoginProgress;

    private DatabaseReference mUserData;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmail = (TextInputLayout) findViewById(R.id.login_email);
        mLoginPwd = (TextInputLayout) findViewById(R.id.login_pwd);
        mLoginBtn = (Button) findViewById(R.id.login_submit_btn);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mUserData = FirebaseDatabase.getInstance().getReference().child("Users");

        mLoginProgress = new ProgressDialog(this);

        mToolbar = findViewById(R.id.login_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Login Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getEditText().getText().toString();
                String password = mLoginPwd.getEditText().getText().toString();

                if(!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)) {
                    mLoginProgress.setTitle("Logging In");
                    mLoginProgress.setMessage("Please wait while we check your credentials !");
                    mLoginProgress.setCanceledOnTouchOutside(false);
                    mLoginProgress.show();

                    loginUser(email,password);
                }
            }
        });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");

                            String deviceTokenId = FirebaseInstanceId.getInstance().getToken();
                            String userId = mAuth.getCurrentUser().getUid();
                            mUserData.child(userId).child("device_token").setValue(deviceTokenId).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mLoginProgress.dismiss();
                                    Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(mainIntent);
                                    finish();
                                }
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                            mLoginProgress.hide();
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}

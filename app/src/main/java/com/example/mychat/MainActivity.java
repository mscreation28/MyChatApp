package com.example.mychat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.mychat.Adapters.ViewPageAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;

    private DatabaseReference mUserRef;

    private ViewPager mViewPager;
    private ViewPageAdapter mViewPageAdapter;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mToolbar = findViewById(R.id.main_page_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("My Chat");

        mViewPager = (ViewPager) findViewById(R.id.main_pager);
        mViewPageAdapter = new ViewPageAdapter(getSupportFragmentManager());
        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        mViewPager.setAdapter(mViewPageAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser==null) {
            sendToStart();
        }
        else {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            mUserRef.child("online").setValue("true");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mUserRef!=null) {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId())
        {
            case R.id.main_logout:
                mAuth.signOut();
                Log.d(TAG, "onOptionsItemSelected: log out");
                sendToStart();
                break;
            case  R.id.account_setting:
                Intent settingIntent = new Intent(MainActivity.this,SettingActivity.class);
                startActivity(settingIntent);
                break;
            case R.id.all_user:
                Intent allUserIntent = new Intent(MainActivity.this,AllUserActivity.class);
                startActivity(allUserIntent);
                break;
        }

        return true;
    }

    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this,StartActivity.class);
        startActivity(startIntent);
        finish();
    }
}

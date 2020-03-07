package com.example.mysns;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.mysns.Fragments.MyPageFragment;
import com.example.mysns.Fragments.NewsfeedFragment;
import com.example.mysns.Fragments.PostFragment;
import com.example.mysns.Fragments.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "TAG_main";

    public static Activity activity;

    private RelativeLayout button_logout, button_mypage;

    private FragmentManager fm;
    private FragmentTransaction ft;

    private NewsfeedFragment newsfeedFragment;
    private PostFragment postFragment;
    private MyPageFragment myPageFragment;
    private SettingsFragment settingsFragment;

    private BottomNavigationView bottomNavigationView;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = MainActivity.this;

        mAuth = FirebaseAuth.getInstance();

//        checking user auth
        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null){
            Log.d(TAG, "user logged out");
            //          back to Login Activity if there is no user information
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Sorry");
            builder.setMessage("We cannot access your information, so you have to login again.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    goActivity(LoginActivity.class);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        button_logout = findViewById(R.id.button_logout);
        button_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Are you sure you want to logout?");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAuth.signOut();
                        Log.d(TAG, "user logged out");
                        goActivity(LoginActivity.class);
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

//        initiating bottom navigation view and fragments

        newsfeedFragment = new NewsfeedFragment();
        postFragment = new PostFragment();
        myPageFragment = new MyPageFragment();
        settingsFragment = new SettingsFragment();

        bottomNavigationView = findViewById(R.id.bottomNavi);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId()){
                    case R.id.action_newsfeed:
                        setFragment(0);
                        break;
                    case R.id.action_post:
                        setFragment(1);
                        break;
                    case R.id.action_mypage:
                        setFragment(2);
                        break;
                    case R.id.action_settings:
                        setFragment(3);
                        break;
                }
                return true;
            }
        });

//        initiating first fragment page
        setFragment(0);
    }

    private void goActivity(Class c){
        Intent intent = new Intent(MainActivity.this, c);
        startActivity(intent);
        finish();
    }

//    changing fragment
    private void setFragment(int n){
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();

        switch(n){
            case 0:
                ft.replace(R.id.frameLayout, newsfeedFragment);
                ft.commit();
                break;
            case 1:
                ft.replace(R.id.frameLayout, postFragment);
                ft.commit();
                break;
            case 2:
                ft.replace(R.id.frameLayout, myPageFragment);
                ft.commit();
                break;
            case 3:
                ft.replace(R.id.frameLayout, settingsFragment);
                ft.commit();
                break;
        }
    }
}

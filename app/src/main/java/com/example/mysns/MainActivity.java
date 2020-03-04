package com.example.mysns;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "TAG_main";

    private RelativeLayout button_logout;
    private TextView textView_userNickname;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView_userNickname = findViewById(R.id.textView_userNickname);

        mAuth = FirebaseAuth.getInstance();

//        getting user nickname
        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null){
            Log.d(TAG, "user logged out");
            goActivity(LoginActivity.class);
        }else{
            String userNickname = user.getDisplayName();
            if(userNickname == null){
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
            }else{
                textView_userNickname.setText("Hi, "+userNickname);
            }
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
    }

    private void goActivity(Class c){
        Intent intent = new Intent(MainActivity.this, c);
        startActivity(intent);
        finish();
    }
}

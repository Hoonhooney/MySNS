package com.example.mysns.Activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mysns.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {
    private static final String TAG = "TAG_reset_password";

    private EditText editText_email;
    private RelativeLayout button_send;

    private String email = "";

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        editText_email = findViewById(R.id.editText_resetp_email);

        button_send = findViewById(R.id.button_resetp_send);
        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = editText_email.getText().toString();
                send();
            }
        });

        mAuth = FirebaseAuth.getInstance();
    }

    private void send(){
        if(email.isEmpty()){
            Toast.makeText(ResetPasswordActivity.this, "Please text your correct email address",
                    Toast.LENGTH_SHORT).show();
        }else{
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Email sent.");
                                AlertDialog.Builder builder = new AlertDialog.Builder(ResetPasswordActivity.this);
                                builder.setTitle("Resetting Password");
                                builder.setMessage("We've sent an email to your email address.\n" +
                                        "Please check your email and reset your password");
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }else{
                                Toast.makeText(ResetPasswordActivity.this, "Please check your email address",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}

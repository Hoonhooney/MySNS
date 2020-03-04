package com.example.mysns;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "TAG_sign up";

    private EditText editText_email;
    private EditText editText_password;
    private EditText editText_passwordCheck;
    private EditText editText_nickname;
    private RelativeLayout button_signup, button_checkNickname;
    private TextView textView_checkNickname;
    private String email = "", password = "", passwordCheck = "", nickname = "";
    private boolean nicknameCheck;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editText_email = findViewById(R.id.editText_signup_email);
        editText_password = findViewById(R.id.editText_signup_password);
        editText_passwordCheck = findViewById(R.id.editText_signup_passwordCheck);
        editText_nickname = findViewById(R.id.editText_nickname);
        textView_checkNickname = findViewById(R.id.textView_checkNickname);

        nicknameCheck = false;

        button_checkNickname = findViewById(R.id.button_checkNickname);
        button_checkNickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nickname = editText_nickname.getText().toString();

                db.collection("users").whereEqualTo("nickname", nickname)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                Log.d(TAG, "checking nickname success");
                                AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
                                builder.setTitle("Checking Nickname...");

                                if(!nickname.isEmpty() && queryDocumentSnapshots.getDocuments().isEmpty()){
                                    builder.setMessage("This nickname is available!");
                                    nicknameCheck = true;
                                    button_checkNickname.
                                            setBackground(ContextCompat.getDrawable(SignupActivity.this, R.drawable.btn_true));
                                    textView_checkNickname.setText("Valid");
                                    textView_checkNickname.
                                            setTextColor(ContextCompat.getColor(SignupActivity.this, R.color.colorTrue));
                                }else if(nickname.isEmpty()){
                                    builder.setMessage("Please Text your nickname!");
                                    nicknameCheck = false;
                                }else{
                                    builder.setMessage("This nickname is already being used.");
                                    nicknameCheck = false;
                                    button_checkNickname.
                                            setBackground(ContextCompat.getDrawable(SignupActivity.this, R.drawable.btn_false));
                                    textView_checkNickname.setText("Invalid");
                                    textView_checkNickname.
                                            setTextColor(ContextCompat.getColor(SignupActivity.this, R.color.colorFalse));
                                }

                                builder.setPositiveButton("OK", null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "checking nickname failed", e);
                            }
                        });
            }
        });

        button_signup = findViewById(R.id.button_signup);
        button_signup.setClickable(true);
        button_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = editText_email.getText().toString();
                password = editText_password.getText().toString();
                passwordCheck = editText_passwordCheck.getText().toString();
                signup();
            }
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void signup(){
        if(email.isEmpty())
            Toast.makeText(SignupActivity.this, "Please text your correct email address!",
                    Toast.LENGTH_SHORT).show();
        else if(!nicknameCheck)
            Toast.makeText(SignupActivity.this, "Please check your nickname!",
                    Toast.LENGTH_SHORT).show();
        else if(!password.equals(passwordCheck))
            Toast.makeText(SignupActivity.this, "Please check your password!",
                    Toast.LENGTH_SHORT).show();
        else if(password.length() < 6)
            Toast.makeText(SignupActivity.this, "Password should be at least 6 characters!",
                    Toast.LENGTH_SHORT).show();
        else {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();

                                Map<String, Object> map = new HashMap<>();
                                map.put("uid", user.getUid());
                                map.put("nickname", nickname);
                                map.put("email", email);

                                db.collection("users")
                                        .add(map)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Log.d(TAG, "adding user information to db : success");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "createUserWithEmail:fail", e);
                                            }
                                        });

                                AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
                                builder.setTitle("Sign up").setMessage("Sign up Completed!");
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mAuth.signOut();
                                        finish();
                                    }
                                });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(SignupActivity.this, "Authentication failed."+task.getException(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}

package com.example.mysns.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.mysns.Post;
import com.example.mysns.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EditPostActivity extends AppCompatActivity {
    private static final String TAG = "TAG_edit_post";

    MainActivity mainActivity = (MainActivity)MainActivity.activity;

    private ImageView button_cancel, button_edit;
    private ImageView imageView_postedImage;
    private EditText editText_description;

    private Post post;
    private String description;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        button_cancel = findViewById(R.id.button_edit_post_cancel);
        button_edit = findViewById(R.id.button_edit_post_edit);

        imageView_postedImage = findViewById(R.id.imageView_edit_post_postedImage);

        editText_description = findViewById(R.id.editText_edit_post_description);

        db = FirebaseFirestore.getInstance();

        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Bundle bundle = getIntent().getExtras();

        Uri postedImgUri = (Uri)bundle.get("ImageUri");

        if(postedImgUri != null){
            Glide.with(EditPostActivity.this)
                    .load(postedImgUri)
                    .into(imageView_postedImage);
        }

        String postId = bundle.getString("PostId");
        if(postId != null){
            db.collection("posts").document(postId)
                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    post = documentSnapshot.toObject(Post.class);

                    if(post != null){
                        Log.d(TAG, "getting post data : success : "+post.getPostId());
                        description = post.getDescription();
                        if(description != null)
                            editText_description.setText(description);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "getting post data : fail", e);
                }
            });
        }

        button_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPost();
            }
        });
    }

    private void editPost(){
        if(post != null && description != null){
            String updatedDescription = editText_description.getText().toString();

            final AlertDialog.Builder builder = new AlertDialog.Builder(EditPostActivity.this);
            builder.setTitle("Edit Post");
            if(description.equals(updatedDescription)){
                builder.setMessage("The text is same as it was!");
                builder.setPositiveButton("OK", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }else{
                Map<String, Object> editMap = new HashMap<>();
                editMap.put("description", updatedDescription);
                String editTime ="(updated at) " + new SimpleDateFormat("HH:mm dd/MM/YYYY").format(new Date());
                editMap.put("postedTime", editTime);
                db.collection("posts").document(post.getPostId())
                        .update(editMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "update post : success");

                            builder.setMessage("Update Succeed!");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mainActivity.finish();
                                    Intent intent = new Intent(EditPostActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }else{
                            Log.d(TAG, "update post : fail");
                            builder.setMessage("Update Failed!");
                            builder.setPositiveButton("OK", null);
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    }
                });
            }
        }
    }
}

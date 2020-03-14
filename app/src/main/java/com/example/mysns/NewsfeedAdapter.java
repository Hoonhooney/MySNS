package com.example.mysns;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mysns.Activities.EditPostActivity;
import com.example.mysns.Activities.EditProfileActivity;
import com.example.mysns.Activities.MainActivity;
import com.example.mysns.Activities.SignupActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewsfeedAdapter extends RecyclerView.Adapter<NewsfeedAdapter.MyViewHolder> {
    private static final String TAG = "TAG_newsfeed_adapter";

    MainActivity mainActivity = (MainActivity)MainActivity.activity;

    private Context mContext;
    private List<Post> postList;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseAuth mAuth;

    private String uid, postImgUri_string;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private CircleImageView imageView_userProfile;
        private TextView textView_userNickname;
        private ImageView imageView_postImage;
        private TextView textView_postedTime;
        private ImageView button_like;
        private ImageView button_comment;
        private ImageView button_more;
        private TextView textView_description;
        private View rootView;

        public MyViewHolder(View v) {
            super(v);
            Log.d(TAG, "Constructor_MyViewHolder");

            imageView_userProfile = v.findViewById(R.id.circleImageView_newsfeed_profile);
            textView_userNickname = v.findViewById(R.id.textView_newsfeed_nickname);
            imageView_postImage = v.findViewById(R.id.imageView_newsfeed_postedImage);
            textView_postedTime = v.findViewById(R.id.textView_newsfeed_uploadedTime);
            button_like = v.findViewById(R.id.button_newsfeed_like);
            button_comment = v.findViewById(R.id.button_newsfeed_comment);
            button_more = v.findViewById(R.id.button_post_more);
            textView_description = v.findViewById(R.id.textView_newsfeed_description);

            rootView = v;
        }
    }

    public NewsfeedAdapter(List<Post> newsfeed, Context context){
        Log.d(TAG, "Constructor");
        postList = newsfeed;
        mContext = context;
    }

    @NonNull
    @Override
    public NewsfeedAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_post, parent, false);
        Log.d(TAG, "onCreateViewHolder()");

        MyViewHolder vh = new MyViewHolder(v);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder()");

        final Post post = postList.get(position);

        final Uri[] postImgUriforIntent = new Uri[1];

        uid = post.getUserId();

//        getting a profile image from db
        db.collection("users").document(uid)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserInfo userInfo = documentSnapshot.toObject(UserInfo.class);
                if(userInfo != null){
                    Log.d(TAG, "getting uploader data : success");

                    String profileImgUri = userInfo.getProfileImgUri();
                    String profileNickname = userInfo.getNickname();
                    holder.textView_userNickname.setText(profileNickname);

                    if(profileImgUri != null && !profileImgUri.isEmpty()){
                        Uri profileUri = Uri.parse(profileImgUri);

                        StorageReference pathRef = storage.getReference().child("images/profile/"+profileUri.getLastPathSegment());

                        pathRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if(task.isSuccessful()){
                                    Log.d(TAG, "getting uploader profile image : success");

                                    Uri uploadImgUri = task.getResult();

                                    Glide.with(mContext)
                                            .load(uploadImgUri)
                                            .into(holder.imageView_userProfile);
                                }
                            }
                        });
                    }
                }
            }
        });

        postImgUri_string = post.getPostedImageUri();
        String time = post.getPostedTime();
        String description = post.getDescription();

        holder.textView_postedTime.setText(time);
        holder.textView_description.setText(description);

//        getting a post image from db
        Uri postImgUri = Uri.parse(postImgUri_string);
        StorageReference pathRef = storage.getReference().child("images/post/"+ postImgUri.getLastPathSegment());

        pathRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    Uri uploadImgUri = task.getResult();
                    postImgUriforIntent[0] = uploadImgUri;

                    if(uploadImgUri != null){
                        Glide.with(mContext)
                                .load(uploadImgUri)
                                .into(holder.imageView_postImage);
                    }
                }
            }
        });

//        setting 'more' button visible on current user's posts
        final String currentUserId = mAuth.getUid();
        if(uid.equals(currentUserId )){
            holder.button_more.setVisibility(View.VISIBLE);
            holder.button_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final CharSequence[] oItems = {"Edit", "Delete"};

                    AlertDialog.Builder oDialog = new AlertDialog.Builder(mContext,
                            android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);

                    oDialog.setItems(oItems, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            switch(which){
                                case 0:
                                    Intent intent = new Intent(mContext, EditPostActivity.class);
                                    intent.putExtra("ImageUri", postImgUriforIntent[0]);
                                    intent.putExtra("PostId", post.getPostId());
                                    mContext.startActivity(intent);
                                    break;
                                case 1:
                                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                    builder.setTitle("Delete Post");
                                    builder.setMessage("Are you sure you want to delete this post?");
                                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            deletePost(post);
                                        }
                                    }).setNegativeButton("No", null);
                                    AlertDialog aDialog = builder.create();
                                    aDialog.show();
                                    break;
                            }
                        }
                    })
                            .setCancelable(true)
                            .show();
                }
            });
        }else
            holder.button_more.setVisibility(View.INVISIBLE);

//        setting 'like' button
        final List<String> likingPeople = post.getLikeList();

        if(likingPeople.contains(currentUserId)){
            holder.button_like.setImageResource(R.drawable.ic_like_red_24dp);
        }else{
            holder.button_like.setImageResource(R.drawable.ic_like_black_24dp);
        }

        holder.button_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentUserId != null){
                    Map<String, Object> likeMap = new HashMap<>();

                    if(!likingPeople.contains(currentUserId)){
                        likingPeople.add(currentUserId);

                        likeMap.put("likeList", likingPeople);
                        likeMap.put("numLike", likingPeople.size());
                        db.collection("posts").document(post.getPostId())
                                .update(likeMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "adding like : success");
                                holder.button_like.setImageResource(R.drawable.ic_like_red_24dp);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "adding like : fail", e);
                            }
                        });
                    }else{
                        likingPeople.remove(currentUserId);

                        likeMap.put("likeList", likingPeople);
                        likeMap.put("numLike", likingPeople.size());
                        db.collection("posts").document(post.getPostId())
                                .update(likeMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "removing like : success");
                                holder.button_like.setImageResource(R.drawable.ic_like_black_24dp);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "removing like : fail", e);
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList == null ? 0 : postList.size();
    }

    public Post getPost(int position){
        return postList != null ? postList.get(position) : null;
    }

    public void addPost(Post post){
        postList.add(post);
        notifyItemInserted(postList.size()-1); // 데이터 갱신
    }

    private void deletePost(final Post postToBeDeleted){
        db.collection("posts").document(postToBeDeleted.getPostId())
                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "delete post : success : "+postToBeDeleted.getPostId());

                Uri imageToBeDeletedUri = Uri.parse(postToBeDeleted.getPostedImageUri());

                StorageReference pathRef = storage.getReference().child("images/post/"+imageToBeDeletedUri.getLastPathSegment());

                pathRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "delete post image : success");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "delete post image : fail", e);
                    }
                });

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Delete Post");
                builder.setMessage("The post is deleted!");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(mContext, MainActivity.class);
                        mContext.startActivity(intent);
                        mainActivity.finish();
                    }
                });
                AlertDialog aDialog = builder.create();
                aDialog.show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "delete post : fail", e);

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Delete Post");
                builder.setMessage("Failed to delete post!");
                builder.setPositiveButton("OK", null);
                AlertDialog aDialog = builder.create();
                aDialog.show();
            }
        });
    }
}

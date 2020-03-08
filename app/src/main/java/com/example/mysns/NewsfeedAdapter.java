package com.example.mysns;

import android.content.Context;
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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewsfeedAdapter extends RecyclerView.Adapter<NewsfeedAdapter.MyViewHolder> {
    private static final String TAG = "TAG_newsfeed_adapter";

    private Context mContext;
    private List<Post> postList;
    private FirebaseFirestore db;
    FirebaseStorage storage;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private CircleImageView imageView_userProfile;
        private TextView textView_userNickname;
        private ImageView imageView_postImage;
        private TextView textView_postedTime;
        private ImageView button_like;
        private ImageView button_comment;
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
        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder()");

        Post post = postList.get(position);

        String uid = post.getUserId();

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

        final String postImgUri_string = post.getPostedImageUri();
        String time = post.getPostedTime();
        String description = post.getDescription();

        holder.textView_postedTime.setText(time);
        holder.textView_description.setText(description);

//        getting a post image from db
        Uri postImgUri = Uri.parse(postImgUri_string);
        StorageReference pathRef = storage.getReference().child("images/post/"+postImgUri.getLastPathSegment());

        pathRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    Uri uploadImgUri = task.getResult();
                    Log.d(TAG, "getting post data : success"+uploadImgUri);

                    if(uploadImgUri != null){
                        Glide.with(mContext)
                                .load(uploadImgUri)
                                .into(holder.imageView_postImage);
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
}

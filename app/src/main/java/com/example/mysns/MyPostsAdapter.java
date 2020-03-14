package com.example.mysns;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;


public class MyPostsAdapter extends RecyclerView.Adapter<MyPostsAdapter.MyViewHolder> {
    private static final String TAG = "TAG_my_post_adapter";

    private Context mContext;
    private List<Post> postList;
    private FirebaseStorage storage;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private ImageView imageView_myPost;

        public MyViewHolder(View v) {
            super(v);
            Log.d(TAG, "Constructor_MyViewHolder");

            imageView_myPost = v.findViewById(R.id.imageView_my_post);
        }
    }

    public MyPostsAdapter(List<Post> posts, Context context){
        Log.d(TAG, "Constructor");
        postList = posts;
        mContext = context;
    }

    @NonNull
    @Override
    public MyPostsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_post, parent, false);
        Log.d(TAG, "onCreateViewHolder()");

        MyPostsAdapter.MyViewHolder vh = new MyPostsAdapter.MyViewHolder(v);
        storage = FirebaseStorage.getInstance();
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyPostsAdapter.MyViewHolder holder, int position) {

        final Post post = postList.get(position);

        String postedImageUri = post.getPostedImageUri();

        if(postedImageUri != null){
            Uri uri_postedImg = Uri.parse(postedImageUri);

            StorageReference pathRef = storage.getReference().child("images/post/"+uri_postedImg.getLastPathSegment());

            pathRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Log.d(TAG, "downloading post image uri : success");
                    Glide.with(mContext)
                            .load(uri)
                            .apply(new RequestOptions().centerCrop())
                            .into(holder.imageView_myPost);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "downloading post image uri : fail", e);
                }
            });
        }
    }

    public void addPost(Post post){
        postList.add(post);
        notifyItemInserted(postList.size()-1); // 데이터 갱신
    }

    public void clearList(){
        if(!postList.isEmpty())
            postList.clear();
    }

    @Override
    public int getItemCount() {
        return postList == null ? 0 : postList.size();
    }
}

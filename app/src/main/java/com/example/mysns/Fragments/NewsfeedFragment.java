package com.example.mysns.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mysns.NewsfeedAdapter;
import com.example.mysns.Post;
import com.example.mysns.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.core.OrderBy;

import java.util.ArrayList;
import java.util.List;


public class NewsfeedFragment extends Fragment {
    private static final String TAG = "TAG_newsfeed";
    private Context context;

    private View rootView;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private List<Post> postList;

    private FirebaseFirestore db;

    public NewsfeedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context c) {
        super.onAttach(context);
        context = c;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        layoutManager = new LinearLayoutManager(context);
        postList = new ArrayList<>();
        mAdapter = new NewsfeedAdapter(postList, context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_newsfeed, container, false);

//        setting post Data in RecyclerView
        readData();

        return rootView;
    }

    private void readData(){
        db.collection("posts").orderBy("createdAt", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot document : queryDocumentSnapshots.getDocuments()){
                            Post post = document.toObject(Post.class);
                            Log.d(TAG, post.getDescription());
                            ((NewsfeedAdapter)mAdapter).addPost(post);
                        }

                        recyclerView = rootView.findViewById(R.id.recyclerView_newsfeed);
                        recyclerView.setHasFixedSize(true);
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setAdapter(mAdapter);

                    }
                });
    }
}

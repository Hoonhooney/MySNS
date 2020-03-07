package com.example.mysns.Fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mysns.LoginActivity;
import com.example.mysns.R;
import com.example.mysns.UserInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPageFragment extends Fragment {
    private static final String TAG = "TAG_mypage";

    private Context context;
    private View rootView;

    private CircleImageView imageView_profile;
    private TextView textView_nickname;
    private TextView textView_birthday;
    private TextView textView_email;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;

    private UserInfo userInfo;

    private String nickname, email, birthday, profileImgUri;
    private boolean haveProfileImg = false, haveInit = false;
    private Uri uploadImgUri;

    private ProgressDialog progressDialog;

    public MyPageFragment() {
        // Required empty public constructor
    }
    @Override

    public void onAttach(Context c) {
        super.onAttach(context);
        context = c;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_my_page, container, false);

        imageView_profile = rootView.findViewById(R.id.circleImageView_mypage_profile);
        textView_nickname = rootView.findViewById(R.id.textView_mypage_nickname);
        textView_birthday = rootView.findViewById(R.id.textView_mypage_birthday);
        textView_email = rootView.findViewById(R.id.textView_mypage_email);

        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Please wait...");

        if(!haveInit)
            getMyInfo();

        if(haveInit && userInfo != null)
            setViews();

        return rootView;
    }

    private void goActivity(Class c){
        Intent intent = new Intent(context, c);
        startActivity(intent);
//        context.finish();
    }

    private void getMyInfo(){
        progressDialog.show();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //        getting user information
        user = mAuth.getCurrentUser();

        if(user == null){
            Log.d(TAG, "user logged out");
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
            db.collection("users").document(user.getUid())
                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    userInfo = documentSnapshot.toObject(UserInfo.class);

                    if(userInfo != null){
                        nickname = userInfo.getNickname();
                        email = userInfo.getEmail();
                        birthday = userInfo.getBirthday();
                        profileImgUri = userInfo.getProfileImgUri();

                        if(profileImgUri != null && !profileImgUri.isEmpty()){
                            Uri profileUri = Uri.parse(profileImgUri);

                            FirebaseStorage storage = FirebaseStorage.getInstance();
                            StorageReference pathRef = storage.getReference().child("images/profile/"+profileUri.getLastPathSegment());

                            pathRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if(task.isSuccessful()){
                                        Log.d(TAG, "downloading user profile image : success");
                                        uploadImgUri = task.getResult();
                                        haveProfileImg = true;

                                        Glide.with(context)
                                                .load(uploadImgUri)
                                                .into(imageView_profile);

                                        progressDialog.dismiss();

                                    }else{
                                        Log.d(TAG, "downloading user profile image : fail");
                                        Toast.makeText(context,
                                                "downloading user profile image : fail", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }

                        setViews();

                    }
                    progressDialog.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "getting userInfo from db : fail", e);
                    Toast.makeText(context, "getting userInfo from db is failed",
                            Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            });

            haveInit = true;
        }
    }

    private void setViews(){
        textView_nickname.setText(nickname);
        textView_birthday.setText(birthday);
        textView_email.setText(email);

//                        profile image upload
        if(haveProfileImg) {
            Glide.with(context)
                    .load(uploadImgUri)
                    .into(imageView_profile);
        }
    }
}

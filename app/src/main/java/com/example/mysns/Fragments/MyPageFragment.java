package com.example.mysns;

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

    private Context context = getActivity();

    private CircleImageView imageView_profile;
    private TextView textView_nickname;
    private TextView textView_birthday;
    private TextView textView_email;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private UserInfo userInfo;
    private ProgressDialog progressDialog;

    private OnFragmentInteractionListener mListener;

    public MyPageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_page, container, false);

        imageView_profile = view.findViewById(R.id.circleImageView_mypage_profile);
        textView_nickname = view.findViewById(R.id.textView_mypage_nickname);
        textView_birthday = view.findViewById(R.id.textView_mypage_birthday);
        textView_email = view.findViewById(R.id.textView_mypage_email);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //        getting user information
        FirebaseUser user = mAuth.getCurrentUser();

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
            progressDialog = new ProgressDialog(context);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("Please wait...");
            progressDialog.show();

            db.collection("users").document(user.getUid())
                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Log.d(TAG, "getting userInfo from db : success");
                    userInfo = documentSnapshot.toObject(UserInfo.class);

                    if(userInfo != null){
                        textView_nickname.setText(userInfo.getNickname());
                        textView_birthday.setText(userInfo.getBirthday());
                        textView_email.setText(userInfo.getEmail());

//                        profile image upload
                        String profileImgUri = userInfo.getProfileImgUri();
                        if(profileImgUri != null && !profileImgUri.isEmpty()){
                            Uri profileUri = Uri.parse(profileImgUri);

                            FirebaseStorage storage = FirebaseStorage.getInstance();
                            StorageReference pathRef = storage.getReference().child("images/profile/"+profileUri.getLastPathSegment());

                            pathRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if(task.isSuccessful()){
                                        Glide.with(context)
                                                .load(task.getResult())
                                                .into(imageView_profile);

                                        progressDialog.dismiss();
                                    }else{
                                        Log.d(TAG, "downloading user profile image : fail");
                                        Toast.makeText(context,
                                                "downloading user profile image : fail", Toast.LENGTH_LONG).show();

                                        progressDialog.dismiss();
                                    }
                                }
                            });
                        }
                        progressDialog.dismiss();
                    }
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
        }

        return inflater.inflate(R.layout.fragment_my_page, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    private void goActivity(Class c){
        Intent intent = new Intent(context, c);
        startActivity(intent);
//        context.finish();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

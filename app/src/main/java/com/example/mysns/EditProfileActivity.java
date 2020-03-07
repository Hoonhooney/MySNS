package com.example.mysns;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {
    private static final String TAG = "TAG_edit_profile";
    private static final int PICK_FROM_ALBUM = 1;
    private static final int PICK_FROM_CAMERA = 2;

    MainActivity mainActivity = (MainActivity)MainActivity.activity;

    private EditText editText_nickname;
    private EditText editText_birthday;
    private RelativeLayout button_editFinish, button_checkNickname, button_browse, button_cam;
    private TextView textView_checkNickname, textView_profileImg;
    private CircleImageView profileImageView;

    private String nickname = "", birthday = "", profileImgUri = "", nickname_now, birthday_now, profileImgUri_now_string;
    private Uri profileImgUri_now;
    private boolean nicknameCheck;

    private Boolean isPermission = true;

    private File tempFile;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Bundle bundle = getIntent().getExtras();
        nickname_now = (String)bundle.get("nickname");
        birthday_now = (String)bundle.get("birthday");
        profileImgUri_now = (Uri)bundle.get("imageUri");
        profileImgUri_now_string = (String)bundle.get("imageUriString");

        editText_nickname = findViewById(R.id.editText_edit_nickname);
        editText_nickname.setText(nickname_now);
        textView_checkNickname = findViewById(R.id.textView_edit_checkNickname);

        editText_birthday = findViewById(R.id.editText_edit_birthday);
        editText_birthday.setText(birthday_now);

        textView_profileImg = findViewById(R.id.textView_edit_profileImage);
        textView_profileImg.setText(profileImgUri_now_string);

        profileImageView = findViewById(R.id.circleImageView_edit_profile);
        if(profileImgUri_now != null){
            Glide.with(EditProfileActivity.this)
                    .load(profileImgUri_now)
                    .into(profileImageView);
        }

        nicknameCheck = false;

        progressDialog = new ProgressDialog(EditProfileActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Please wait...");

        tedPermission();

        button_checkNickname = findViewById(R.id.button_edit_checkNickname);
        button_checkNickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkNickname();
            }
        });

        button_browse = findViewById(R.id.button_edit_profileImg_browse);
        button_browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPermission) goToAlbum();
                else Toast.makeText(EditProfileActivity.this, "not granted", Toast.LENGTH_LONG).show();
            }
        });

        button_cam = findViewById(R.id.button_edit_profileImg_cam);
        button_cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPermission)  takePhoto();
                else Toast.makeText(EditProfileActivity.this, "not granted", Toast.LENGTH_LONG).show();
            }
        });

        button_editFinish = findViewById(R.id.button_edit_finish);
        button_editFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editProfile();
            }
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void checkNickname(){
        nickname = editText_nickname.getText().toString();

        if(!nickname.equals(nickname_now)){
            progressDialog.show();

            db.collection("users").whereEqualTo("nickname", nickname)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            Log.d(TAG, "checking nickname success");
                            progressDialog.dismiss();
                            AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                            builder.setTitle("Checking Nickname...");

                            if(!nickname.isEmpty() && queryDocumentSnapshots.getDocuments().isEmpty()){
                                builder.setMessage("This nickname is available!");
                                nicknameCheck = true;
                                button_checkNickname.
                                        setBackground(ContextCompat.getDrawable(EditProfileActivity.this, R.drawable.btn_true));
                                textView_checkNickname.setText("Valid");
                                textView_checkNickname.
                                        setTextColor(ContextCompat.getColor(EditProfileActivity.this, R.color.colorTrue));
                            }else if(nickname.isEmpty()){
                                builder.setMessage("Please Text your nickname!");
                                nicknameCheck = false;
                            }else{
                                builder.setMessage("This nickname is already being used.");
                                nicknameCheck = false;
                                button_checkNickname.
                                        setBackground(ContextCompat.getDrawable(EditProfileActivity.this, R.drawable.btn_false));
                                textView_checkNickname.setText("Invalid");
                                textView_checkNickname.
                                        setTextColor(ContextCompat.getColor(EditProfileActivity.this, R.color.colorFalse));
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
                            progressDialog.dismiss();
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();

            if(tempFile != null) {
                if (tempFile.exists()) {
                    if (tempFile.delete()) {
                        Log.e(TAG, tempFile.getAbsolutePath() + " 삭제 성공");
                        tempFile = null;
                    }
                }
            }

            return;
        }

        switch (requestCode) {
            case PICK_FROM_ALBUM: {

                Uri photoUri = data.getData();
                profileImgUri = photoUri.toString();
                textView_profileImg.setText(profileImgUri);
                Log.d(TAG, "PICK_FROM_ALBUM photoUri : " + photoUri);

                cropImage(photoUri);

                break;
            }
            case PICK_FROM_CAMERA: {

                Uri photoUri = Uri.fromFile(tempFile);
                profileImgUri = photoUri.toString();
                textView_profileImg.setText(profileImgUri);
                Log.d(TAG, "takePhoto photoUri : " + photoUri);

                cropImage(photoUri);

                break;
            }
            case Crop.REQUEST_CROP: {
                //File cropFile = new File(Crop.getOutput(data).getPath());
                setImage();
            }
        }
    }

    private void goToAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            tempFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            e.printStackTrace();
        }
        if (tempFile != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {

                Uri photoUri = FileProvider.getUriForFile(this,
                        "com.example.mysns.provider", tempFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, PICK_FROM_CAMERA);

            } else {

                Uri photoUri = Uri.fromFile(tempFile);
                Log.d(TAG, "takePhoto photoUri : " + photoUri);

                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, PICK_FROM_CAMERA);

            }
        }
    }

    private void cropImage(Uri photoUri) {

        Log.d(TAG, "tempFile : " + tempFile);

        if(tempFile == null) {
            try {
                tempFile = createImageFile();
            } catch (IOException e) {
                Toast.makeText(this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                finish();
                e.printStackTrace();
            }
        }

        //크롭 후 저장할 Uri
        Uri savingUri = Uri.fromFile(tempFile);

        Crop.of(photoUri, savingUri).asSquare().start(this);
    }

    private File createImageFile() throws IOException {

        // 이미지 파일 이름 ( blackJin_{시간}_ )
        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String imageFileName = "blackJin_" + timeStamp + "_";

        // 이미지가 저장될 폴더 이름 ( blackJin )
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/blackJin/");
        if (!storageDir.exists()) storageDir.mkdirs();

        // 빈 파일 생성
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        Log.d(TAG, "createImageFile : " + image.getAbsolutePath());

        return image;
    }

    private void setImage(){
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap originalBm = BitmapFactory.decodeFile(tempFile.getAbsolutePath(), options);
        Log.d(TAG, "setImage : " + tempFile.getAbsolutePath());

        profileImageView.setImageBitmap(originalBm);

        tempFile = null;
    }

    private void profileImgUpload(){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        if(profileImgUri_now_string != null){
            //Delete existing file
            Uri file_delete = Uri.parse(profileImgUri_now_string);
            StorageReference profileRef_delete = storageRef.child("images/profile/"+file_delete.getLastPathSegment());

            profileRef_delete.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "delete existing profile image : success");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "delete existing profile image : fail", e);
                }
            });
        }

        //Upload new file
        Uri file = Uri.parse(profileImgUri);
        StorageReference profileRef = storageRef.child("images/profile/"+file.getLastPathSegment());

        UploadTask uploadTask = profileRef.putFile(file);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.w(TAG, "image upload failed : ",exception);
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.w(TAG, "image upload success");
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
            }
        });

    }

    private void editProfile(){

        nickname = editText_nickname.getText().toString();
        birthday = editText_birthday.getText().toString();

        progressDialog.show();

//            update database

        Map<String, Object> edits = new HashMap<>();
        if(nicknameCheck)
            edits.put("nickname", nickname);
        if(!birthday.isEmpty() && !birthday.equals(birthday_now))
            edits.put("birthday", birthday);
        if(!profileImgUri.isEmpty() && !profileImgUri.equals(profileImgUri_now_string))
            edits.put("profileImgUri", profileImgUri);

        FirebaseUser user = mAuth.getCurrentUser();

        if(user != null && !edits.isEmpty()){
            db.collection("users").document(user.getUid())
                    .update(edits)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Log.d(TAG, "edit db : success");

                                if(!profileImgUri.isEmpty()){
                                    profileImgUpload();
                                }
                                progressDialog.dismiss();

                                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                                builder.setTitle("Edit Profile").setMessage("Update Completed!");
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mainActivity.finish();
                                        Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }else{
                                Log.d(TAG, "edit db : fail");
                                progressDialog.dismiss();

                                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                                builder.setTitle("Edit Profile").setMessage("Update failed : failed to add user information to db");
                                builder.setPositiveButton("OK", null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }
                    });
        }
    }

    /**
     *  권한 설정
     */
    private void tedPermission() {

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // 권한 요청 성공
                isPermission = true;

            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                // 권한 요청 실패
                isPermission = false;

            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();
    }
}

package com.example.mysns;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "TAG_sign up";
    private static final int PICK_FROM_ALBUM = 1;
    private static final int PICK_FROM_CAMERA = 2;

    private EditText editText_email;
    private EditText editText_password;
    private EditText editText_passwordCheck;
    private EditText editText_nickname;
    private EditText editText_birthday;
    private RelativeLayout button_signup, button_checkNickname, button_browse, button_cam;
    private TextView textView_checkNickname, textView_profileImg;
    private CircleImageView profileImageView;
    private String email = "", password = "", passwordCheck = "", nickname = "", birthday = "", profileImgUrl = "";
    private boolean nicknameCheck;

    private Boolean isPermission = true;

    private File tempFile;

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
        editText_birthday = findViewById(R.id.editText_birthday);
        textView_profileImg = findViewById(R.id.textView_profileImage);

        profileImageView = findViewById(R.id.circleImageView_profile);

        nicknameCheck = false;

        tedPermission();

        button_checkNickname = findViewById(R.id.button_checkNickname);
        button_checkNickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkNickname();
            }
        });

        button_browse = findViewById(R.id.button_profileImg_browse);
        button_browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPermission) goToAlbum();
                else Toast.makeText(SignupActivity.this, "not granted", Toast.LENGTH_LONG).show();
            }
        });

        button_cam = findViewById(R.id.button_profileImg_cam);
        button_cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPermission)  takePhoto();
                else Toast.makeText(SignupActivity.this, "not granted", Toast.LENGTH_LONG).show();
            }
        });

        button_signup = findViewById(R.id.button_signup);
        button_signup.setClickable(true);
        button_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void checkNickname(){
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
                profileImgUrl = photoUri.toString();
                textView_profileImg.setText(profileImgUrl);
                Log.d(TAG, "PICK_FROM_ALBUM photoUri : " + photoUri);

                cropImage(photoUri);

                break;
            }
            case PICK_FROM_CAMERA: {

                Uri photoUri = Uri.fromFile(tempFile);
                profileImgUrl = photoUri.toString();
                textView_profileImg.setText(profileImgUrl);
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

    private void signup(){

        email = editText_email.getText().toString();
        password = editText_password.getText().toString();
        passwordCheck = editText_passwordCheck.getText().toString();
        birthday = editText_birthday.getText().toString();

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
        else if(birthday.length() != 6)
            Toast.makeText(SignupActivity.this, "Please text your birthday correctly!\n(6 digits like 'YYMMDD')",
                    Toast.LENGTH_LONG).show();
        else {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();

                                //init user profile
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(nickname)
                                        .build();
                                user.updateProfile(profileUpdates);

                                String uid = user.getUid();
                                final UserInfo userInfo = new UserInfo(uid, email, nickname, birthday, profileImgUrl);

                                db.collection("users").document(uid)
                                        .set(userInfo)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "adding user information to db : success");

                                                if(!profileImgUrl.isEmpty()){
                                                    profileImgUpload();
                                                }

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
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "adding user information to db : fail", e);

                                                AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
                                                builder.setTitle("Sign up").setMessage("Sign up failed : failed to add user information to db");
                                                builder.setPositiveButton("OK", null);
                                                AlertDialog dialog = builder.create();
                                                dialog.show();
                                            }
                                        });

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

    private void profileImgUpload(){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        Uri file = Uri.parse(profileImgUrl);
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

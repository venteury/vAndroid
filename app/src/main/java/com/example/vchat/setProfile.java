package com.example.vchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class setProfile extends AppCompatActivity {

    private CardView mgetuserimage;
    private ImageView mgetuserimageinimageview;
    public static int PICK_IMAGE = 123;
    private Uri imagepath;
    private EditText mgetusername;
    private android.widget.Button msaveprofile;

    private FirebaseAuth firebaseAuth;

    private String name;
    private FirebaseStorage firebaseStorage;
    private FirebaseStorage storageReference;
    private String imageuriacesstoken;
    private FirebaseFirestore firebaseFirestore;
    ProgressBar mprogressbarofsetprofile;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance();

        mgetusername = findViewById(R.id.getusername);
        mgetuserimage = findViewById(R.id.getuserimage);
        mgetuserimageinimageview = findViewById(R.id.getuserimageinimageview);
        msaveprofile = findViewById(R.id.saveprofile);
        mprogressbarofsetprofile = findViewById(R.id.progressbarofsaveprofile);

        mgetuserimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(i,PICK_IMAGE);
            }

        });


        msaveprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                name = mgetusername.getText().toString();
                if(name.isEmpty())
                {
                    Toast.makeText(setProfile.this, "Enter Valid Name", Toast.LENGTH_SHORT).show();
                }
                else if(imagepath == null)
                {
                    Toast.makeText(setProfile.this, "Image is Empty", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mprogressbarofsetprofile.setVisibility(View.VISIBLE);
                    sendDataForNewUser();
                    mprogressbarofsetprofile.setVisibility(View.INVISIBLE );
                    Intent i = new Intent(setProfile.this,chatActivity.class);
                    startActivity(i);
                    finish();
                }

            }

        });


    }





    private void sendDataForNewUser()
    {
        sendDataToRealTimeDatabase();
    }





    private void sendDataToRealTimeDatabase()
    {
        name = mgetusername.getText().toString().trim();
        FirebaseDatabase firebaseDatabase =  FirebaseDatabase.getInstance();
        DatabaseReference databaseReference =  firebaseDatabase.getReference(firebaseAuth.getUid());
        userprofile  muserprofile = new userprofile(name,firebaseAuth.getUid());
        databaseReference.setValue(muserprofile);
        Toast.makeText(getApplicationContext(), "User Added Sucessfully", Toast.LENGTH_SHORT).show();

        sendImageToStorage();



    }






    private void sendImageToStorage()
    {
        StorageReference imageref = storageReference.getReference().child("Images").child(firebaseAuth.getUid()).child("Profile Pic");

        // Image Compression Code

        Bitmap bitmap = null;

        try
        {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),imagepath);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,25,byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        //Putting Image to Storage

        UploadTask uploadTask = imageref.putBytes(data);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imageref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                    @Override
                    public void onSuccess(Uri uri) {
                        imageuriacesstoken = uri.toString();
                        Toast.makeText(getApplicationContext(), "URI get success", Toast.LENGTH_SHORT).show();
                        sendDataTocloudFirestore();
                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "URI get Failed", Toast.LENGTH_SHORT).show();
                    }
                });

            Toast.makeText(getApplicationContext(),"Image Uploaded Successfully!",Toast.LENGTH_SHORT).show();

            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Image not Uploaded",Toast.LENGTH_SHORT).show();
            }
        });

    }





    private void sendDataTocloudFirestore() {


        DocumentReference documentReference = firebaseFirestore.collection("users").document(firebaseAuth.getUid());
        Map<String,Object> userData = new HashMap<>();
        userData.put("name",name);
        userData.put("image",imageuriacesstoken);
        userData.put("uid",firebaseAuth.getUid());
        userData.put("status","online");

        documentReference.set(userData).addOnSuccessListener(new OnSuccessListener<Void>() {

            @Override
            public void  onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(),"Data Transfer on Cloud is Successful!",Toast.LENGTH_SHORT).show();
            }

        });

    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            imagepath = data.getData();
            mgetuserimageinimageview.setImageURI(imagepath);
        }


        super.onActivityResult(requestCode, resultCode, data);
    }
}

package com.example.mianshahbazidrees.uber.Customer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;


import com.bumptech.glide.Glide;
import com.example.mianshahbazidrees.uber.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class CustomerSettingActivity extends AppCompatActivity  implements View.OnClickListener{


    private EditText mNameField,mPhoneField;
    private Button mConfrim,mBack;
    private ImageView mProfileImageView;


    private FirebaseAuth mAuth;
    private DatabaseReference mCustomerDatabase;

    String userId="";

    String mName="";
    String mPhone="";
    String mProfileImageUrl="";

    private  Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_setting);


        mAuth=FirebaseAuth.getInstance();

       userId= mAuth.getCurrentUser().getUid();

        mCustomerDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId);


       getUserInformation();


        mNameField=findViewById(R.id.name);
        mPhoneField=findViewById(R.id.phone);

        mProfileImageView=findViewById(R.id.profileImageView);


        mBack=findViewById(R.id.back);
        mConfrim=findViewById(R.id.confirm);


        mBack.setOnClickListener(this);
        mConfrim.setOnClickListener(this);
        mProfileImageView.setOnClickListener(this);



    }




    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.profileImageView:

                Intent intent=new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,1);

                break;
            case R.id.confirm:
                saveUserInformation();
                break;
            case  R.id.back:
                finish();
                break;

        }
    }


    private  void  getUserInformation()
    {


        mCustomerDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0)
                {
                    Map<String,Object> map= (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name")!=null)
                    {
                        mName=map.get("name").toString();
                        mNameField.setText(mName);
                    }
                    if (map.get("phone")!=null)
                    {
                        mPhone=map.get("phone").toString();
                        mPhoneField.setText(mPhone);
                    }
                    if (map.get("profileImageUrl")!=null)
                    {
                        mProfileImageUrl=map.get("profileImageUrl").toString();


                        // now set image url  into image view throgh glide liberary
                       // Glide
                        Glide.with(getApplicationContext())
                                .load(mProfileImageUrl)
                                .into(mProfileImageView);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private  void  saveUserInformation()

    {

        mName=mNameField.getText().toString();
        mPhone=mPhoneField.getText().toString();

        HashMap map=new HashMap();

        map.put("name",mName);
        map.put("phone",mPhone);

        mCustomerDatabase.updateChildren(map);

        if (resultUri!=null)
        {
                    StorageReference filePath= FirebaseStorage.getInstance().getReference().child("profile_images").child(userId);

                    // now converting  url path String into Image
                    Bitmap bitmap=null;
                    try {
                        bitmap= MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(),resultUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // now  write image
                    ByteArrayOutputStream baos=new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG,20,baos);

                    //now convert image into byte array

                    byte[] data=baos.toByteArray();

                    // upload task is used to upload the image into  firebase storage
                    UploadTask uploadTask=filePath.putBytes(data);

                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            finish();

                        }
                    });

                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Uri downloadUrl=taskSnapshot.getDownloadUrl();

                            HashMap newImage=new HashMap();

                            newImage.put("profileImageUrl",downloadUrl.toString());

                            mCustomerDatabase.updateChildren(newImage);
                            finish();

                        }
                    });


        }
        else
        {
        finish();
        }








    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1 && resultCode==RESULT_OK)
        {
            final  Uri imageUri=data.getData();

            resultUri=imageUri;
            mProfileImageView.setImageURI(resultUri);


        }
    }
}

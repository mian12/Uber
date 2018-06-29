package com.example.mianshahbazidrees.uber.Driver;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mianshahbazidrees.uber.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverLoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEamil,mPassword;
    private Button mLogin,mRegistration;

    private FirebaseAuth mAuth;
    private  FirebaseAuth.AuthStateListener mFirebaseAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);




// its used for only to handle authentication for login users
        mAuth=FirebaseAuth.getInstance();

  // its used for litening the FirebaseAuth which  worked  automaticaly  to check the current state of the user

        mFirebaseAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
                if (user!=null)                {
                    Intent intent=new Intent(DriverLoginActivity.this, DriverMapActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }

            }


        };





        mEamil=findViewById(R.id.email);
        mPassword=findViewById(R.id.password);


        mLogin=findViewById(R.id.login);
        mRegistration=findViewById(R.id.registration);




        mLogin.setOnClickListener(this);
        mRegistration.setOnClickListener(this);




    }

    @Override
    public void onClick(View view) {

        String email=null;
        String password=null;
        switch (view.getId())
        {
            case R.id.login:
                 email=mEamil.getText().toString();
                 password=mPassword.getText().toString();

                mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (!task.isSuccessful())
                        {
                            Toast.makeText(DriverLoginActivity.this, "Sign in Error :(", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                break;



            case  R.id.registration:

                 email=mEamil.getText().toString();
                 password=mPassword.getText().toString();

                if (TextUtils.isEmpty(email)   )
                {
                    Toast.makeText(getApplicationContext()," enter email",Toast.LENGTH_SHORT).show();
                }

                else if(TextUtils.isEmpty(password))
                {
                    Toast.makeText(getApplicationContext()," enter password",Toast.LENGTH_SHORT).show();

                }

                else {

                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(DriverLoginActivity.this, "Sign up Error", Toast.LENGTH_SHORT).show();
                            } else {
                                // Its a newly genereted  id of the current login user
                                String user_id = mAuth.getCurrentUser().getUid();

                                DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(user_id);
                                current_user_db.setValue(true);
                            }

                        }
                    });

                }
                break;
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mFirebaseAuthListener);
    }
    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mFirebaseAuthListener);
    }


}

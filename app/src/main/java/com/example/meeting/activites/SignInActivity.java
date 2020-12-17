package com.example.meeting.activites;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.meeting.R;
import com.example.meeting.utilities.Constants;
import com.example.meeting.utilities.PreferenceManager;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class SignInActivity extends AppCompatActivity {
    private EditText inputEmail ,inputPassword;
    private MaterialButton buttonSignIn;
    private ProgressBar signInProgressBar;
    private PreferenceManager preferenceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        inputEmail=findViewById(R.id.signInEmail);
        inputPassword=findViewById(R.id.signInPassword);
        buttonSignIn=findViewById(R.id.signinbutton);
        signInProgressBar=findViewById(R.id.signProgressBar);
        preferenceManager=new PreferenceManager(getApplicationContext());

        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent=new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }

         }

    public void SignUpTextBtn(View view) {
        Intent home =new Intent(SignInActivity.this, SignUpActivity.class);
        startActivity(home);
    }

    public void SignInBtn(View view) {
        if (inputEmail.getText().toString().trim().isEmpty()){
            Toast.makeText(SignInActivity.this, "Enter  Email", Toast.LENGTH_SHORT).show();
        }else if(!Patterns.EMAIL_ADDRESS.matcher(inputEmail.getText().toString()).matches()){

            Toast.makeText(SignInActivity.this,"Enter Valid Email",Toast.LENGTH_LONG).show();

        }else if  (inputPassword.getText().toString().trim().isEmpty()){
            Toast.makeText(SignInActivity.this, "Enter  Password", Toast.LENGTH_SHORT).show();
        }else {
            signIn();
        }
    }

    private void signIn() {
        buttonSignIn.setVisibility(View.INVISIBLE);
        signInProgressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL,inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD,inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                if (task.isSuccessful()&&task.getResult().getDocuments().size() > 0){
                    DocumentSnapshot documentSnapshot=task.getResult().getDocuments().get(0);
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                    preferenceManager.putString(Constants.KEY_USER_ID,documentSnapshot.getId());
                    preferenceManager.putString(Constants.KEY_FIRST_NAME,documentSnapshot.getString(Constants.KEY_FIRST_NAME));
                    preferenceManager.putString(Constants.KEY_LAST_NAME,documentSnapshot.getString(Constants.KEY_LAST_NAME));
                    preferenceManager.putString(Constants.KEY_EMAIL,documentSnapshot.getString(Constants.KEY_EMAIL));

                    Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                }else {
                    signInProgressBar.setVisibility(View.INVISIBLE);
                    buttonSignIn.setVisibility(View.VISIBLE);
                    Toast.makeText(SignInActivity.this, "Unable to sign in", Toast.LENGTH_SHORT).show();
                }
                });


    }
}
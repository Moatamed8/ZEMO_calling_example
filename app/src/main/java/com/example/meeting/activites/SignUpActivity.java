package com.example.meeting.activites;

import androidx.annotation.NonNull;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {
    private EditText inputFirstName,inputLastName,inputEmail,inputPassword,inputConfirmPassword;
    private MaterialButton buttonSignUp;
    private ProgressBar signUpPrgressBar;
    private PreferenceManager preferenceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        preferenceManager=new PreferenceManager(getApplicationContext());

        inputFirstName=findViewById(R.id.inputfirstname);
        inputLastName=findViewById(R.id.inputlastname);
        inputEmail=findViewById(R.id.inputemail);
        inputPassword=findViewById(R.id.inputpassword);
        inputConfirmPassword=findViewById(R.id.inputconfirmPassword);
        buttonSignUp=findViewById(R.id.signupbutton);
        signUpPrgressBar=findViewById(R.id.signUpPrgressBar);
    }

    public void ImageClick(View view) {
        onBackPressed();
    }

    public void SignTextbtn(View view) {
        Intent home=new Intent(SignUpActivity.this, SignInActivity.class);
        startActivity(home);
    }

    public void SignUpBtn(View view) {
        if(inputFirstName.getText().toString().trim().isEmpty()){
            Toast.makeText(SignUpActivity.this,"Enter First Name",Toast.LENGTH_LONG).show();
        }else if (inputLastName.getText().toString().trim().isEmpty()){
            Toast.makeText(SignUpActivity.this,"Enter Last Name",Toast.LENGTH_LONG).show();
        }else if(inputEmail.getText().toString().trim().isEmpty()){
            Toast.makeText(SignUpActivity.this,"Enter Email",Toast.LENGTH_LONG).show();
        }else if(!Patterns.EMAIL_ADDRESS.matcher(inputEmail.getText().toString()).matches()){

            Toast.makeText(SignUpActivity.this,"Enter Valid Email",Toast.LENGTH_LONG).show();

        }else if(inputPassword.getText().toString().trim().isEmpty()){
            Toast.makeText(SignUpActivity.this,"Enter Password",Toast.LENGTH_LONG).show();
        }else if(inputConfirmPassword.getText().toString().trim().isEmpty()){
            Toast.makeText(SignUpActivity.this,"Confirm Your Password",Toast.LENGTH_LONG).show();
        }else if (!inputPassword.getText().toString().equals(inputConfirmPassword.getText().toString())){
            Toast.makeText(SignUpActivity.this,"Password & Confirm Your Password must be the same",Toast.LENGTH_LONG).show();

        }else {
            SignUp();
        }

    }

    private void SignUp() {
        buttonSignUp.setVisibility(View.INVISIBLE);
        signUpPrgressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore database =FirebaseFirestore.getInstance();
        HashMap<String,Object>user=new HashMap<>();
        user.put(Constants.KEY_FIRST_NAME,inputFirstName.getText().toString());
        user.put(Constants.KEY_LAST_NAME,inputLastName.getText().toString());
        user.put(Constants.KEY_EMAIL,inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD,inputPassword.getText().toString());

        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                    preferenceManager.putString(Constants.KEY_USER_ID,documentReference.getId());

                    preferenceManager.putString(Constants.KEY_FIRST_NAME,inputFirstName.getText().toString());
                    preferenceManager.putString(Constants.KEY_LAST_NAME,inputLastName.getText().toString());
                    preferenceManager.putString(Constants.KEY_EMAIL,inputEmail.getText().toString());
                    Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                   signUpPrgressBar.setVisibility(View.INVISIBLE);
                   buttonSignUp.setVisibility(View.VISIBLE);
                   Toast.makeText(SignUpActivity.this,"Error"+ e.getMessage(),Toast.LENGTH_LONG).show();
                });

    }
}

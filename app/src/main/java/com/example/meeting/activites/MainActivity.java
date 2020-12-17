package com.example.meeting.activites;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.meeting.R;
import com.example.meeting.adapters.UsersAdapter;
import com.example.meeting.listeners.UsersListeners;
import com.example.meeting.models.User;
import com.example.meeting.utilities.Constants;
import com.example.meeting.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity implements UsersListeners {
    private PreferenceManager preferenceManager;
    private List<User>users;
    private UsersAdapter usersAdapter;
    private TextView textErrorMessage;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView imageConfrence;

    private int REQUEST_CODE_BATTERY_OPTIMIZATION = 1;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferenceManager=new PreferenceManager(getApplicationContext());
        imageConfrence=findViewById(R.id.imageConference);
//hna msln
        TextView textTitle=findViewById(R.id.textTitle);
        textTitle.setText(String.format(
                "%s %s",
                preferenceManager.getString(Constants.KEY_FIRST_NAME),
                preferenceManager.getString(Constants.KEY_LAST_NAME)
        ));

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
           if (task.isSuccessful()&&task.getResult()!=null){
               sendFCMTokentoDatabase(task.getResult().getToken());
           }
        });

        RecyclerView usersRecyclerView=findViewById(R.id.usersRecycleView);
        textErrorMessage=findViewById(R.id.textErrorMessage);

        swipeRefreshLayout=findViewById(R.id.swiperefreshlayout);
        swipeRefreshLayout.setOnRefreshListener(this::getUsers);





        users=new ArrayList<>();
        usersAdapter=new UsersAdapter(users,this);
        usersRecyclerView.setAdapter(usersAdapter);

        getUsers();
        checkForBatteryOptimization();
    }
    private void getUsers(){

        swipeRefreshLayout.setRefreshing(true);

        FirebaseFirestore database=FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    swipeRefreshLayout.setRefreshing(false);
                    String myUserId=preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful()&&task.getResult()!=null){
                        users.clear();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                            if (myUserId.equals(documentSnapshot.getId())){
                                continue;
                            }
                            User user=new User();
                            user.firstName=documentSnapshot.getString(Constants.KEY_FIRST_NAME);
                            user.lastName=documentSnapshot.getString(Constants.KEY_LAST_NAME);
                            user.email=documentSnapshot.getString(Constants.KEY_EMAIL);
                            user.token=documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            users.add(user);
                        }
                        if (users.size() > 0){
                            usersAdapter.notifyDataSetChanged();
                        }
                    }else {
                        textErrorMessage.setText(String.format("%s","No User Available"));
                        textErrorMessage.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void sendFCMTokentoDatabase(String token){
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        DocumentReference documentReference=
                database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN,token)
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Unable to send token"+e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    private void signOut(){
        Toast.makeText(this,"Signing out.....",Toast.LENGTH_LONG).show();
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        DocumentReference documentReference=database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        HashMap <String,Object>updates=new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(aVoid -> {
                    preferenceManager.clearPreferences();
                    startActivity(new Intent(getApplicationContext(),SignInActivity.class));
                    finish();

                })
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Unable to Sign Out", Toast.LENGTH_SHORT).show());
    }

    public void SignOutBtn(View view) {
        signOut();
    }



    @Override
    public void initiateAudioMeeting(User user) {
        if (user.token == null || user.token.trim().isEmpty()){
            Toast.makeText(this,
                    user.firstName + " " + user.lastName + "is not available for meeting",
                    Toast.LENGTH_SHORT).show();
        }else {
            Intent intent=new Intent(getApplicationContext(),OutgoingInvitationActivity.class);
            intent.putExtra("user",user);
            intent.putExtra("type","audio");
            startActivity(intent);

        }

    }

    @Override
    public void initiateVideoMeeting(User user) {
        if (user.token == null || user.token.trim().isEmpty()){
            Toast.makeText(this,
                    user.firstName + " " + user.lastName + "is not available for meeting",
                    Toast.LENGTH_SHORT).show();
        }else {
          Intent intent=new Intent(getApplicationContext(),OutgoingInvitationActivity.class);
          intent.putExtra("user",user);
          intent.putExtra("type","video");
          startActivity(intent);
        }
    }

    @Override
    public void onMultiplerUsersAction(Boolean isMultipleUsersSelected) {
        if (isMultipleUsersSelected){
            imageConfrence.setVisibility(View.VISIBLE);
            imageConfrence.setOnClickListener(v -> {
                Intent intent=new Intent(getApplicationContext(),OutgoingInvitationActivity.class);
                intent.putExtra("selectedUsers",new Gson().toJson(usersAdapter.getSelectedUser()));
                intent.putExtra("type","video");
                intent.putExtra("isMultiple",true);
                startActivity(intent);
            });
        }else {imageConfrence.setVisibility(View.GONE);}

    }
    private void checkForBatteryOptimization(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            PowerManager powerManager=(PowerManager)getSystemService(POWER_SERVICE);
            if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())){
                AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("warning");
                builder.setMessage("Battery Optimization is enabled. It can interrupt running background services");
                builder.setPositiveButton("Disable", (dialog, which) -> {
                    Intent intent=new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    startActivityForResult(intent,REQUEST_CODE_BATTERY_OPTIMIZATION);
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                builder.create().show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_CODE_BATTERY_OPTIMIZATION){
            checkForBatteryOptimization();
        }
    }
}

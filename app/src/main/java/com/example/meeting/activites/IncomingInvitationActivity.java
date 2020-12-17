package com.example.meeting.activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.meeting.R;
import com.example.meeting.network.ApiClient;
import com.example.meeting.network.ApiService;
import com.example.meeting.utilities.Constants;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;

public class IncomingInvitationActivity extends AppCompatActivity {
    private String meetingType=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_invitation);
        ImageView imageMeetingType=findViewById(R.id.imageMeetingType);
         meetingType = getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_TYPE);

        if (meetingType!=null){
            if (meetingType.equals("video")){
                imageMeetingType.setImageResource(R.drawable.ic_video);
            }else {
                imageMeetingType.setImageResource(R.drawable.ic_audio);
            }
        }
        TextView textFirstChar=findViewById(R.id.textFirstChar3);
        TextView textUserName=findViewById(R.id.textUserName3);
        TextView textEmail=findViewById(R.id.textEmail3);

        String firstName=getIntent().getStringExtra(Constants.KEY_FIRST_NAME);
        if (firstName!=null){
            textFirstChar.setText(firstName.substring(0,1));
        }
        textUserName.setText(String.format(
                "%s %s",
                firstName,
                getIntent().getStringExtra(Constants.KEY_LAST_NAME)

        ));
        textEmail.setText(getIntent().getStringExtra(Constants.KEY_EMAIL));
    }
    private void sendInvitationMessage(String type,String receiverToken){
        try {
            JSONArray tokens=new JSONArray();
            tokens.put(receiverToken);
            JSONObject body=new JSONObject();
            JSONObject data=new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE,Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE,type);

            body.put(Constants.REMOTE_MSG_DATA,data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens);

            sendRemoteMessage(body.toString(),type);

        }catch (Exception exception){
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }

    }
    private void sendRemoteMessage(String remoteMessageBody,String type){
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(Constants.getRemoteMessageHeader(),remoteMessageBody).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()){
                    if (type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)){

                        try {
                            URL serverURL=new URL("https://meet.jit.si");

                            JitsiMeetConferenceOptions.Builder builder=new JitsiMeetConferenceOptions.Builder();
                            builder.setServerURL(serverURL);
                            builder.setWelcomePageEnabled(false);
                            builder.setRoom(getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_ROOM));

                            if(meetingType.equals("audio")){
                                builder.setVideoMuted(true);

                            }

                            JitsiMeetActivity.launch(IncomingInvitationActivity.this,builder.build());
                            finish();

                        }
                        catch (Exception e){
                            Toast.makeText(IncomingInvitationActivity.this, e.getMessage() , Toast.LENGTH_SHORT).show();
                            finish();
                        }


                    }else {
                        Toast.makeText(IncomingInvitationActivity.this, "Invitation Rejected", Toast.LENGTH_SHORT).show();
                        finish();

                    }

                }else {
                    Toast.makeText(IncomingInvitationActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                    finish();

                }

            }

            @Override
            public void onFailure(@NonNull Call<String> call,@NonNull Throwable t) {
                Toast.makeText(IncomingInvitationActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();

            }
        });
    }

    public void ImageBTnAccept(View view) {
        sendInvitationMessage(Constants.REMOTE_MSG_INVITATION_ACCEPTED,
                getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN));

    }

    public void ImageBtnReject(View view) {
        sendInvitationMessage(Constants.REMOTE_MSG_INVITATION_REJECTED,
                getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN));

    }
    private BroadcastReceiver invitationResponseReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type=intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_CANCELLED);
            if (type!=null){
                if (type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)){
                    Toast.makeText(context, "Invitation Cancelled", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    };
    ///hna hn2of hna


    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(invitationResponseReceiver,
                new IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(invitationResponseReceiver);

    }
}

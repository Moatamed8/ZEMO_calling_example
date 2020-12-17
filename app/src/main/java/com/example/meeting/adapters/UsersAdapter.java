package com.example.meeting.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.meeting.R;
import com.example.meeting.listeners.UsersListeners;
import com.example.meeting.models.User;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {
    private List<User>users;
    private UsersListeners usersListeners;
    private List<User> selectedUsers;

    public UsersAdapter(List<User> users ,UsersListeners usersListeners) {

        this.users = users;
        this.usersListeners=usersListeners;
        selectedUsers=new ArrayList<>();

    }
    public List<User>getSelectedUser(){
        return selectedUsers;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_user,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

     class UserViewHolder extends RecyclerView.ViewHolder{
        TextView textFirstChar,textUserName,textEmail;
        ImageView imageAudioMeeting,imageVideoMeeting;
        ConstraintLayout userContainer;
        ImageView imageSelected;


        UserViewHolder(@NonNull View itemView) {
            super(itemView);
        textFirstChar=itemView.findViewById(R.id.textFirstChar);
        textUserName=itemView.findViewById(R.id.textUserName);
        textEmail=itemView.findViewById(R.id.textEmail);
        imageAudioMeeting=itemView.findViewById(R.id.imageAudioMeeting);
        imageVideoMeeting=itemView.findViewById(R.id.imageVideoMeeting);
        userContainer=itemView.findViewById(R.id.userContainer);
        imageSelected=itemView.findViewById(R.id.imageItemSelected);

        }
        void setUserData(User user)
        {
            textFirstChar.setText(user.firstName.substring(0,1));
            textUserName.setText(String.format("%s %s",user.firstName,user.lastName));
            textEmail.setText(user.email);
            imageAudioMeeting.setOnClickListener(v -> usersListeners.initiateAudioMeeting(user));
            imageVideoMeeting.setOnClickListener(v -> usersListeners.initiateVideoMeeting(user));

            userContainer.setOnLongClickListener(v -> {
                if (imageSelected.getVisibility()!=View.VISIBLE){
                    selectedUsers.add(user);
                    imageSelected.setVisibility(View.VISIBLE);
                    imageAudioMeeting.setVisibility(View.GONE);
                    imageVideoMeeting.setVisibility(View.GONE);
                    usersListeners.onMultiplerUsersAction(true);
                }


                return true;
            });

            userContainer.setOnClickListener(v -> {
                if (imageSelected.getVisibility()==View.VISIBLE){
                    selectedUsers.remove(user);
                    imageSelected.setVisibility(View.GONE);
                    imageAudioMeeting.setVisibility(View.VISIBLE);
                    imageVideoMeeting.setVisibility(View.VISIBLE);

                    if (selectedUsers.size()==0){
                        usersListeners.onMultiplerUsersAction(false);

                    }else {
                        if (selectedUsers.size()>0){
                            selectedUsers.add(user);
                            imageSelected.setVisibility(View.VISIBLE);
                            imageVideoMeeting.setVisibility(View.GONE);
                            imageAudioMeeting.setVisibility(View.GONE);
                        }
                    }
                }
            });
        }
    }
}

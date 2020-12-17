package com.example.meeting.listeners;

import com.example.meeting.models.User;

public interface UsersListeners {

    void initiateAudioMeeting (User user);

    void initiateVideoMeeting  (User user);

    void onMultiplerUsersAction(Boolean isMultipleUsersSelected);
}

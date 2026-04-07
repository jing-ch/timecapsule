package edu.northeastern.timecapsule.model;

import com.google.firebase.Timestamp;

public class Friend {
    private String friendUid;
    private String friendEmail;
    private String friendName;
    private Timestamp addedAt;

    public Friend() {
        // Firestore needs empty constructor
    }

    public Friend(String friendUid, String friendEmail, String friendName, Timestamp addedAt) {
        this.friendUid = friendUid;
        this.friendEmail = friendEmail;
        this.friendName = friendName;
        this.addedAt = addedAt;
    }

    public String getFriendUid() {
        return friendUid;
    }

    public void setFriendUid(String friendUid) {
        this.friendUid = friendUid;
    }

    public String getFriendEmail() {
        return friendEmail;
    }

    public void setFriendEmail(String friendEmail) {
        this.friendEmail = friendEmail;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public Timestamp getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Timestamp addedAt) {
        this.addedAt = addedAt;
    }
}
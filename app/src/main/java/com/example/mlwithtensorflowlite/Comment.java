package com.example.mlwithtensorflowlite;

import com.google.firebase.Timestamp;

public class Comment {
    private String userId;
    private String username;
    private String content;
    private Timestamp timestamp;

    // Required empty constructor for Firestore
    public Comment() {}

    public Comment(String userId, String username, String content, Timestamp timestamp) {
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getContent() {
        return content;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}

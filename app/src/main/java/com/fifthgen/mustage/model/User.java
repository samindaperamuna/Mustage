package com.fifthgen.mustage.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fifthgen.mustage.Const;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created on 02/05/2017.
 * Copyright by 01eg.me
 */

public class User {

    private String id;
    private String name;
    private String photo;

    public User() {
    }

    public static DatabaseReference collection() {
        return FirebaseDatabase.getInstance().getReference(Const.kUsersKey);
    }

    public static DatabaseReference collection(String userId) {
        return FirebaseDatabase.getInstance().getReference(Const.kUsersKey).child(userId);
    }

    public static DatabaseReference following(String userId) {
        return collection(userId).child(Const.kFollowinsKey);
    }

    public static DatabaseReference followers(String userId) {
        return collection(userId).child(Const.kFollowersKey);
    }

    public static DatabaseReference uploads(String userId) {
        return FirebaseDatabase.getInstance().getReference(Const.kDataUploadsKey).child(userId);
    }

    public static DatabaseReference chats() {
        return FirebaseDatabase.getInstance().getReference(Const.kChatsKey).child(User.current().getKey());
    }

    public static DatabaseReference messages(String chat) {
        return FirebaseDatabase.getInstance().getReference(Const.kMessagesKey).child(chat);
    }

    public static String currentKey() {
        return FirebaseAuth.getInstance().getUid();
    }

    @Nullable
    public static DatabaseReference current() {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            return collection(userId);
        }

        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

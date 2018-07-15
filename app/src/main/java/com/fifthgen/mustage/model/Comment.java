package com.fifthgen.mustage.model;

import com.fifthgen.mustage.Const;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

/**
 * Created on 10/05/2017.
 * Copyright by 01eg.me
 */

public class Comment {

    private String profile_id;
    private String message;
    private String profile_name;
    private String profile_image;
    private String time;

    public Comment() {
        // default constructor
    }

    public static DatabaseReference collection(String storyKey) {
        return FirebaseDatabase.getInstance().getReference(Const.kDataCommentKey).child(storyKey);
    }

    /* Constructor */

    public static Comment send(final String storyKey, String message) {
        final Comment comment = new Comment();
        comment.time = Const.dateFormatter.format(new Date());
        comment.profile_id = User.current().getKey();
        comment.message = message;

        User.current().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    comment.profile_name = user.getName();
                    comment.profile_image = user.getPhoto();
                }

                collection(storyKey).push().setValue(comment);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // can't send
            }
        });

        return comment;
    }

    /* Properties */

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getProfile_id() {
        return profile_id;
    }

    public void setProfile_id(String profile_id) {
        this.profile_id = profile_id;
    }

    public String getProfile_name() {
        return profile_name;
    }

    public void setProfile_name(String profile_name) {
        this.profile_name = profile_name;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}

package com.fifthgen.mustage.model;

import android.net.Uri;
import android.text.format.DateUtils;

import com.fifthgen.mustage.Const;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.UploadTask;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static android.text.format.DateUtils.MINUTE_IN_MILLIS;

/**
 * Created on 28/04/2017.
 * Copyright by 01eg.me
 * <p>
 * <b>
 * Modified on 19/06/2018.
 * Author Saminda Peramuna
 * </b>
 */
public class Story {

    private String user;
    private String image;
    private String video;
    private String time;
    private String message;
    private List<String> tags;

    public static DatabaseReference recent() {
        return FirebaseDatabase.getInstance().getReference(Const.kDataRecentsKey);
    }

    public static DatabaseReference feed(String userId) {
        return FirebaseDatabase.getInstance().getReference(Const.kUserFeedKey).child(userId);
    }

    public static DatabaseReference favorites(String userId) {
        return FirebaseDatabase.getInstance().getReference(Const.kDataFavoritesKey).child(userId);
    }

    public static DatabaseReference tags(String postId) {
        return FirebaseDatabase.getInstance().getReference(Const.kDataPostKey).child(postId).child(Const.kTagKey);
    }

    @SuppressWarnings({"VisibleForTests", "deprecation"})
    public static void uploadImageStory(UploadTask uploadTask, String message, List<String> tags) {
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            Uri downloadUri = taskSnapshot.getDownloadUrl();

            Story newStory = new Story();
            DatabaseReference userRef = User.current();

            if (userRef != null && downloadUri != null) {
                newStory.setUser(userRef.getKey());
                newStory.setImage(downloadUri.toString());
                newStory.setTime(Const.dateFormatter.format(new Date()));
                newStory.setMessage(message);
                newStory.setTags(tags);
                Story.uploadStory(newStory);
            }
        });
    }

    private static void uploadStory(Story story) {
        DatabaseReference mPostRef = FirebaseDatabase.getInstance().getReference(Const.kDataPostKey);
        String id = mPostRef.push().getKey();
        if (id != null)
            if (!id.isEmpty()) mPostRef.child(id).setValue(story);
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Exclude
    public CharSequence getTimeAgo() {
        if (this.time != null && this.time.length() > 0) {
            try {
                Date date = Const.dateFormatter.parse(getTime());
                return DateUtils.getRelativeTimeSpanString(date.getTime(), System.currentTimeMillis(),
                        MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

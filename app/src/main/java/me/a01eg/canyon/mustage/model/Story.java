package me.a01eg.canyon.mustage.model;

import android.net.Uri;
import android.text.format.DateUtils;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.UploadTask;

import java.text.ParseException;
import java.util.Date;

import me.a01eg.canyon.mustage.Const;

import static android.text.format.DateUtils.MINUTE_IN_MILLIS;

/**
 * Created on 28/04/2017.
 * Copyright by 01eg.me
 */

public class Story {

    private String user;
    private String image;
    private String video;
    private String time;
    private String message;

    public Story() {
    }

    public static DatabaseReference recent() {
        return FirebaseDatabase.getInstance().getReference(Const.kDataRecentsKey);
    }

    public static DatabaseReference feed(String userId) {
        return FirebaseDatabase.getInstance().getReference(Const.kUserFeedKey).child(userId);
    }

    public static DatabaseReference favorites(String userId) {
        return FirebaseDatabase.getInstance().getReference(Const.kDataFavoritesKey).child(userId);
    }

    @SuppressWarnings("VisibleForTests")
    public static void uploadImageStory(UploadTask uploadTask) {
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();

                Story newStory = new Story();
                newStory.setUser(User.current().getKey());
                newStory.setImage(downloadUrl.toString());
                newStory.setTime(Const.dateFormatter.format(new Date()));
                Story.uploadStory(newStory);
            }
        });
    }

    private static void uploadStory(Story story) {
        FirebaseDatabase.getInstance().getReference(Const.kDataPostKey).push()
                .setValue(story);
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

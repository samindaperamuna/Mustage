package me.a01eg.canyon.mustage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created on 06/07/2017.
 * Copyright by 01eg.me
 */

public final class Const {

    /**
     * Change this values to set another firebase key path.
     * Must be a non-empty string and not contain '.' '#' '$' '[' or ']'
     */

    public static final String kUserFeedKey = "user_feed";
    public static final String kUsersKey = "users";
    public static final String kFollowersKey = "followers";
    public static final String kFollowinsKey = "followings";
    public static final String kDataRecentsKey = "recents";
    public static final String kDataPostKey = "posts";
    public static final String kDataCommentKey = "comments";
    public static final String kDataLikeKey = "likes";
    public static final String kDataUploadsKey = "uploads";
    public static final String kDataFavoritesKey = "activity";
    public static final String kChatsKey = "chats";
    public static final String kMessagesKey = "messages";
    public static final String kTopicsFeed = "/topics/feed";
    public static final DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
}

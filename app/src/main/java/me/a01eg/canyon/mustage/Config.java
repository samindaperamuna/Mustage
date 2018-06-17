package me.a01eg.canyon.mustage;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

/**
 * Created on 02/05/2017.
 * Copyright by 01eg.me
 */

public final class Config {

    public static final String kReportContactEmail = "oleg@mail.com";
    public static final String EULA = "https://termsfeed.com/eula/generator/";

    // Icons
    public static final int ProfilePlaceholder = R.mipmap.profile_placeholder;
    public static final int StoryPlaceholder = R.mipmap.placeholder_dark;

    public static int getJPEGImageQuality() {
        return (int) FirebaseRemoteConfig.getInstance().getLong("image_quality");
    }

    public static int getPagination() {
        return (int) FirebaseRemoteConfig.getInstance().getLong("pref_pagination");
    }

    public static boolean isAdmobEnabled() {
        return FirebaseRemoteConfig.getInstance().getBoolean("admob_enabled")
                && BuildConfig.ADMOB_APP_ID != null;
    }

    public static boolean isPersonalFeed() {
        return FirebaseRemoteConfig.getInstance().getBoolean("personal_feed");
    }
}


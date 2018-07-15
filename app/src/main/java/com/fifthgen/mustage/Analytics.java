package com.fifthgen.mustage;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

public final class Analytics {

    private static final String STORY_TYPE = "story";
    private static final String COMMENT_TYPE = "comment";
    private static final String TAG = Analytics.class.getSimpleName();

    // should be initialized on first screen
    public static Analytics shared;
    private FirebaseAnalytics analytics;

    private Analytics(Context context) {
        analytics = FirebaseAnalytics.getInstance(context);
    }

    public static Analytics init(Context context) {
        shared = new Analytics(context);
        return shared;
    }

    /**
     * Tracking methods
     */

    public static void trackOpenCamera() {
        shared.analytics.logEvent("camera_opened", Bundle.EMPTY);
    }

    public static void trackRemoteConfigLoad(boolean successful) {
        shared.analytics.setUserProperty("remote_config_loading", String.valueOf(successful));
    }

    public static void trackAdmobLoading(String appId) {
        Log.d(TAG, "admob loaded: " + appId);
        shared.analytics.setUserProperty("admob_enabled", appId);
    }

    public static void trackCrashlogEnabled() {
        shared.analytics.setUserProperty("crashlog_enabled", "true");
    }

    public static void trackUserLogout() {
        Bundle bundle = new Bundle();
        shared.analytics.logEvent("logout", bundle);
    }

    public static void trackOpenStory(String id) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, STORY_TYPE);
        shared.analytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
    }

    public static void trackPersonalFeed(String uid) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.VALUE, "Open Personal Feed: " + uid);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, STORY_TYPE);
        shared.analytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM_LIST, bundle);
    }

    public static void trackRecentFeed() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.VALUE, "Open Recent Feed");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, STORY_TYPE);
        shared.analytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM_LIST, bundle);
    }

    public static void trackError(String localizedMessage) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.VALUE, localizedMessage);
        shared.analytics.logEvent("Error", bundle);
    }

    public static void trackSendComment(String storyKey, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, storyKey);
        bundle.putString(FirebaseAnalytics.Param.VALUE, message);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, COMMENT_TYPE);
        shared.analytics.logEvent("send_comment", bundle);
    }

    public static void trackOpenComments(String storyKey) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, storyKey);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, COMMENT_TYPE);
        shared.analytics.logEvent("open_comments", bundle);
    }
}

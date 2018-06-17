package me.a01eg.canyon.mustage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Arrays;
import java.util.List;

import me.a01eg.canyon.mustage.model.User;

import static me.a01eg.canyon.mustage.Const.kTopicsFeed;

/**
 * Created by 01eg.me 04/05/2017.
 */

public class AuthManager {

    public static final int REQUEST_AUTH_CODE = 123;

    private static AuthManager _shared = null;

    public static AuthManager getInstance() {
        if (_shared == null) {
            _shared = new AuthManager();
        }

        return _shared;
    }

    public boolean authorizeIfNeeded(Activity activity) {

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {

            // subscribe for new notifications
            FirebaseMessaging.getInstance().subscribeToTopic(kTopicsFeed + User.currentKey());

            // already signed in, open feed activity
            return false;
        } else {
            final List providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.PhoneBuilder().build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build(),
                    new AuthUI.IdpConfig.FacebookBuilder().build(),
                    new AuthUI.IdpConfig.TwitterBuilder().build());

            // show sign-in dialog
            Intent intent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setTosUrl(Config.EULA)
                    .setIsSmartLockEnabled(false, true) // disabled to let user logout
                    .build();

            activity.startActivityForResult(intent, REQUEST_AUTH_CODE);

            return true;
        }
    }

    public void completeAuth(final Context context) {
        final FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(fbuser.getUid());

        // subscribe for new notifications
        FirebaseMessaging.getInstance().subscribeToTopic(kTopicsFeed + User.currentKey());

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Bundle bundle = new Bundle();
                FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(context);

                // store user info if user not exist yet
                if (!dataSnapshot.exists()) {
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, fbuser.getUid());
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, fbuser.getDisplayName());

                    User user = new User();
                    user.setName(fbuser.getDisplayName());

                    if (fbuser.getPhotoUrl() != null) {
                        user.setPhoto(fbuser.getPhotoUrl().getPath());
                    }

                    ref.setValue(user);

                    // record new user
                    analytics.setUserProperty("username", user.getName());
                    analytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle);
                } else {
                    User user = dataSnapshot.getValue(User.class);
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, user.getId());
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, user.getName());

                    // user back to app, as user record is already created
                    analytics.setUserProperty("username", user.getName());
                    analytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void logout() {
        // subscribe for new notifications
        FirebaseMessaging.getInstance().unsubscribeFromTopic(kTopicsFeed + User.currentKey());
        FirebaseAuth.getInstance().signOut();
    }
}

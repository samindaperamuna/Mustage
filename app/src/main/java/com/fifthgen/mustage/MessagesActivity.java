package com.fifthgen.mustage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.fifthgen.mustage.model.User;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class MessagesActivity extends AppCompatActivity {

    private static final String EXTRA_KEY = "messages.chat_key";
    private static final String EXTRA_USER = "messages.user_key";
    private static final String EXTRA_TITLE = "messages.title_key";

    public static void start(final Context context, String key, String title) {
        Intent intent = new Intent(context, MessagesActivity.class);
        intent.putExtra(EXTRA_KEY, key);
        intent.putExtra(EXTRA_TITLE, title);
        context.startActivity(intent);
    }

    public static void start(final Context context, User user) {
        Intent intent = new Intent(context, MessagesActivity.class);
        intent.putExtra(EXTRA_USER, user.getId());
        intent.putExtra(EXTRA_TITLE, user.getName());
        context.startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!AuthManager.getInstance().authorizeIfNeeded(this)) {
            // display data
        } else {
            // show login screen
        }

        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_layout);

        FirebaseRemoteConfig.getInstance().setDefaults(R.xml.config_defaults);

        final String key = getIntent().getStringExtra(EXTRA_KEY);
        final String user = getIntent().getStringExtra(EXTRA_USER);
        final String title = getIntent().getStringExtra(EXTRA_TITLE);

        // show messages of the chat
        MessagesFragment messagesFragment = new MessagesFragment(key, user, title);
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, messagesFragment)
                .commit();
    }
}

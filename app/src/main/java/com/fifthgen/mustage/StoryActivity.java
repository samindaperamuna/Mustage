package com.fifthgen.mustage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class StoryActivity extends AppCompatActivity {

    private static final String EXTRA_STORY_ID = "extra.story.id";

    public static void navigate(Context context, String storyId) {
        Intent intent = new Intent(context, StoryActivity.class);
        intent.putExtra(EXTRA_STORY_ID, storyId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_layout);

        String storyId = getIntent().getStringExtra(EXTRA_STORY_ID);
        FeedFragment fragment = FeedFragment.newInstance(storyId);

        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }
}

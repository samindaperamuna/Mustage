package com.fifthgen.mustage.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fifthgen.mustage.Config;
import com.fifthgen.mustage.Const;
import com.fifthgen.mustage.R;
import com.fifthgen.mustage.StoryActivity;
import com.fifthgen.mustage.model.Story;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created on 23/05/2017.
 * Copyright by 01eg.me
 */

public class PreviewViewHolder extends RecyclerView.ViewHolder
        implements ValueEventListener, View.OnClickListener {

    private final ImageView imageView;
    private final ImageView muteStatusView;
    private DatabaseReference mPostRef;

    public PreviewViewHolder(View view) {
        super(view);

        imageView = view.findViewById(android.R.id.icon);
        imageView.setOnClickListener(this);

        muteStatusView = view.findViewById(R.id.mute_status);
        muteStatusView.setVisibility(View.INVISIBLE);
        muteStatusView.setSelected(true);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        unbindStory();
    }

    private void unbindStory() {
        if (mPostRef != null) {
            mPostRef.removeEventListener(this);
        }
    }

    public void bindStory(String storyId) {
        unbindStory();

        mPostRef = FirebaseDatabase.getInstance().getReference(Const.kDataPostKey).child(storyId);
        mPostRef.addValueEventListener(this);

        imageView.setImageResource(Config.StoryPlaceholder);
    }

    private void show(Story story) {
        final Context context = itemView.getContext();

        if (story == null)
            return;

        final String videoUrl = story.getVideo();
        final Boolean isVideo = videoUrl != null && videoUrl.length() > 0;

        if (isVideo) {
            muteStatusView.setVisibility(View.VISIBLE);
        } else {
            muteStatusView.setVisibility(View.INVISIBLE);
        }

        RequestOptions options = new RequestOptions()
                .placeholder(Config.StoryPlaceholder);

        Glide.with(context)
                .load(story.getImage())
                .apply(options)
                .into(imageView);
    }

    /* ValueChangeListener */

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (!dataSnapshot.exists()) {
            return;
        }

        show(dataSnapshot.getValue(Story.class));
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    @Override
    public void onClick(View v) {
        // navigate to story view
        StoryActivity.navigate(itemView.getContext(), mPostRef.getKey());
    }
}

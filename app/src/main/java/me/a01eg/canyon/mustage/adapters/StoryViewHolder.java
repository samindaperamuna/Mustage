package me.a01eg.canyon.mustage.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.perf.metrics.AddTrace;

import me.a01eg.canyon.mustage.AccountActivity;
import me.a01eg.canyon.mustage.Analytics;
import me.a01eg.canyon.mustage.CommentsActivity;
import me.a01eg.canyon.mustage.Config;
import me.a01eg.canyon.mustage.Const;
import me.a01eg.canyon.mustage.R;
import me.a01eg.canyon.mustage.model.Story;
import me.a01eg.canyon.mustage.model.User;

/**
 * Created on 23/05/2017.
 * Copyright by 01eg.me
 */

public class StoryViewHolder extends RecyclerView.ViewHolder implements
        MediaPlayer.OnPreparedListener,
        View.OnTouchListener,
        View.OnClickListener,
        ValueEventListener {
    private static final String TAG = StoryViewHolder.class.getSimpleName();
    private final ImageView imageView;
    private final VideoView videoView;
    private final TextView messageView;
    private final ImageView userImage;
    private final TextView userName;
    private final TextView timeView;
    private final ImageView muteStatusView;
    private final ImageButton btnLike;
    private final View btnReport;

    private MediaPlayer videoMediaPlayer;
    private Story mStory = null;
    private Boolean isMuted = true;

    private DatabaseReference mPostRef;
    private DatabaseReference mlikeRef;
    private DatabaseReference mUserRef;

    public StoryViewHolder(View view) {
        super(view);

        imageView = view.findViewById(android.R.id.icon);
        videoView = view.findViewById(R.id.video_view);
        messageView = view.findViewById(android.R.id.text1);

        userImage = view.findViewById(R.id.user_profile);
        userImage.setOnClickListener(this);

        userName = view.findViewById(R.id.user_name);
        timeView = view.findViewById(R.id.time);

        muteStatusView = view.findViewById(R.id.mute_status);
        muteStatusView.setSelected(isMuted);

        ImageButton btnComment = view.findViewById(R.id.btn_comment);
        btnComment.setOnClickListener(this);

        btnLike = view.findViewById(R.id.btn_like);
        btnLike.setOnClickListener(this);

        ImageButton btnShare = view.findViewById(R.id.btn_share);
        btnShare.setOnClickListener(this);

        btnReport = view.findViewById(R.id.btn_report);
        btnReport.setOnClickListener(this);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        unbindStory();
    }

    private void unbindStory() {

        if (mPostRef != null) {
            mPostRef.removeEventListener(this);
            mPostRef = null;
        }

        if (mlikeRef != null) {
            mlikeRef.removeEventListener(this);
            mlikeRef = null;
        }

        if (mUserRef != null) {
            mUserRef.removeEventListener(this);
            mUserRef = null;
        }
    }

    @AddTrace(name = "storyView.bind")
    public void bindStory(String storyId) {
        unbindStory();

        mPostRef = FirebaseDatabase.getInstance().getReference(Const.kDataPostKey).child(storyId);
        mPostRef.addListenerForSingleValueEvent(this);

        userName.setText(null);
        timeView.setText(null);
        messageView.setText(null);
        imageView.setImageResource(Config.StoryPlaceholder);
        userImage.setImageResource(Config.ProfilePlaceholder);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        videoMediaPlayer = mp;

        if (isMuted) {
            videoMediaPlayer.setVolume(0, 0);
        } else {
            videoMediaPlayer.setVolume(1, 1);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // mute/unmute video
        if (event.getAction() == MotionEvent.ACTION_UP) {
            try {
                if (videoMediaPlayer != null && videoMediaPlayer.isPlaying()) {
                    if (isMuted) {
                        isMuted = false;
                        videoMediaPlayer.setVolume(1, 1);
                        muteStatusView.setSelected(isMuted);
                    } else {
                        isMuted = true;
                        videoMediaPlayer.setVolume(0, 0);
                        muteStatusView.setSelected(isMuted);
                    }
                }
            } catch (Exception e) {
                Analytics.trackError(e.getLocalizedMessage());
            }
        }

        v.performClick();

        return true;
    }

    @AddTrace(name = "storyView.render")
    private void show(Story story) {

        final Activity context = (Activity) itemView.getContext();

        if (story == null || context.isDestroyed()) {
            return;
        }

        mStory = story;
        final String video = story.getVideo();

        if (video != null && video.length() > 0) {
            imageView.setVisibility(View.INVISIBLE);
            videoView.setVisibility(View.VISIBLE);
            muteStatusView.setVisibility(View.VISIBLE);

            videoView.stopPlayback(); // stop previous
            videoView.setOnPreparedListener(this);
            videoView.setVideoURI(Uri.parse(story.getVideo()));
            videoView.setOnTouchListener(this);
            videoView.start();
        } else {
            imageView.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.INVISIBLE);
            isMuted = true;

            muteStatusView.setVisibility(View.INVISIBLE);
            muteStatusView.setSelected(isMuted);

            DrawableTransitionOptions transitionOptions = new DrawableTransitionOptions()
                    .crossFade();
            RequestOptions options = new RequestOptions()
                    .placeholder(Config.StoryPlaceholder);

            Glide.with(context)
                    .load(story.getImage())
                    .transition(transitionOptions)
                    .apply(options)
                    .into(imageView);
        }

        timeView.setText(story.getTimeAgo());

        mUserRef = User.collection(story.getUser());
        mUserRef.removeEventListener(this);
        mUserRef.addValueEventListener(this);

        boolean current = mUserRef.getKey().contentEquals(User.current().getKey());

        if (current) {
            btnReport.setVisibility(View.INVISIBLE);
        } else {
            btnReport.setVisibility(View.VISIBLE);
        }

        DatabaseReference likes = FirebaseDatabase.getInstance().getReference().child(Const.kDataLikeKey);
        mlikeRef = likes.child(mPostRef.getKey()).child(User.current().getKey()).child("liked");
        mlikeRef.removeEventListener(this);
        mlikeRef.addValueEventListener(this);

        String html = story.getMessage();
        Spanned spanned = null;

        if (html != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                spanned = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
            } else {
                spanned = Html.fromHtml(html);
            }
        }

        messageView.setText(spanned);
    }

    /* Actions */

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_like:
                onLikeClicked(v);
                break;
            case R.id.btn_comment:
                onCommentClicked(v);
                break;
            case R.id.btn_share:
                onShareClicked(v);
                break;
            case R.id.user_profile:
                onProfileClicked(v);
                break;
            case R.id.btn_report:
                onReportClicked(v);
                break;
        }
    }

    private void onReportClicked(View v) {
        final Context context = itemView.getContext();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.report_title);
        builder.setMessage(R.string.report_message);
        builder.setPositiveButton(R.string.report_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // need to report the user/story
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/html");
                intent.putExtra(Intent.EXTRA_EMAIL, Config.kReportContactEmail);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Report the story/user");
                intent.putExtra(Intent.EXTRA_TEXT, "Please, report the story, with id: "
                        + mPostRef.toString() + " where user: " + mUserRef.toString());

                context.startActivity(Intent.createChooser(intent, context.getString(R.string.report_send)));
            }
        });

        builder.setNegativeButton(R.string.dialog_cancel, null);
        builder.create().show();
    }

    private void onProfileClicked(View v) {
        // open user activity
        final Context context = itemView.getContext();
        Intent intent = new Intent(context, AccountActivity.class);
        intent.putExtra(AccountActivity.EXTRA_USER_ID, mUserRef.getKey());
        context.startActivity(intent);
    }

    private void onShareClicked(View v) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, "Sharing URL");

        if (mStory.getVideo() != null && mStory.getVideo().length() > 0) {
            // share video
            i.putExtra(Intent.EXTRA_TEXT, mStory.getVideo());
        } else {
            // share photo
            i.putExtra(Intent.EXTRA_TEXT, mStory.getImage());
        }

        final Context context = itemView.getContext();
        context.startActivity(Intent.createChooser(i, "Share URL"));
    }

    private void onCommentClicked(View v) {
        // open comments activity
        final Context context = itemView.getContext();
        Intent intent = new Intent(context, CommentsActivity.class);
        intent.putExtra(CommentsActivity.EXTRA_STORY_KEY, mPostRef.getKey());
        context.startActivity(intent);
    }

    private void onLikeClicked(View v) {
        boolean isLiked = !btnLike.isSelected();
        final String currentUserKey = User.currentKey();

        DatabaseReference likes = FirebaseDatabase.getInstance().getReference().child(Const.kDataLikeKey);
        DatabaseReference favorites = FirebaseDatabase.getInstance().getReference().child(Const.kDataFavoritesKey);

        DatabaseReference curLike = likes.child(mPostRef.getKey()).child(currentUserKey).child("liked");
        DatabaseReference curFavorite = favorites.child(currentUserKey).child(mPostRef.getKey());

        // update Model
        curLike.setValue(isLiked);

        if (isLiked) {
            curFavorite.setValue(true);
        } else {
            curFavorite.removeValue();
        }

        // update UI
        btnLike.setSelected(isLiked);
    }

    /* ValueChangeListener */

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {

        Activity activity = (Activity) itemView.getContext();

        if(!dataSnapshot.exists() || activity.isDestroyed()) {
            Log.w(TAG, dataSnapshot.getRef().toString() + " not exist!");
            return;
        }

        final String ref = dataSnapshot.getRef().toString();

        if (ref == null) {
            return;
        }

        if (mPostRef != null && ref.equals(mPostRef.toString())) {
            show(dataSnapshot.getValue(Story.class));
        }

        if (mUserRef != null && ref.equals(mUserRef.toString())) {
            User user = dataSnapshot.getValue(User.class);
            if (user.getName() != null && !user.getName().isEmpty()) {
                userName.setText(user.getName());
            } else {
                userName.setText("Unknown");
            }

            RequestOptions options = new RequestOptions()
                    .placeholder(Config.ProfilePlaceholder);

            if (user.getPhoto() != null && user.getPhoto().length() > 0) {

                Glide.with(activity)
                        .load(user.getPhoto())
                        .apply(options)
                        .into(userImage);
            }
        }

        if (mlikeRef != null && ref.equals(mlikeRef.toString())) {
            Boolean isLiked = dataSnapshot.getValue(Boolean.class);

            if (isLiked != null) {
                btnLike.setSelected(isLiked);
            }
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
    }
}

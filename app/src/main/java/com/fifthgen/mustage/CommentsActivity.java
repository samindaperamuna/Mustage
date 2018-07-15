package com.fifthgen.mustage;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fifthgen.mustage.adapters.PaginationAdapter;
import com.fifthgen.mustage.model.Comment;
import com.fifthgen.mustage.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

public class CommentsActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String EXTRA_STORY_KEY = "extra.story.key";

    private EditText mCommentView;
    private ImageButton mSendButton;
    private RecyclerView mList;
    private PaginationAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        mList = findViewById(R.id.list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, false);
        mList.setLayoutManager(mLayoutManager);
        mList.setItemAnimator(new DefaultItemAnimator());

        mCommentView = findViewById(R.id.comment_view);
        mSendButton = findViewById(R.id.btn_send);
        mSendButton.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        String storyKey = getIntent().getStringExtra(EXTRA_STORY_KEY);

        if (storyKey != null) {
            // load comments
            showComments(storyKey);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    /* AdapterView */

    private void sendComment() {

        if (mCommentView.getText() == null) {
            return;
        }

        String storyKey = getIntent().getStringExtra(EXTRA_STORY_KEY);
        String message = mCommentView.getText().toString().trim();

        if (storyKey != null && message.length() > 0) {
            Analytics.trackSendComment(storyKey, message);

            Comment.send(storyKey, message);
            // empty field
            mCommentView.setText(null);
        }
    }

    private void showComments(String storyKey) {
        if (mAdapter == null) {

            Analytics.trackOpenComments(storyKey);
            DatabaseReference ref = Comment.collection(storyKey);

            mAdapter = new PaginationAdapter<CommentViewHolder>(CommentViewHolder.class, R.layout.comment_item, ref) {
                @Override
                protected void populateViewHolder(CommentViewHolder viewHolder, DataSnapshot snapshot, int position) {
                    viewHolder.bind(snapshot.getValue(Comment.class), snapshot.getRef());
                }
            };

            mList.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mSendButton) {
            sendComment();
        }
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        final ImageView profilePhotoView;
        final TextView messageView;
        Comment mComment;
        DatabaseReference mRef;

        public CommentViewHolder(View itemView) {
            super(itemView);

            profilePhotoView = itemView.findViewById(R.id.profile_photo);
            messageView = itemView.findViewById(R.id.comment_text);

            profilePhotoView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @SuppressWarnings("deprecation")
        public void bind(Comment comment, DatabaseReference ref) {

            mRef = ref;
            mComment = comment;

            String username = comment.getProfile_name();
            String message = comment.getMessage();
            String html = "<b>" + username + "</b>: " + message;
            Spanned spanned;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                spanned = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
            } else {
                spanned = Html.fromHtml(html);
            }

            messageView.setText(spanned);

            if (comment.getProfile_image() != null) {
                Glide.with(itemView.getContext())
                        .load(comment.getProfile_image())
                        .into(profilePhotoView);
            }
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.profile_photo:
                    onProfileClicked(v);
                    break;
            }
        }

        private void onRemoveCommendClicked(View v) {
            // remove own comment only
            if (mComment.getProfile_id().equalsIgnoreCase(User.current().getKey())) {
                mRef.removeValue();
            }
        }

        private void onProfileClicked(View v) {
            // open user activity
            final Context context = itemView.getContext();
            Intent intent = new Intent(context, AccountActivity.class);
            intent.putExtra(AccountActivity.EXTRA_USER_ID, mComment.getProfile_id());
            context.startActivity(intent);
        }

        @Override
        public boolean onLongClick(final View view) {

            // if this is my comment I can remove it
            if (mComment.getProfile_id().equalsIgnoreCase(User.current().getKey())) {
                AlertDialog alert = new AlertDialog.Builder(itemView.getContext())
                        .setMessage("Do you want to delete the comment?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                onRemoveCommendClicked(view);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                alert.show();
                return true;
            }

            return false;
        }
    }
}
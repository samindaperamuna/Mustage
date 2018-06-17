package me.a01eg.canyon.mustage;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import me.a01eg.canyon.mustage.model.Message;
import me.a01eg.canyon.mustage.model.User;

/**
 * Created on 05/09/2017.
 * Copyright by oleg
 */

public class MessageViewHolder extends RecyclerView.ViewHolder implements ValueEventListener {

    private static final String TAG = "MessageViewHolder";
    private final ImageView profileImageLeft;
    private final ImageView profileImageRight;
    private ImageView profileImage;
    private final TextView nameText;
    private DatabaseReference mContactRef;

    public MessageViewHolder(View view) {
        super(view);

        profileImageLeft = view.findViewById(R.id.iconLeft);
        profileImageRight = view.findViewById(R.id.iconRight);
        nameText = view.findViewById(android.R.id.title);
    }

    public void showMessage(Message message) {
        if (message == null) return;

        nameText.setText(message.getMessage());

        // get user image, just once
        if (message.getUser_id() != null) {

            String currentId = User.current() != null ? User.current().getKey() : "";

            if (message.getUser_id().equalsIgnoreCase(currentId)) {
                profileImage = profileImageRight;
                profileImageLeft.setVisibility(View.INVISIBLE);
                nameText.setGravity(Gravity.RIGHT);
            } else {
                profileImage = profileImageLeft;
                nameText.setGravity(Gravity.LEFT);
                profileImageRight.setVisibility(View.INVISIBLE);
            }

            mContactRef = User.collection(message.getUser_id());
            mContactRef.addListenerForSingleValueEvent(this);
        } else { // defaults
            profileImage = profileImageLeft;
            nameText.setGravity(Gravity.LEFT);
            profileImageLeft.setVisibility(View.VISIBLE);
            profileImageRight.setVisibility(View.INVISIBLE);
        }
    }

    // ValueEventListener

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {

        if (mContactRef != null && dataSnapshot.getKey().equals(mContactRef.getKey())) {
            User user = dataSnapshot.getValue(User.class);
            user.setId(dataSnapshot.getKey());

            Context context = itemView.getContext();
            RequestOptions options = new RequestOptions()
                    .circleCrop()
                    .placeholder(Config.ProfilePlaceholder);
            Glide.with(context).load(user.getPhoto())
                    .apply(options)
                    .into(profileImage);

        } else if (dataSnapshot.exists()) {
            showMessage(dataSnapshot.getValue(Message.class));
        } else {
            Log.e(TAG, "Data not exist - onDataChange()");
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.e(TAG, databaseError.toString());
    }
}

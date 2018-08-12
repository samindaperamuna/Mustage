package com.fifthgen.mustage.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fifthgen.mustage.Config;
import com.fifthgen.mustage.R;
import com.fifthgen.mustage.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class FollowingViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
        ValueEventListener {

    private final ImageView imageView;
    private final TextView textView;

    private DatabaseReference mUserRef;

    public FollowingViewHolder(View itemView) {
        super(itemView);

        imageView = itemView.findViewById(R.id.imageView);
        textView = itemView.findViewById(R.id.textView);

        ImageButton homeButton = itemView.findViewById(R.id.homeButton);
        homeButton.setOnClickListener(this);

        ImageButton notificationButton = itemView.findViewById(R.id.notificationButton);
        notificationButton.setOnClickListener(this);

        ImageButton binocularButton = itemView.findViewById(R.id.binocularButton);
        binocularButton.setOnClickListener(this);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        unbindRef();
    }

    public void bindRef(String userRef) {
        unbindRef();

        mUserRef = User.collection(userRef);
        mUserRef.addValueEventListener(this);
    }

    private void unbindRef() {
        if (mUserRef != null) {
            mUserRef.removeEventListener(this);
            mUserRef = null;
        }
    }

    private void loadUser(User user) {
        final Context context = itemView.getContext();

        textView.setText(user.getName());

        RequestOptions options = new RequestOptions()
                .placeholder(Config.ProfilePlaceholder);

        Glide.with(context)
                .load(user.getPhoto())
                .apply(options)
                .into(imageView);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.homeButton:
                break;
            case R.id.notificationButton:
                break;
            case R.id.binocularButton:
                break;
        }
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists()) {
            DatabaseReference ref = dataSnapshot.getRef();

            if (mUserRef != null && mUserRef.equals(ref)) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    loadUser(user);
                }
            }
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
        Log.e(FollowingViewHolder.class.getName(), "Error in Firebase transaction: " + databaseError.getMessage());
    }
}

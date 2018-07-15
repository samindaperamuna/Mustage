package com.fifthgen.mustage;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fifthgen.mustage.model.Chat;

/**
 * Created on 05/09/2017.
 * Copyright by oleg
 */

public class ChatViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "ContactViewHolder";
    private final ImageView profileImage;
    private final TextView nameText;

    public ChatViewHolder(View view) {
        super(view);

        profileImage = view.findViewById(android.R.id.icon);
        nameText = view.findViewById(android.R.id.title);
    }

    public void showChat(Chat chat) {
        final Context context = itemView.getContext();

        RequestOptions options = new RequestOptions()
                .placeholder(Config.ProfilePlaceholder);

        nameText.setText(chat.getName());

        Glide.with(context)
                .load(chat.getProfile())
                .apply(options)
                .into(profileImage);
    }

}

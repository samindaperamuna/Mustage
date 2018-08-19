package com.fifthgen.mustage.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fifthgen.mustage.R;
import com.fifthgen.mustage.model.User;
import com.fifthgen.mustage.views.CircleImageView;

import java.util.List;

public class MomentsAdapter extends RecyclerView.Adapter<MomentsAdapter.MomentViewHolder> {

    // Layout resource
    private int mLayout;

    private List<User> mUsers;

    public MomentsAdapter(@LayoutRes int layout, List<User> users) {
        mLayout = layout;
        mUsers = users;
    }

    @Override
    public int getItemViewType(int position) {
        return mLayout;
    }

    @NonNull
    @Override
    public MomentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);

        return new MomentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MomentViewHolder holder, int position) {
        User user = mUsers.get(position);
        Glide.with(holder.imageView).load(user.getPhoto()).into(holder.imageView);
        holder.textView.setText(user.getName());
    }

    @Override
    public int getItemCount() {
        if (mUsers != null && !mUsers.isEmpty())
            return mUsers.size();

        return 0;
    }

    class MomentViewHolder extends RecyclerView.ViewHolder {

        CircleImageView imageView;
        TextView textView;

        MomentViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
            textView = itemView.findViewById(R.id.textView);
        }
    }
}

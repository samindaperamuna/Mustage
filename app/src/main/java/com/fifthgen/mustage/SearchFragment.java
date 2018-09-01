package com.fifthgen.mustage;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.fifthgen.mustage.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment implements TextWatcher {

    private RecyclerView mList;
    private EditText mEditText;

    /**
     * Required empty constructor.
     */
    public SearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_search, container, false);
        mList = view.findViewById(R.id.list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mList.setLayoutManager(mLayoutManager);
        mList.setItemAnimator(new DefaultItemAnimator());
        mList.setAdapter(new SearchAdapter());

        mEditText = view.findViewById(R.id.search);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mEditText != null)
            mEditText.addTextChangedListener(this);

        loadData();
    }

    private void loadData() {
        User.collection()
                .orderByChild("liked")
                .limitToLast(10)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        final SearchAdapter adapter = new SearchAdapter();

                        for (DataSnapshot item : dataSnapshot.getChildren()) {
                            User user = item.getValue(User.class);

                            if (user != null) {
                                user.setId(item.getKey());

                                // If not the currently logged in user add it to the adapter.
                                if (!user.getId().equals(User.currentKey())) {
                                    // If its the current user get his followings and notify items.
                                    if (user.getId().equals(User.currentKey())) {
                                        GenericTypeIndicator<HashMap<String, Boolean>> type = new GenericTypeIndicator<HashMap<String, Boolean>>() {
                                        };
                                        adapter.mFollowingMap = item.child(Const.kFollowinsKey).getValue(type);
                                        adapter.mNotifyMap = item.child(Const.kNotifyKey).getValue(type);
                                    }

                                    adapter.mUsers.add(user);
                                }
                            }
                        }

                        mList.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("E", databaseError.getMessage());
                    }
                });
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mEditText != null)
            mEditText.removeTextChangedListener(this);
    }

    // TextWatcher

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s != null) {
            handleSearch(s.toString());
        }
    }

    private void handleSearch(final String query) {

        if (query == null || query.length() == 0) return;

        User.collection().orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    final SearchAdapter adapter = new SearchAdapter();
                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        User user = item.getValue(User.class);

                        if (user != null && user.getName() != null && !user.getId().equals(User.currentKey())) {
                            // If its the current user get his followings and notify items.
                            if (user.getId().equals(User.currentKey())) {
                                GenericTypeIndicator<HashMap<String, Boolean>> type = new GenericTypeIndicator<HashMap<String, Boolean>>() {
                                };
                                adapter.mFollowingMap = item.child(Const.kFollowinsKey).getValue(type);
                                adapter.mNotifyMap = item.child(Const.kNotifyKey).getValue(type);
                            }

                            String childName = user.getName();

                            if (childName.toLowerCase().contains(query)) {
                                user.setId(item.getKey());
                                adapter.mUsers.add(user);
                            }
                        }
                    }

                    mList.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("E", databaseError.getMessage());
            }
        });
    }

    /**
     * Adapter class that handles the Search results display.
     */
    class SearchAdapter extends RecyclerView.Adapter<UserViewHolder> {
        ArrayList<User> mUsers = new ArrayList<>();
        Map<String, Boolean> mFollowingMap;
        Map<String, Boolean> mNotifyMap;

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item, parent, false);
            return new UserViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            User user = mUsers.get(position);
            Boolean following = false, notify = false;
            if (mFollowingMap != null && mFollowingMap.containsKey(user.getId())) {
                following = true;
            }
            if (mNotifyMap != null && mNotifyMap.containsKey(user.getId())) {
                notify = true;
            }
            holder.bind(user, following, notify);
        }

        @Override
        public int getItemCount() {
            return mUsers.size();
        }
    }

    /**
     * View holder that contains the recycler view.
     */
    class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView profilePhotoView;
        final TextView profileName;

        User mUser;

        private final ImageButton homeButton;
        private final ImageButton notificationButton;
        private final ImageButton binocularButton;

        UserViewHolder(View itemView) {
            super(itemView);

            profilePhotoView = itemView.findViewById(R.id.profile_photo);
            profileName = itemView.findViewById(R.id.profile_name);

            homeButton = itemView.findViewById(R.id.homeButton);
            homeButton.setOnClickListener(this);

            notificationButton = itemView.findViewById(R.id.notificationButton);
            notificationButton.setOnClickListener(this);

            binocularButton = itemView.findViewById(R.id.binocularButton);
            binocularButton.setOnClickListener(this);

            this.itemView.setOnClickListener(this);
        }

        void bind(User user, boolean following, boolean notify) {

            mUser = user;

            // Load the users' name.
            if (user.getName() != null) {
                profileName.setText(user.getName());
            } else {
                profileName.setText(itemView.getResources().getText(R.string.cd_profile_picture));
            }

            // Load the users' profile picture.
            if (user.getPhoto() != null && user.getPhoto().length() > 0) {
                DrawableTransitionOptions transitionOptions = new DrawableTransitionOptions().crossFade();
                RequestOptions options = new RequestOptions().placeholder(Config.ProfilePlaceholder);

                Glide.with(itemView.getContext())
                        .load(user.getPhoto())
                        .transition(transitionOptions)
                        .apply(options)
                        .into(profilePhotoView);
            } else {
                profilePhotoView.setImageResource(Config.ProfilePlaceholder);
            }

            // Load the users' following and notification button status.
            if (following) {
                homeButton.setColorFilter(getResources().getColor(R.color.colorPrimary));
            }
            if (notify) {
                notificationButton.setColorFilter(getResources().getColor(R.color.colorPrimary));
            }
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.itemView:
                    // open user activity
                    Intent intent = new Intent(getActivity(), AccountActivity.class);
                    intent.putExtra(AccountActivity.EXTRA_USER_ID, mUser.getId());
                    getActivity().startActivity(intent);
                    break;
                case R.id.homeButton:
                    DatabaseReference followRef = User.following(User.currentKey()).getRef();
                    DatabaseReference followerRef = User.followers(mUser.getId()).getRef();

                    // Add remove from following list of the logged in user.
                    followRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            boolean exists = false;

                            if (dataSnapshot.exists()) {
                                // If user exists remove entry.
                                if (dataSnapshot.child(mUser.getId()).exists()) {
                                    exists = true;
                                    followRef.child(mUser.getId()).removeValue();
                                    homeButton.setColorFilter(
                                            ContextCompat.getColor(itemView.getContext(), R.color.colorAccent),
                                            android.graphics.PorterDuff.Mode.SRC_IN);
                                }
                            }

                            // Else add the entry.
                            if (!exists) {
                                followRef.child(mUser.getId()).setValue(true);
                                homeButton.setColorFilter(
                                        ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary),
                                        android.graphics.PorterDuff.Mode.SRC_IN);
                            }

                            followRef.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                            Log.e(this.getClass().getName(), "Couldn't add/remove follow.");
                        }
                    });

                    // Add remove from the follower list of the following user.
                    followerRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            boolean exists = false;

                            if (dataSnapshot.exists()) {
                                // If user exists remove entry.
                                if (dataSnapshot.child(User.currentKey()).exists()) {
                                    exists = true;
                                    followerRef.child(User.currentKey()).removeValue();
                                }
                            }

                            if (!exists) {
                                followerRef.child(User.currentKey()).setValue(true);
                            }

                            followerRef.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(this.getClass().getName(), "Couldn't add/remove follower.");
                        }
                    });

                    break;
                case R.id.binocularButton:
                    break;
                case R.id.notificationButton:
                    DatabaseReference notifyRef = User.notify(User.currentKey()).getRef();
                    notifyRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(this.getClass().getName(), "Couldn't add/remove notify.");
                        }
                    });

                    break;
            }
        }
    }
}

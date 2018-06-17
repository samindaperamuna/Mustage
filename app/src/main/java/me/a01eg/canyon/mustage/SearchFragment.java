package me.a01eg.canyon.mustage;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import me.a01eg.canyon.mustage.model.User;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment implements TextWatcher {

    private RecyclerView mList;
    private EditText mEditText;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_search, container, false);
        mList = view.findViewById(R.id.list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false);
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
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final SearchAdapter adapter = new SearchAdapter();

                        for (DataSnapshot item : dataSnapshot.getChildren()) {
                            User user = item.getValue(User.class);
                            user.setId(item.getKey());
                            adapter.mUsers.add(user);
                        }

                        mList.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
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

    // Methods

    private void handleSearch(final String query) {

        if (query == null || query.length() == 0) return;

        User.collection().orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final SearchAdapter adapter = new SearchAdapter();

                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    User user = item.getValue(User.class);

                    if (user != null && user.getName() != null) {
                        String childName = user.getName();

                        if (childName.toLowerCase().contains(query)) {
                            user.setId(item.getKey());
                            adapter.mUsers.add(user);
                        }
                    }
                }

                mList.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("E", databaseError.getMessage());
            }
        });
    }

    class SearchAdapter extends RecyclerView.Adapter<UserViewHolder> {
        ArrayList<User> mUsers = new ArrayList<>();

        @Override
        public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.search_item, parent, false);
            return new UserViewHolder(v);
        }

        @Override
        public void onBindViewHolder(UserViewHolder holder, int position) {
            User user = mUsers.get(position);
            holder.bind(user);
        }

        @Override
        public int getItemCount() {
            return mUsers.size();
        }
    }

    class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView profilePhotoView;
        final TextView profileName;
        User mUser;

        UserViewHolder(View itemView) {
            super(itemView);

            profilePhotoView = itemView.findViewById(R.id.profile_photo);
            profileName = itemView.findViewById(R.id.profile_name);

            this.itemView.setOnClickListener(this);
        }

        void bind(User user) {

            mUser = user;

            if (user.getName() != null) {
                profileName.setText(user.getName());
            } else {
                profileName.setText(itemView.getResources().getText(R.string.cd_profile_picture));
            }

            if (user.getPhoto() != null && user.getPhoto().length() > 0) {
                DrawableTransitionOptions transitionOptions = new DrawableTransitionOptions()
                        .crossFade();
                RequestOptions options = new RequestOptions()
                        .placeholder(Config.ProfilePlaceholder);

                Glide.with(itemView.getContext())
                        .load(user.getPhoto())
                        .transition(transitionOptions)
                        .apply(options)
                        .into(profilePhotoView);
            } else {
                profilePhotoView.setImageResource(Config.ProfilePlaceholder);
            }
        }

        @Override
        public void onClick(View v) {
            // open user activity
            Intent intent = new Intent(getActivity(), AccountActivity.class);
            intent.putExtra(AccountActivity.EXTRA_USER_ID, mUser.getId());
            getActivity().startActivity(intent);
        }

    }
}

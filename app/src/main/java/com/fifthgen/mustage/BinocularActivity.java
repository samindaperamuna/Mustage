package com.fifthgen.mustage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.fifthgen.mustage.adapters.MomentsAdapter;
import com.fifthgen.mustage.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BinocularActivity extends AppCompatActivity implements ValueEventListener {

    // Users followings database reference.
    private DatabaseReference mUserRef;

    private List<DatabaseReference> mFollowingRefs;

    private List<User> users;
    private RecyclerView momentsRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binocular);

        // Get RecyclerView ref.
        momentsRecycler = findViewById(R.id.momentsRecycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        momentsRecycler.setLayoutManager(layoutManager);

        users = new ArrayList<>();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mUserRef = User.following(User.currentKey());
        mUserRef.addValueEventListener(this);
    }

    @Override
    protected void onStop() {
        if (mUserRef != null) {
            mUserRef.removeEventListener(this);
            mUserRef = null;
        }

        if (mFollowingRefs != null) {
            for (DatabaseReference ref : mFollowingRefs) {
                ref.removeEventListener(this);
            }

            mFollowingRefs = null;
        }

        super.onStop();
    }

    private void loadBinocular() {
        if (users != null && !users.isEmpty()) {
            MomentsAdapter adapter = new MomentsAdapter(R.layout.moments_item, users);
            momentsRecycler.setHasFixedSize(true);
            momentsRecycler.setAdapter(adapter);
        }
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists()) {
            DatabaseReference ref = dataSnapshot.getRef();

            if (mUserRef != null && ref.equals(mUserRef)) {
                // Get users followings and load their details.
                GenericTypeIndicator<HashMap<String, Boolean>> userType = new GenericTypeIndicator<HashMap<String, Boolean>>() {
                };
                HashMap<String, Boolean> followings = dataSnapshot.getValue(userType);

                if (followings != null && !followings.isEmpty()) {
                    mFollowingRefs = new ArrayList<>();

                    for (Map.Entry<String, Boolean> entry : followings.entrySet()) {
                        if (entry.getValue()) {
                            DatabaseReference userRef = User.collection(entry.getKey());
                            userRef.addValueEventListener(this);
                            mFollowingRefs.add(userRef);
                        }
                    }
                }
            } else if (mFollowingRefs != null && !mFollowingRefs.isEmpty()) {

                for (DatabaseReference userRef : mFollowingRefs) {
                    if (ref.equals(userRef)) {
                        User user = dataSnapshot.getValue(User.class);
                        users.remove(user);
                        users.add(user);
                    }
                }

                // Load binocular view.
                loadBinocular();
            }
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
        Log.e("Fetch Moments", "An error occurred: " + databaseError.getMessage());
    }
}

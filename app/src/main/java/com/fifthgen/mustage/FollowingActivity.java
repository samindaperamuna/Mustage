package com.fifthgen.mustage;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.fifthgen.mustage.adapters.FollowingViewHolder;
import com.fifthgen.mustage.adapters.PaginationAdapter;
import com.fifthgen.mustage.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Query;

public class FollowingActivity extends AppCompatActivity {

    private String userID;
    private RecyclerView recyclerView;
    private PaginationAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userID = FirebaseAuth.getInstance().getUid();
        showFollowing();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    private void showFollowing() {
        if (mAdapter == null) {
            if (userID != null) {
                Query ref = User.following(userID).orderByKey();

                mAdapter = new PaginationAdapter<FollowingViewHolder>(FollowingViewHolder.class, R.layout.following_item, ref) {
                    @Override
                    protected void populateViewHolder(FollowingViewHolder viewHolder, final DataSnapshot snapshot, int position) {
                        viewHolder.bindRef(snapshot.getKey());
                    }
                };

                recyclerView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
            }
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }
}

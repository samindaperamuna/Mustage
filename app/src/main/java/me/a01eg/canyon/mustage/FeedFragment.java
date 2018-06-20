package me.a01eg.canyon.mustage;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import me.a01eg.canyon.mustage.adapters.PaginationAdapter;
import me.a01eg.canyon.mustage.adapters.StoryViewHolder;
import me.a01eg.canyon.mustage.model.Story;
import me.a01eg.canyon.mustage.model.User;

public class FeedFragment extends Fragment {

    public static final String EXTRA_STORY_ID = "feed.storyId";
    private RecyclerView mList;
    private RecyclerView.Adapter mAdapter;
    private View emptyView;
    private RecyclerView.AdapterDataObserver emptyObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            checkIfEmpty();
        }

        @Override
        public void onChanged() {
            super.onChanged();
            checkIfEmpty();
        }
    };

    public FeedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param storyId For single story.
     * @return A new instance of fragment FeedFragment.
     */
    public static FeedFragment newInstance(String storyId) {
        FeedFragment fragment = new FeedFragment();

        if (storyId != null) {
            Bundle args = new Bundle();
            args.putString(EXTRA_STORY_ID, storyId);
            fragment.setArguments(args);
        }

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        mList = view.findViewById(R.id.list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mList.setLayoutManager(mLayoutManager);
        mList.setItemAnimator(new DefaultItemAnimator());

        emptyView = view.findViewById(R.id.empty);

        AdView mAdView = view.findViewById(R.id.adView);

        if (Config.isAdmobEnabled()) {
            MobileAds.initialize(getActivity(), BuildConfig.ADMOB_APP_ID);
            mAdView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
            Analytics.trackAdmobLoading(BuildConfig.ADMOB_APP_ID);
        } else {
            mAdView.setVisibility(View.GONE);
        }

        if (getArguments() != null) {
            String storyID = getArguments().getString(EXTRA_STORY_ID);
            showStory(storyID);
        } else {
            showFeed();
        }

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);

        // Inflate the layout for this fragment
        return view;
    }

    private void showStory(String id) {
        if (mAdapter == null) {
            Analytics.trackOpenStory(id);
            Query ref = FirebaseDatabase.getInstance().getReference(Const.kDataPostKey)
                    .orderByKey().equalTo(id);

            mAdapter = new PaginationAdapter<StoryViewHolder>(StoryViewHolder.class, R.layout.story_item, ref) {
                @Override
                protected void populateViewHolder(StoryViewHolder viewHolder, DataSnapshot snapshot, int position) {
                    // key is a storyId, same as in posts collection
                    viewHolder.bindStory(snapshot.getKey());
                }
            };
            mList.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            mAdapter.registerAdapterDataObserver(emptyObserver);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    @SuppressLint("InvalidAnalyticsName")
    private void showFeed() {
        if (mAdapter == null) {

            if (User.current() != null) {
                Query ref;

                if (Config.isPersonalFeed()) {
                    // show own stories + stories of users we following
                    ref = Story.feed(User.current().getKey()).orderByKey();
                    Analytics.trackPersonalFeed(User.current().getKey());
                } else {
                    // show all stories posted from all users
                    ref = Story.recent().orderByKey();
                    Analytics.trackRecentFeed();
                }

                mAdapter = new PaginationAdapter<StoryViewHolder>(StoryViewHolder.class, R.layout.story_item, ref) {
                    @Override
                    protected void populateViewHolder(StoryViewHolder viewHolder, DataSnapshot snapshot, int position) {
                        // key is a storyId, same as in posts collection
                        viewHolder.bindStory(snapshot.getKey());
                    }
                };

                mList.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
                mAdapter.registerAdapterDataObserver(emptyObserver);
            }
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    void checkIfEmpty() {
        if (mAdapter != null && emptyView != null) {

            if (mAdapter.getItemCount() == 0) {
                emptyView.setVisibility(View.VISIBLE);
                mList.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                mList.setVisibility(View.VISIBLE);
            }
        }
    }
}

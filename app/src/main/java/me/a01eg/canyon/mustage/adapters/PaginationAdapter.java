package me.a01eg.canyon.mustage.adapters;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import me.a01eg.canyon.mustage.Config;

/**
 * Created on 05/05/2017.
 * Copyright by 01eg.me
 */

public abstract class PaginationAdapter<T extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<T>
        implements ChildEventListener, ValueEventListener {

    private final int mLayout;
    private final Query mQuery;
    private final Class<T> mViewHolderClass;
    private final List<DataSnapshot> mItems = new ArrayList<>();
    private String mLatestKey = null;
    private Boolean mCanLoadMore = false;
    private Boolean mLoadingMore = false;
    private int pagination;

    public PaginationAdapter(Class<T> viewHolderClass, @LayoutRes int modelLayout, Query ref) {
        mViewHolderClass = viewHolderClass;
        mLayout = modelLayout;
        mQuery = ref;
        pagination = Config.getPagination();
        loadData(pagination);
    }

    /* BaseAdapter */

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public long getItemId(int position) {
        return mItems.get(position).getKey().hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return mLayout;
    }

    @Override
    public T onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        try {
            Constructor<T> constructor = mViewHolderClass.getConstructor(View.class);
            return constructor.newInstance(view);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onBindViewHolder(T holder, int position) {

        // if the latest view is requested, check we can load more
        if (mItems.size() <= position + 1 && mCanLoadMore && !mLoadingMore) {
            // load more
            loadMore(pagination);
            Log.d("", "New load more");
        }

        DataSnapshot model = mItems.get(position);
        populateViewHolder(holder, model, position);
    }

    /* ChildEventListener */

    @Override
    public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
        int index = 0;

        if (previousChildKey != null) {
            index = getIndexForKey(previousChildKey);
        }

        mItems.add(index, snapshot);
        int count = mItems.size();
        mLatestKey = mItems.get(count - 1).getKey();

        // we can load more items only if all items loaded
        mCanLoadMore = (count == pagination);
        notifyItemInserted(index);
    }

    @Override
    public void onChildChanged(DataSnapshot snapshot, String previousChildKey) {
        int index = getIndexForKey(snapshot.getKey());
        mItems.set(index, snapshot);
        notifyItemChanged(index);
    }

    @Override
    public void onChildRemoved(DataSnapshot snapshot) {
        int index = getIndexForKey(snapshot.getKey());
        mItems.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public void onChildMoved(DataSnapshot snapshot, String previousChildKey) {
        int oldIndex = getIndexForKey(snapshot.getKey());
        mItems.remove(oldIndex);
        int newIndex = previousChildKey == null ? 0 : (getIndexForKey(previousChildKey) + 1);
        mItems.add(newIndex, snapshot);
        notifyItemMoved(newIndex, oldIndex);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        notifyDataSetChanged();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    /*  Pagination Adapter */

    /**
     * Each time the data at the given Firebase location changes,
     * this method will be called for each item that needs to be displayed.
     * The first two arguments correspond to the mLayout and mModelClass given to the constructor of
     * this class. The third argument is the item's position in the list.
     * <p>
     * Your implementation should populate the view using the data contained in the model.
     *
     * @param viewHolder The view to populate
     * @param snapshot   The object containing the data used to populate the view
     * @param position   The position in the list of the view being populated
     */
    protected abstract void populateViewHolder(T viewHolder, DataSnapshot snapshot, int position);

    /* Helper methods */

    private void loadData(final int pagination) {
        // load current
        mQuery.addValueEventListener(this);
        mQuery.limitToLast(pagination).addChildEventListener(this);
    }

    private void loadMore(final int pagination) {
        mLoadingMore = true;

        // if no more new items, lets stop
        mQuery.endAt(mLatestKey)
                .limitToLast(pagination)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        int newItemsAdded = 0;
                        int lastIndex = mItems.size();

                        for (DataSnapshot item : snapshot.getChildren()) {

                            if (mLatestKey.equalsIgnoreCase(item.getKey())) continue;

                            // add to the end of list
                            mItems.add(lastIndex, item);
                            newItemsAdded++;
                        }

                        // and update latest key
                        mLatestKey = mItems.get(mItems.size() - 1).getKey();

                        // we can continue loading items is the page is full
                        mCanLoadMore = (newItemsAdded == pagination - 1);
                        mLoadingMore = false;
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // ..
                    }
                });
    }

    private int getIndexForKey(String key) {
        int index = 0;
        for (DataSnapshot snapshot : mItems) {
            if (snapshot.getKey().equals(key)) {
                return index;
            } else {
                index++;
            }
        }
        throw new IllegalArgumentException("Key not found");
    }

    public void cleanup() {
        mQuery.removeEventListener((ValueEventListener) this);
        mQuery.removeEventListener((ChildEventListener) this);
    }
}

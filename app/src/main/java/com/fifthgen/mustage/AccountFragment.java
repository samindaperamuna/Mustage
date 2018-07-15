package com.fifthgen.mustage;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.fifthgen.mustage.adapters.PaginationAdapter;
import com.fifthgen.mustage.adapters.PreviewViewHolder;
import com.fifthgen.mustage.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment implements ValueEventListener {

    public static final String EXTRA_USER_ID = "mustage.userID";

    private RecyclerView mList;
    private PaginationAdapter mAdapter;
    private String userID;
    private User mUser = null;
    private CollapsingToolbarLayout mToolbarContainer;

    private MenuItem followItem;
    private MenuItem unfollowItem;
    private ImageView profileImage;
    private boolean mIsFollowed = false;

    public AccountFragment() {
        // Required empty public constructor
        userID = FirebaseAuth.getInstance().getUid();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        mToolbarContainer = view.findViewById(R.id.toolbar_layout);

        profileImage = view.findViewById(R.id.bgImage);
        mList = view.findViewById(R.id.list);

        final Intent intent = getActivity().getIntent();

        if (intent != null) {
            userID = intent.getStringExtra(EXTRA_USER_ID);
        }

        if (userID == null) {
            userID = FirebaseAuth.getInstance().getUid();
        }

        if (userID != null) {
            User.collection(userID).addValueEventListener(this);
            RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 3);
            mList.setLayoutManager(mLayoutManager);
            mList.setItemAnimator(new DefaultItemAnimator());
        }

        showFeed();

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (userID != null) {
            User.collection(userID).removeEventListener(this);
        }
    }

    public User getUser() {
        return mUser;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case MediaUtils.REQUEST_PROFILE_CAPTURE: {
                    final UploadTask task = MediaUtils.handleImageCapture(data);
                    updatePhoto(task);
                }

                case MediaUtils.REQUEST_PROFILE_PICK: {
                    final UploadTask uploadTask = MediaUtils.handleImagePick(getActivity(), data);
                    updatePhoto(uploadTask);
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        String currentId = User.current() != null ? User.current().getKey() : "";

        // if user is same as current, show 'edit' menu
        if (userID.equalsIgnoreCase(currentId)) {
            inflater.inflate(R.menu.menu_account, menu);
        } else {
            inflater.inflate(R.menu.menu_users, menu);

            followItem = menu.findItem(R.id.action_follow);
            unfollowItem = menu.findItem(R.id.action_unfollow);

            followItem.setVisible(!mIsFollowed);
            unfollowItem.setVisible(mIsFollowed);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // action enabled only when user loaded
        if (mUser != null) {

            switch (item.getItemId()) {
                case R.id.action_rename: {
                    showRenameDialog();
                    break;
                }

                case R.id.action_photo: {
                    changeProfilePhoto();
                    break;
                }

                case R.id.action_follow:
                    // follow
                    startFollowing();
                    break;

                case R.id.action_unfollow:
                    // unfollow
                    stopFollowing();
                    break;

                case R.id.action_message:
                    // send direct message
                    sendDirectMessage();
                    break;

                case R.id.action_logout: {
                    logout();
                    break;
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendDirectMessage() {
        MessagesActivity.start(getActivity(), mUser);
    }

    private void stopFollowing() {
        final String currentID = User.current().getKey();
        User.following(currentID).child(userID).removeValue();
        User.followers(userID).child(currentID).removeValue();
    }

    private void startFollowing() {
        final String currentID = User.current().getKey();
        User.following(currentID).child(userID).setValue(true);
        User.followers(userID).child(currentID).setValue(true);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    /* ValueEventListener */

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        // don't not anything if destroyed
        if (getActivity().isDestroyed()) return;

        final String currentID = User.currentKey();
        User user = dataSnapshot.getValue(User.class);
        user.setId(dataSnapshot.getKey());
        mUser = user;

        if (user.getName() != null) {
            mToolbarContainer.setTitle(user.getName());
        }

        if (user.getPhoto() != null) {
            Glide.with(this).load(user.getPhoto())
                    .into(profileImage);
        }

        mIsFollowed = dataSnapshot.child(Const.kFollowersKey).hasChild(currentID);

        if (followItem != null) {
            followItem.setVisible(!mIsFollowed);
            unfollowItem.setVisible(mIsFollowed);
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
    }

    private void showFeed() {
        if (mAdapter == null) {
            if (userID != null) {
                Query ref = User.uploads(userID).orderByKey();

                mAdapter = new PaginationAdapter<PreviewViewHolder>(PreviewViewHolder.class,
                        R.layout.preview_item, ref) {
                    @Override
                    protected void populateViewHolder(PreviewViewHolder viewHolder, final DataSnapshot snapshot, int position) {
                        // key is a storyId, same as in posts collection
                        viewHolder.bindStory(snapshot.getKey());
                    }
                };

                mList.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
            }
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    /* Helpers */

    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Logout");
        builder.setMessage("Do you want to log out on this device?");
        builder.setPositiveButton("Cancel", null);
        builder.setNegativeButton("Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Analytics.trackUserLogout();
                // sign out from current user
                AuthManager.getInstance().logout();
                // navigate to login screen
                AuthManager.getInstance().authorizeIfNeeded(getActivity());
            }
        });
        builder.show();
    }

    private void changeProfilePhoto() {
        if (!MediaUtils.openLibrary(getActivity(), MediaUtils.REQUEST_PROFILE_PICK)) {
            MediaUtils.openCamera(getActivity(), MediaUtils.REQUEST_PROFILE_CAPTURE);
        }
    }

    @SuppressLint("InflateParams")
    private void showRenameDialog() {
        final AlertDialog builder = new AlertDialog.Builder(getActivity())
                .setPositiveButton(getString(R.string.dialog_rename), null)
                .setNegativeButton(getString(R.string.dialog_cancel), null)
                .create();

        View dialog = LayoutInflater.from(getActivity()).inflate(R.layout.rename_dialog, null);
        final EditText etNickName = dialog.findViewById(android.R.id.edit);
        etNickName.setText(mUser.getName());

        builder.setView(dialog);
        builder.setTitle(getString(R.string.dialog_rename_title));
        builder.setMessage(getString(R.string.dialog_rename_content));

        builder.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button btnAccept = builder.getButton(AlertDialog.BUTTON_POSITIVE);
                btnAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!etNickName.getText().toString().isEmpty()) {
                            mUser.setName(etNickName.getText().toString());
                            User.current().child("name").setValue(mUser.getName());
                        }

                        builder.dismiss();
                    }
                });

                final Button btnDecline = builder.getButton(DialogInterface.BUTTON_NEGATIVE);
                btnDecline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        builder.dismiss();
                    }
                });
            }
        });

        /* Show the dialog */
        builder.show();
    }

    public void updatePhoto(UploadTask task) {
        if (task != null) {
            task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    String url = downloadUrl.toString();

                    getUser().setPhoto(url);
                    User.current().child("photo").setValue(url);
                }
            });
        }
    }
}

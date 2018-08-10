package com.fifthgen.mustage;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.fifthgen.mustage.model.Story;
import com.fifthgen.mustage.model.Tag;
import com.fifthgen.mustage.views.BottomNavigationViewHelper;
import com.google.firebase.perf.metrics.AddTrace;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private FirebaseRemoteConfig config;
    private int saveState = 0;
    private BottomNavigationView navigation;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            if (item.getItemId() == navigation.getSelectedItemId()) {
                return false;
            }

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    showMainFeed();
                    break;
                case R.id.navigation_search:
                    showSearch();
                    break;
                case R.id.navigation_camera:
                    openCamera();
                    break;
                case R.id.navigation_messages:
                    showMessages();
                    break;
                case R.id.navigation_profile:
                    showProfile();
                    break;
                default:
                    return false;
            }

            return true;
        }
    };

    private void showSearch() {
        SearchFragment fragment = new SearchFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }

    private void showMessages() {
        ChatFragment fragment = new ChatFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }

    private void showProfile() {
        // show user feed
        AccountFragment fragment = new AccountFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }

    private void showMainFeed() {
        // show user feed
        FeedFragment fragment = FeedFragment.newInstance(null);
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }

    private void loadConfigs() {
        config = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings settings = new FirebaseRemoteConfigSettings.Builder().build();
        config.setConfigSettings(settings);
        config.setDefaults(R.xml.config_defaults);
        config.fetch(0).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                config.activateFetched();
            }

            Analytics.trackRemoteConfigLoad(task.isSuccessful());
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // need to use application context, to keep it alive
        Analytics.init(getApplicationContext());
        loadConfigs();

        navigation = findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        if (!AuthManager.getInstance().authorizeIfNeeded(this)
                && savedInstanceState == null) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!BuildConfig.DEBUG) { // collection crashes only for release version
            Analytics.trackCrashlogEnabled();
        }

        if (saveState != 0) {
            navigation.setSelectedItemId(saveState);
        } else {
            showMainFeed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        saveState = navigation.getSelectedItemId();
    }

    @SuppressWarnings("VisibleForTests")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // send result to fragment
        Fragment fragment = getFragmentManager().findFragmentById(R.id.content_frame);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }

        switch (requestCode) {
            case MediaUtils.REQUEST_IMAGE_CAPTURE: {
                if (resultCode == RESULT_OK) {
                    final UploadTask uploadTask = MediaUtils.handleImageCapture(data);
                    Story.uploadImageStory(uploadTask, "", null);
                    showUploadingProgress(uploadTask);
                    showMainFeed();
                }
                break;
            }

            case MediaUtils.REQUEST_IMAGE_PICK: {
                if (resultCode == RESULT_OK) {
                    final UploadTask uploadTask = MediaUtils.handleImagePick(this, data);
                    Story.uploadImageStory(uploadTask, "", null);
                    showUploadingProgress(uploadTask);
                    showMainFeed();
                }
                break;
            }

            case CameraActivity.TAG_CAMERA_ACTION: {
                if (resultCode == RESULT_OK) {
                    final UploadTask uploadTask = MediaUtils.handleImagePick(this, data);
                    String description = data.getStringExtra(CameraActivity.ARG_DESCRIPTION);
                    List<Tag> tags = (ArrayList<Tag>) data.getSerializableExtra(CameraActivity.ARG_TAGS);
                    List<String> stringTags = new ArrayList<>();
                    for (Tag tag : tags) {
                        Object id = tag.getId();
                        if (id != null)
                            stringTags.add(id.toString());
                    }

                    Story.uploadImageStory(uploadTask, description, stringTags);
                    showUploadingProgress(uploadTask);

                    showMainFeed();
                }
                break;
            }

            case AuthManager.REQUEST_AUTH_CODE: {
                if (resultCode == RESULT_OK) {
                    AuthManager.getInstance().completeAuth(this);
                } else {
                    // error
                }
                break;
            }
        }
    }

    private void showUploadingProgress(final UploadTask uploadTask) {
        View view = navigation;
        Snackbar.make(view, "Uploading..", Snackbar.LENGTH_SHORT)
                .setAction("Cancel", v -> uploadTask.cancel()).show();
    }

    @AddTrace(name = "openCamera")
    private void openCamera() {
        Analytics.trackOpenCamera();

//        if (BuildConfig.DEBUG) {
//            MediaUtils.openLibrary(this, MediaUtils.REQUEST_IMAGE_PICK);
//        } else {
//            MediaUtils.openCamera(this, MediaUtils.REQUEST_IMAGE_CAPTURE);
//        }

        // Load custom camera activity.
        startActivityForResult(new Intent(this, CameraActivity.class), CameraActivity.TAG_CAMERA_ACTION);
    }
}

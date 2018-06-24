package me.a01eg.canyon.mustage;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.perf.metrics.AddTrace;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.UploadTask;

import me.a01eg.canyon.mustage.model.Story;
import me.a01eg.canyon.mustage.views.BottomNavigationViewHelper;

public class HomeActivity extends AppCompatActivity {

    private FirebaseRemoteConfig config;
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

    private int saveState = 0;
    private BottomNavigationView navigation;

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
        FirebaseRemoteConfigSettings settings = new FirebaseRemoteConfigSettings.Builder()
                .build();
        config.setConfigSettings(settings);
        config.setDefaults(R.xml.config_defaults);
        config.fetch(0).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    config.activateFetched();
                }

                Analytics.trackRemoteConfigLoad(task.isSuccessful());
            }
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
                    Story.uploadImageStory(uploadTask);
                    showUploadingProgress(uploadTask);
                    showMainFeed();
                }
                break;
            }

            case MediaUtils.REQUEST_IMAGE_PICK: {
                if (resultCode == RESULT_OK) {
                    final UploadTask uploadTask = MediaUtils.handleImagePick(this, data);
                    Story.uploadImageStory(uploadTask);
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
                .setAction("Cancel", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        uploadTask.cancel();
                    }
                }).show();
    }

    @AddTrace(name = "openCamera")
    private void openCamera() {
        Analytics.trackOpenCamera();

//        if (BuildConfig.DEBUG) {
//            MediaUtils.openLibrary(this, MediaUtils.REQUEST_IMAGE_PICK);
//        } else {
            MediaUtils.openCamera(this, MediaUtils.REQUEST_IMAGE_CAPTURE);
//        }
    }
}

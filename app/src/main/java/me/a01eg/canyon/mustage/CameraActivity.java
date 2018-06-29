package me.a01eg.canyon.mustage;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.tylersuehr.chips.Chip;
import com.tylersuehr.chips.ChipsInputLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import me.a01eg.canyon.mustage.model.Tag;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener, ValueEventListener {

    public static final int TAG_CAMERA_ACTION = 2356;
    public static final String ARG_DESCRIPTION = "description";
    public static final String ARG_TAGS = "tags";

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final String TAG = CameraActivity.class.getName();
    private static String CAM_ACCESS_ERR_MSG = "Couldn't access device camera.";

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private EditText descriptionText;
    private ChipsInputLayout chipsInput;
    private DatabaseReference mTagsRef;
    private TextureView textureView;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    // Texture listener instance.
    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };
    private ConstraintLayout overlayView;
    private File file;
    private Handler mBackgroundHandler;
    // State callback instance.
    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            CameraActivity.this.cameraDevice = cameraDevice;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            CameraActivity.this.cameraDevice = null;
        }
    };
    private HandlerThread mBackgroundThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        textureView = findViewById(R.id.textureView);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);
        Button btnRed = findViewById(R.id.buttonRed);
        btnRed.setOnClickListener(this);
        Button btnGreen = findViewById(R.id.buttonGreen);
        btnGreen.setOnClickListener(this);
        Button btnOrange = findViewById(R.id.buttonOrange);
        btnOrange.setOnClickListener(this);
        Button btnBlue = findViewById(R.id.buttonBlue);
        btnBlue.setOnClickListener(this);
        chipsInput = findViewById(R.id.chipsInput);
        descriptionText = findViewById(R.id.descriptionText);
        Button uploadButton = findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(this);
        overlayView = findViewById(R.id.overlayView);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mTagsRef = Tag.collection();
        mTagsRef.addValueEventListener(this);
    }

    @Override
    protected void onStop() {
        if (mTagsRef != null) {
            mTagsRef.removeEventListener(this);
            mTagsRef = null;
        }

        super.onStop();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonRed:
                takePicture();
                addTag("red");
                break;
            case R.id.buttonGreen:
                takePicture();
                addTag("green");
                break;
            case R.id.buttonOrange:
                takePicture();
                addTag("orange");
                break;
            case R.id.buttonBlue:
                takePicture();
                addTag("blue");
                break;
            case R.id.uploadButton:
                uploadStory();
                break;
        }
    }

    private void addTag(String color) {
        Chip addedChip = null;

        for (Chip chip : chipsInput.getFilteredChips()) {
            Tag tag = (Tag) chip;
            if (tag.getColor().equals(color)) {
                addedChip = chip;
                break;
            }
        }

        if (addedChip != null) {
            chipsInput.getOriginalFilterableChips().remove(addedChip);
            chipsInput.getFilteredChips().remove(addedChip);
            chipsInput.addSelectedChip(addedChip);
        }
    }

    private void uploadStory() {
        List<Tag> tags = new ArrayList<>();

        for (Chip chip : chipsInput.getSelectedChips()) {
            tags.add((Tag) chip);
        }

        String description = descriptionText.getText().toString();

        Intent postIntent = new Intent(this, HomeActivity.class);
        postIntent.setData(Uri.fromFile(file));
        postIntent.putExtra(ARG_DESCRIPTION, description);
        postIntent.putExtra(ARG_TAGS, (Serializable) tags);
        setResult(Activity.RESULT_OK, postIntent);
        finish();
    }

    private void takePicture() {
        if (cameraDevice == null)
            return;

        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        if (manager == null) return;

        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes;
            StreamConfigurationMap key = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            if (key == null) return;

            jpegSizes = key.getOutputSizes(ImageFormat.JPEG);

            // Capture image with custom size
            int height = 480, width = 640;
            if (jpegSizes != null && jpegSizes.length > 0) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }

            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurface = new ArrayList<>(2);
            outputSurface.add(reader.getSurface());
            outputSurface.add(new Surface(textureView.getSurfaceTexture()));

            CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            // Check orientation.
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            file = new File(this.getCacheDir() + "/" + UUID.randomUUID().toString() + ".jpg");
            ImageReader.OnImageAvailableListener imageListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {

                    try (Image image = reader.acquireLatestImage()) {
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, "File not found.");
                    } catch (IOException e) {
                        Log.e(TAG, "I/O Error." + e.getLocalizedMessage());
                    }
                }

                private void save(byte[] bytes) throws IOException {
                    try (OutputStream out = new FileOutputStream(file)) {
                        out.write(bytes);
                    }
                }
            };

            reader.setOnImageAvailableListener(imageListener, mBackgroundHandler);
            CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(CameraActivity.this, "Image Captured", Toast.LENGTH_SHORT).show();
                }
            };

            cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try {
                        cameraCaptureSession.capture(captureBuilder.build(), captureCallback, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        Toast.makeText(CameraActivity.this, CAM_ACCESS_ERR_MSG, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, CAM_ACCESS_ERR_MSG + "\n" + e.getLocalizedMessage());
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            }, mBackgroundHandler);

            // Hide the overlay.
            overlayView.setVisibility(View.INVISIBLE);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if (cameraDevice == null)
                        return;

                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            }, null);
        } catch (CameraAccessException e) {
            Toast.makeText(this, CAM_ACCESS_ERR_MSG, Toast.LENGTH_SHORT).show();
        }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        if (manager == null) return;

        try {
            String cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];

            // Check real-time permissions if run on APIs higher than 23.
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_CAMERA_PERMISSION);

                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        if (cameraDevice == null) {
            Toast.makeText(CameraActivity.this, CAM_ACCESS_ERR_MSG, Toast.LENGTH_SHORT).show();
            return;
        }

        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);

        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, CAM_ACCESS_ERR_MSG + "\n" + e.getLocalizedMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "You can't use camera without setting the permissions.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();

        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();

        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
        } catch (InterruptedException e) {
            Log.e(TAG, "Couldn't close the thread : " + e.getLocalizedMessage());
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists()) {
            String ref = dataSnapshot.getRef().toString();

            if (mTagsRef != null && ref.equals(mTagsRef.toString())) {
                GenericTypeIndicator<HashMap<String, Tag>> lt = new GenericTypeIndicator<HashMap<String, Tag>>() {
                };

                HashMap<String, Tag> tags = dataSnapshot.getValue(lt);
                if (tags != null && !tags.isEmpty()) {
                    for (Map.Entry<String, Tag> entry : tags.entrySet()) {
                        entry.getValue().setId(entry.getKey());
                    }

                    chipsInput.setFilterableChipList(new ArrayList<>(tags.values()));
                }
            }
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
        Log.e(TAG, databaseError.getCode() + "\n" + databaseError.getMessage() + "\n" + databaseError.getDetails());
    }

    @Override
    public void onBackPressed() {
        // Pop the back stack (remove history).
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack();

        // Navigate to home activity
        NavUtils.navigateUpFromSameTask(this);
    }
}
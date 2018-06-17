package me.a01eg.canyon.mustage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created on 26/05/2017.
 * Copyright by 01eg.me
 */

class MediaUtils {

    private static final int REQUEST_CODE = 1001;
    public static final int REQUEST_IMAGE_CAPTURE = REQUEST_CODE + 1;
    public static final int REQUEST_IMAGE_PICK = REQUEST_CODE + 2;
    public static final int REQUEST_PROFILE_CAPTURE = REQUEST_CODE + 3;
    public static final int REQUEST_PROFILE_PICK = REQUEST_CODE + 4;

    public static void openCamera(Activity activity, int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(takePictureIntent, requestCode);
        }
    }

    public static boolean openLibrary(Activity activity, int requestCode) {
        // pick from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/* video/*");

        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(Intent.createChooser(intent, "Select Picture"), requestCode);
            return true;
        } else {
            return false;
        }
    }

    @Nullable
    public static UploadTask handleImageCapture(Intent intent) {

        Bundle extras = intent.getExtras();
        Bitmap imageBitmap = (Bitmap) extras.get("data");

        if (imageBitmap != null) {
            final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference imgref = storageRef.child(userId + "-" + System.currentTimeMillis() + ".jpg");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, Config.getJPEGImageQuality(), baos);
            byte[] bytes = baos.toByteArray();

            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpg")
                    .build();

            return imgref.putBytes(bytes, metadata);
        } else {
            return null;
        }
    }

    @Nullable
    public static UploadTask handleImagePick(Context context, Intent intent) {
        try {
            Uri selectedImage = intent.getData();

//            Uri selectedMediaUri = intent.getData();
//            if (selectedMediaUri.toString().contains("image")) {
//                //handle image
//            } else  if (selectedMediaUri.toString().contains("video")) {
//                //handle video
//            }

            InputStream is = context.getContentResolver().openInputStream(selectedImage);
            Bitmap imageBitmap = BitmapFactory.decodeStream(is);
            final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference imgref = storageRef.child(userId + "-" + System.currentTimeMillis() + ".jpg");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, Config.getJPEGImageQuality(), baos);
            byte[] bytes = baos.toByteArray();

            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpg")
                    .build();

            return imgref.putBytes(bytes, metadata);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}

package com.reja.chatapp.Utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.reja.chatapp.Interface.MediaPickListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MediaPicker {

    private static final int REQUEST_CODE_PICK_IMAGES = 1001;
    private static final int REQUEST_CODE_PICK_VIDEOS = 1002;
    private static final int REQUEST_CODE_PICK_PDF = 1003;
    private static final int REQUEST_CODE_CAMERA_PERMISSION = 1004;
    private static final int REQUEST_IMAGE_CAPTURE = 1005;

    private Uri imageUri;
    private Activity activity;
    private MediaPickListener mediaPickListener;

    public MediaPicker(Activity activity, MediaPickListener mediaPickListener) {
        this.activity = activity;
        this.mediaPickListener = mediaPickListener;
    }

    public void pickImages(int maxImageCount) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        activity.startActivityForResult(intent, REQUEST_CODE_PICK_IMAGES);
    }

    public void pickVideos() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*");
        activity.startActivityForResult(intent, REQUEST_CODE_PICK_VIDEOS);
    }

    public void pickPdf() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        activity.startActivityForResult(intent, REQUEST_CODE_PICK_PDF);
    }

    public void pickCamera() {
       startCamera();
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void startCamera() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "PNG_" + timeStamp + "_";
        String fileName = imageFileName+"png";
        // Create parameters for Intent with filename
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, fileName);
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image capture by camera");
        imageUri =
                activity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        activity.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "PNG_" + timeStamp + "_";
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        // Create the file in the specified directory
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".png",         /* suffix */
                storageDir      /* directory */
        );
        // Return the file
        return imageFile;
    }

    public void handleMediaPickerResult(int requestCode, int resultCode, Intent data) throws IOException {
        List<Uri> selectedMediaUris = new ArrayList<>(); // Declare selectedMediaUris before using it
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_PICK_IMAGES || requestCode == REQUEST_CODE_PICK_VIDEOS || requestCode == REQUEST_CODE_PICK_PDF) {
                if (data != null) {
                    if (data.getData() != null) {
                        selectedMediaUris.add(data.getData());
                    } else if (data.getClipData() != null) {
                        int count = data.getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            selectedMediaUris.add(data.getClipData().getItemAt(i).getUri());
                        }
                    }
                }
            } else if(requestCode == REQUEST_IMAGE_CAPTURE){
               if(imageUri!=null){
                   selectedMediaUris.add(imageUri);
               }
            }
        }
        // Notify the media pick listener with selectedMediaUris
        mediaPickListener.onMediaPicked(selectedMediaUris);
    }


}


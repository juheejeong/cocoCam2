/*
Copyright 2018 Google LLC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.google.android.imaging.pixelvisualcorecamera.common;

import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Utilities for working with the file system.
 */
public final class FileSystem {

  private static final String TAG = "PvcCamFiles";
  private static final String PICTURES_DIR_NAME = "cocoCam";

  /** The result returned by #saveImage. */
  public static class SaveImageResult {
    public final boolean success;

    public SaveImageResult(boolean success) {
      this.success = success;
    }
  }

  /** Constructs an filename for jpeg photos. */
  private static class OutputFileBuilder {
    private boolean api1;
    private boolean api2;

    OutputFileBuilder api1() {
      if (api2) {
        throw new IllegalStateException("Output file configured for API 1 and API 2");
      }
      api1 = true;
      return this;
    }

    OutputFileBuilder api2() {
      if (api1) {
        throw new IllegalStateException("Output file configured for API 1 and API 2");
      }
      api2 = true;
      return this;
    }

    File build() {
      File storageDir = getStorageDir();
      if (storageDir == null) {
        return null;
      }

      // Create a media file name
      StringBuilder imageFilePathBuilder = new StringBuilder(storageDir.getPath())
          .append(File.separator)
          .append("IMG_")
          .append(new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date()));
      if (api1) {
        imageFilePathBuilder.append("_1");
      } else if (api2) {
        imageFilePathBuilder.append("_2");
      }
      imageFilePathBuilder.append(".jpg");
      return new File(imageFilePathBuilder.toString());
    }
  }

  private static File getStorageDir() {//PICTURES_DIR_NAME
    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_PICTURES), PICTURES_DIR_NAME);

    // Create the storage directory if it does not exist
    if (!mediaStorageDir.exists()){
      if (!mediaStorageDir.mkdirs()){
        Log.w(TAG, "Failed to create output Pictures directory");
        return null;
      }
    }
    return mediaStorageDir;
  }

  /**
   * Saves an image to the file system. Should be called on a background thread.
   */
  public static SaveImageResult saveImage(Context context, Image image, boolean isApi1) {
    Log.i(TAG, String.format("saveImage format: %d, h: %d, w: %d, timestamp: %d",
        image.getFormat(), image.getHeight(), image.getWidth(), image.getTimestamp()));
    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
    return saveBytesToDiskAndReturnResult(context, buffer, isApi1);
  }

  /**
   * Saves an image to the file system. Should be called on a background thread.
   */
  public static SaveImageResult saveImage(Context context, byte[] data, boolean isApi1) {
    ByteBuffer buffer = ByteBuffer.wrap(data);
    return saveBytesToDiskAndReturnResult(context, buffer, isApi1);
  }

  public static SaveImageResult saveBytesToDiskAndReturnResult(
      Context context, ByteBuffer byteBuffer, boolean isApi1) {
    File outputFile = saveBytesToDisk(context, byteBuffer, isApi1);
    if (outputFile != null) {
      updateMediaStore(context, outputFile);

      return new SaveImageResult(/*success*/ true);
    }
    return new SaveImageResult(/*success*/ false);
  }

  public static File saveBytesToDisk(Context context, ByteBuffer byteBuffer, boolean isApi1) {
    byte[] bytes = new byte[byteBuffer.remaining()];
    byteBuffer.get(bytes);
    FileOutputStream output = null;
    File outputFile;
    try {
      OutputFileBuilder outputFileBuilder = new OutputFileBuilder();
      if (isApi1) {
        outputFileBuilder.api1();
      } else {
        outputFileBuilder.api2();
      }
      outputFile = outputFileBuilder.build();
      if (outputFile != null) {
        output = new FileOutputStream(outputFile);
        output.write(bytes);


          goSend(outputFile);

        Log.i(TAG,  "Wrote " + outputFile.getName());
        //Toasts.showToast(context, "Wrote " + outputFile.getName(), Toast.LENGTH_SHORT);
      }
    } catch (IOException e) {
      outputFile = null;
      Log.w(TAG, e);
    } finally {
      Log.i(TAG, "closing Image after saving");
      if (null != output) {
        try {
          output.close();
        } catch (IOException e) {
          outputFile = null;
          Log.w(TAG, e);
        }
      }
    }
    return outputFile;
  }

  /**
   * Notifies system there is a new media file, so that it appears in photo galleries immediately.
   */
  private static void updateMediaStore(Context context, File file) {
    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
    intent.setData(Uri.fromFile(file));
    context.sendBroadcast(intent);
  }



  // from Android to server
  public static void goSend(File output) throws IOException{

  OkHttpClient client = new OkHttpClient();

    RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "original_photo.jpg", RequestBody.create(MultipartBody.FORM, output))
            .build();

    Request request = new Request.Builder()
            .url("http://52.79.240.201/phps/upload.php")
            .post(requestBody)
            .build();

    Response response = client.newCall(request).execute();
    Log.d("response1", "uploadImage:" + response.body().string());

    client.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(@NotNull Call call, @NotNull IOException e) {
        e.printStackTrace();
        Log.d("TAG", "SURE ??");
      }

      @Override
      public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        Log.d("TEST2: ", response.body().string());
      }
    });
  }

  public static void getImage() {


  }

}



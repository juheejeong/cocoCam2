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

package com.google.android.imaging.pixelvisualcorecamera.api2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.imaging.pixelvisualcorecamera.R;
import com.google.android.imaging.pixelvisualcorecamera.common.FileSystem;
import com.google.android.imaging.pixelvisualcorecamera.common.Preferences;
import com.google.android.imaging.pixelvisualcorecamera.common.Toasts;
import com.google.android.imaging.pixelvisualcorecamera.common.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import travel.ithaka.android.horizontalpickerlib.PickerLayoutManager;

/**
 *  Primary activity for an API 2 camera.
 */
public class CameraApi2Activity extends Activity {

  private static final String TAG = "PvcCamApi2";

  private static final String CAMERA_FACING_BACK = "0";
  private static final String CAMERA_FACING_FRONT = "1";
  private static final String STATE_ZOOM = "zoom";
  private static int REQUEST_CODE = 1;


  private String cameraId;
  private HandlerThread backgroundThread;
  private Handler backgroundHandler;
  private Camera2Controller cameraController;
  private Preferences preferences;
  private Size outputSize;
  private ScaleGestureDetector zoomScaleGestureDetector;
  private ZoomScaleGestureListener zoomScaleGestureListener;
  private boolean resumed;
  private boolean cameraAcquired;
  PickerAdapter adapter;


  private final Camera2Controller.OnImageAvailableListener onImageAvailableListener =
      (image) -> FileSystem.saveImage(getApplicationContext(), image, /*isApi1*/ false);

  // ===============================================================================================
  // Activity Framework Callbacks
  // ===============================================================================================

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "[onCreate]");
    preferences = new Preferences(this);
    setContentView(R.layout.camera2);
    AutoFitTextureView textureView = findViewById(R.id.camera_preview);
    Utils.setSystemUiOptionsForFullscreen(this);

    ImageButton galleryButton = findViewById(R.id.galleryButton);
    galleryButton.setOnClickListener(galleryOnClickListener);

    Button captureButton = findViewById(R.id.button_capture);

    captureButton.setOnClickListener(cameraCaptureOnClickListener);
    //Button doubleShotButton = findViewById(R.id.doubleshot_button);
    //doubleShotButton.setVisibility(View.VISIBLE);
    //doubleShotButton.setOnClickListener(v -> cameraController.takeDoubleShot());

    //ImageButton galleryButton = (ImageButton) findViewById(R.id.galleryButton);
    //galleryButton.setOnClickListener(v ->

//        Intent intent = new Intent(Intent.ACTION_PICK);
//        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
//        startActivityForResult(intent, 1);

   // );

    cameraController = new Camera2Controller(
        getApplicationContext(),
        getWindowManager().getDefaultDisplay().getRotation(),
        captureButton,
        textureView,
        onImageAvailableListener);

    initTopControls();
    configureOutputSize();

    zoomScaleGestureListener = new ZoomScaleGestureListener(
        cameraController, findViewById(R.id.zoom_level_label), STATE_ZOOM);
    zoomScaleGestureListener.restoreInstanceState(savedInstanceState);
    zoomScaleGestureDetector = new ScaleGestureDetector(this, zoomScaleGestureListener);


    /**
     * Zoom Control Part
     */
    RecyclerView rv = findViewById(R.id.rv);
    PickerLayoutManager pickerLayoutManager = new PickerLayoutManager(this, PickerLayoutManager.HORIZONTAL, false);
    pickerLayoutManager.setChangeAlpha(true);
    pickerLayoutManager.setScaleDownBy(0.99f);
    pickerLayoutManager.setScaleDownDistance(0.8f);

    adapter = new PickerAdapter(this, getData(5), rv);
    SnapHelper snapHelper = new LinearSnapHelper();
    snapHelper.attachToRecyclerView(rv);
    rv.setLayoutManager(pickerLayoutManager);
    rv.setAdapter(adapter);

    pickerLayoutManager.setOnScrollStopListener(new PickerLayoutManager.onScrollStopListener() {
      @Override
      public void selectedView(View view) {

        String temp = ((TextView) view).getText().toString();
        int real = Integer.parseInt(temp);

        Animation startAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink_animation);


        switch(real){
          case 28:
            cameraController.setZoom(1);
                break;

          case 35:
            textureView.startAnimation(startAnimation);
            textureView.clearAnimation();//깜빡이는 애니메이션 시도중(미완)
            cameraController.setZoom(1.25);
                break;

            case 50:
            cameraController.setZoom(2);
                break;

            case 85:
            cameraController.setZoom(3);
                break;

            case 120:
            cameraController.setZoom(4);
                break;

            default:
                break;

        }
      }
    });
    rv.setLayoutManager(pickerLayoutManager);
  }

  //Add zoom levels
  public List<String> getData(int count) {
    List<String> data = new ArrayList<>();
    data.add(String.valueOf(28));
    data.add(String.valueOf(35));
    data.add(String.valueOf(50));
    data.add(String.valueOf(85));
    data.add(String.valueOf(120));

    return data;
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    Log.d(TAG, "[onWindowFocusChanged] hasFocus: " + hasFocus);
    if (hasFocus) {
      Utils.setSystemUiOptionsForFullscreen(this);
      acquireCameraIfReady();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    Log.d(TAG, "[onResume]");
    startBackgroundThread();
    cameraController.setBackgroundHandler(backgroundHandler);
    zoomScaleGestureListener.initZoomParameters(cameraId);
    resumed = true;
    acquireCameraIfReady();
  }

  @Override
  public void onPause() {
    super.onPause();
    Log.d(TAG, "[onPause]");
    resumed = false;
    cameraController.closeCamera();
    cameraAcquired = false;
    stopBackgroundThread();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    zoomScaleGestureListener.saveInstanceState(outState);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    return zoomScaleGestureDetector.onTouchEvent(event);
  }

  /** Acquires the camera if the window has focus and the activity has been resumed. */
  private void acquireCameraIfReady() {
    if (!cameraAcquired && resumed && hasWindowFocus()) {
      try {
        cameraController.acquireCamera(cameraId, outputSize, zoomScaleGestureListener.getZoom());
        cameraAcquired = true;
      } catch (IOException e) {
        String errorMessage = "Failed to acquire camera";
        Toasts.showToast(this, errorMessage, Toast.LENGTH_LONG);
        Log.w(TAG, errorMessage, e);
        finish();
      }
    }
  }

  // ===============================================================================================
  // Top Controls
  // ===============================================================================================

  private void initTopControls() {
    initApiSwitch();
    initCameraSelection();
   // setCameraIconForCurrentCamera();
    ImageButton cameraSelectionButton = findViewById(R.id.control_camera_selection);
    cameraSelectionButton.setOnClickListener(cameraSelectionOnClickListener);
    ImageButton galleryButton = findViewById(R.id.galleryButton);
    galleryButton.setOnClickListener(galleryOnClickListener);

  }

  @SuppressLint("SetTextI18n")
  private void initApiSwitch() {
//    Button button = findViewById(R.id.api_selector);
//    button.setText("API 2");
//    button.setOnClickListener(v -> {
//      Log.i(TAG, "switching to API 1");
//      preferences.setModeApi1(true);
//      finish();
//      //startActivity(Intents.createApi1Intent());
//    });
  }

  /** Initializes cameraId state from global preferences. */
  @SuppressWarnings("deprecation")
  private void initCameraSelection() {
    int cameraIdInt = preferences.getCameraId();
    if (cameraIdInt > CameraInfo.CAMERA_FACING_FRONT) {
      Log.e(TAG, "out of bounds camera id: " + cameraIdInt);
      cameraIdInt = CameraInfo.CAMERA_FACING_BACK;
      preferences.setCameraId(cameraIdInt);
    }
    cameraId = String.valueOf(cameraIdInt);
  }

  /** Handles clicks on the camera selection button. */
  private final OnClickListener cameraSelectionOnClickListener = new OnClickListener() {

    @Override
    public void onClick(View view) {
      Log.i(TAG, "changing cameras, releasing camera");
      // Swap camera ids.
      switch (cameraId) {
        case CAMERA_FACING_BACK:
          cameraId = CAMERA_FACING_FRONT;
          break;
        case CAMERA_FACING_FRONT:
          cameraId = CAMERA_FACING_BACK;
          break;
        default:
          Log.e(TAG, "unrecognized camera id: " + cameraId);
          cameraId = CAMERA_FACING_BACK;
      }
      preferences.setCameraId(Integer.valueOf(cameraId));
      //setCameraIconForCurrentCamera();

      Log.i(TAG, "restarting with new camera");
      zoomScaleGestureListener.reset();
      closeAndOpenCamera();
    }
  };

  /**
   * Capture Button Listener ******* Server Communication *********
   * */

  private final OnClickListener cameraCaptureOnClickListener = new OnClickListener() {

    @Override
    public void onClick(View view) {
      Log.i(TAG, "changing cameras, releasing camera");

      cameraController.takePicture(); //Take Picture Method
      LoadingActivity loadingActivity = new LoadingActivity(CameraApi2Activity.this);
      loadingActivity.show(); //Invoke Loading Animation

      // after 3 seconds from Loading Activity to Result Activity
      new Thread(new Runnable() {
        public void run() {
          // TODO Auto-generated method stub
          try {
            Thread.sleep(5000);
          } catch (Throwable ex) {
            ex.printStackTrace();
          }
          loadingActivity.dismiss();


          Intent intent = new Intent(CameraApi2Activity.this, ResultActivity.class);
          startActivity(intent);



        }
      }).start();

      //sendToServer();
      /**
       *  Server Communication
       *
       */


    }
  };

//  private void sendToServer(){
//    Intent intent = new Intent();
//    intent.setType("image/*");
//    intent.setAction(Intent.ACTION_GET_CONTENT);
//    startActivityForResult(intent, 1);
//  }


  private final OnClickListener galleryOnClickListener = new OnClickListener() {

    @Override
    public void onClick(View view) {
      Log.i(TAG, "changing cameras, releasing camera");
      // Swap camera ids.
      Log.d(TAG, "GALLERYYYYYYYYYYYYYYYY");

      Intent intent = new Intent();
      intent.setType("image/*");
      intent.setAction(Intent.ACTION_GET_CONTENT);
      startActivityForResult(intent, REQUEST_CODE);



    }
  };

  /** Call this when the camera is not acquired, to select the output size when it is acquired. */
  private void configureOutputSize() {
    Log.d(TAG, "configureOutputSize");
    Size[] sizes = cameraController.getSupportedPictureSizes(cameraId);
    outputSize = sizes[0];
  }

  /** Closes the camera and reacquires with the given outputSize. */
  private void closeAndOpenCamera() {
    Log.d(TAG, "closeAndOpenCamera");
    cameraController.closeCamera();
    cameraAcquired = false;
    configureOutputSize();
    zoomScaleGestureListener.initZoomParameters(cameraId);
    try {
      cameraController.acquireCamera(cameraId, outputSize, zoomScaleGestureListener.getZoom());
      cameraAcquired = true;
    } catch (IOException e) {
      Log.w(TAG, e);
    }
  }

  // ===============================================================================================
  // Thread
  // ===============================================================================================

  /** Starts a background thread and its {@link Handler}. */
  private void startBackgroundThread() {
    backgroundThread = new HandlerThread("CameraBackground");
    backgroundThread.start();
    backgroundHandler = new Handler(backgroundThread.getLooper());
  }

  /** Stops the background thread and its {@link Handler}. */
  private void stopBackgroundThread() {
    backgroundThread.quitSafely();
    try {
      backgroundThread.join();
      backgroundThread = null;
      backgroundHandler = null;
    } catch (InterruptedException e) {
      Log.w(TAG, "Interrupted while shutting background thread down", e);
    }
  }

// unnecessary
//  private void setCameraIconForCurrentCamera() {
//    ImageButton button = findViewById(R.id.control_camera_selection);
//    switch (cameraId) {
//      case CAMERA_FACING_BACK:
//        button.setImageResource(R.drawable.ic_camera_rear_white_24);
//        break;
//      case CAMERA_FACING_FRONT:
//        button.setImageResource(R.drawable.ic_camera_front_white_24);
//        break;
//      default:
//        break;
//    }
//  }


}

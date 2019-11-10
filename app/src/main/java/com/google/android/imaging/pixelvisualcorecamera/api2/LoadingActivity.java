package com.google.android.imaging.pixelvisualcorecamera.api2;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.android.imaging.pixelvisualcorecamera.R;

public class LoadingActivity extends ProgressDialog {

    private Context c;
    private ImageView imgAndroid;
    private Animation anim;

    private ResultActivity resultActivity;

    public LoadingActivity(Context context) {
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setCanceledOnTouchOutside(false);

        c=context;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        imgAndroid = (ImageView) findViewById(R.id.img_android);
        anim = AnimationUtils.loadAnimation(c, R.anim.loading);
        imgAndroid.setAnimation(anim);

    }

    @Override
    public void show() {
        super.show();
    }
    @Override
    public void dismiss() {
        super.dismiss();
    }


}

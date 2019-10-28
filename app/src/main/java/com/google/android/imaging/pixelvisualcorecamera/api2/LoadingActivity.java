package com.google.android.imaging.pixelvisualcorecamera.api2;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.android.imaging.pixelvisualcorecamera.R;
import com.google.android.imaging.pixelvisualcorecamera.api2.ResultActivity;

public class LoadingActivity extends Dialog {

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


//        try{
//            Thread.sleep(SplashTime);
//        }catch (InterruptedException e){
//            e.printStackTrace();
//        }
//
//        Intent intent = new Intent(LoadingActivity.class, ResultActivity.class);
//        startActivity(intent);

    }

    @Override
    public void show() {
        super.show();
    }
    @Override
    public void dismiss() {
        super.dismiss();
    }
    // private void initView() {
    // imgAndroid = (ImageView) findViewById(R.id.img_android);
    //   anim = AnimationUtils.loadAnimation(this, R.anim.loading);
    //   imgAndroid.setAnimation(anim);
//        try{
//            Thread.sleep(SplashTime);
//        }catch (InterruptedException e){
//            e.printStackTrace();
//        }
//
//        Intent intent = new Intent(LoadingActivity.this, ResultActivity.class);
//        startActivity(intent);
    //this.finish();
    // }


}

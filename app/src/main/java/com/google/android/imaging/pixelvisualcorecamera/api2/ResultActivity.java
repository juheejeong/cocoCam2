package com.google.android.imaging.pixelvisualcorecamera.api2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
<<<<<<< HEAD
=======
import android.os.AsyncTask;
>>>>>>> dev_juhee
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
<<<<<<< HEAD
=======
import android.webkit.DownloadListener;
>>>>>>> dev_juhee
import android.widget.ImageView;

import com.google.android.imaging.pixelvisualcorecamera.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class ResultActivity extends AppCompatActivity {

    private final OkHttpClient client = new OkHttpClient();
<<<<<<< HEAD
ImageView imageView;
=======
    ImageView imageView;
>>>>>>> dev_juhee

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
<<<<<<< HEAD
        ImageView imageView = findViewById(R.id.resultImageView);


        String downloadUrl = "http://52.79.240.201/phps/t4.jpg";

        try{downloadFileAsync(downloadUrl);
        }
        catch (Exception e){

        }
=======
        imageView = (ImageView) findViewById(R.id.resultImageView);

        String downloadUrl = "http://52.79.240.201/phps/original_photo.jpg";

        new DownloadImageTask(imageView)
                .execute(downloadUrl);
>>>>>>> dev_juhee
    }

    public void onClick(View view){
        Intent intent = new Intent(this, CameraApi2Activity.class);
        startActivity(intent);
    }

<<<<<<< HEAD

//    public void run() throws Exception {
//        Request request = new Request.Builder()
//                .url("http://52.79.240.201/Project/output/t4.jpg")
//                .build();
//
//        Response response = client.newCall(request).execute();
//        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//
//        Headers responseHeaders = response.headers();
//        for (int i = 0; i < responseHeaders.size(); i++) {
//            System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
//        }
//
//        System.out.println(response.body().string());
//    }

    public void downloadFileAsync(final String downloadUrl) throws Exception {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(downloadUrl).build();
        client.newCall(request).enqueue(new Callback() {
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Failed to download file: " + response);
                }

                InputStream is = response.body().byteStream();

                Bitmap bmp = BitmapFactory.decodeStream(is);
                showImage(bmp);



                //Log.d("response1", "downloadImage:" + response.body().string());

//                FileOutputStream fos = new FileOutputStream("d:/tmp.jpg");
//                fos.write(response.body().bytes());
//                fos.close();
            }
        });
    }

    public void showImage(Bitmap bmp)
    {

        imageView.setImageBitmap(bmp);


    }



=======
    //from server to Android
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {

            final OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(urls[0])
                    .build();

            Response response = null;
            Bitmap mIcon11 = null;
            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (response.isSuccessful()) {
                try {
                    mIcon11 = BitmapFactory.decodeStream(response.body().byteStream());
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }

            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

>>>>>>> dev_juhee
}
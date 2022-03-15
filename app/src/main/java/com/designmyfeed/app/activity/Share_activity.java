package com.designmyfeed.app.activity;

import static com.designmyfeed.app.fragment.WebFragment.SHARE_FILE_PATH;
import static com.designmyfeed.app.fragment.WebFragment.SHARE_IMAGE_URL;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.designmyfeed.app.R;
import java.io.File;
import java.util.Objects;

public class Share_activity extends AppCompatActivity {

    LinearLayout ly_cancel2,ly_share;
    String type="";
    ImageView image_share,image_play;
    ProgressBar native_progress_bar;
    CountDownTimer tt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_activity);

        try {
            image_share=findViewById(R.id.image_share);
            ly_cancel2=findViewById(R.id.ly_cancel2);
            ly_share=findViewById(R.id.ly_share);
            native_progress_bar = findViewById(R.id.native_progress_bar);
            image_play = findViewById(R.id.image_play);

            type="";
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                type = bundle.getString("type");
                if(type.equalsIgnoreCase("image"))
                {
                    //tv_header_text.setText("You can share more than one time the downloaded Image as you want to different apps.");

                    if(SHARE_FILE_PATH.exists()){

                        //Bitmap myBitmap = BitmapFactory.decodeFile(SHARE_FILE_PATH.getAbsolutePath());
                        //image_share.setImageBitmap(myBitmap);
                        File file = new File(SHARE_FILE_PATH.getAbsolutePath());
                        Uri path = FileProvider.getUriForFile(Objects.requireNonNull(Share_activity.this), "com.designmyfeed.app.provider", file);

                        image_share.setVisibility(View.VISIBLE);
                        image_play.setVisibility(View.GONE);

                        Glide.with(Share_activity.this)
                                .load(path)
                                .placeholder(R.drawable.placeholder)
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        native_progress_bar.setVisibility(View.GONE);
                                        //image_share.setVisibility(View.VISIBLE);
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        native_progress_bar.setVisibility(View.GONE);
                                        //image_share.setVisibility(View.VISIBLE);
                                        return false;
                                    }
                                })
                                .into(image_share);

                    }

                }else if(type.equalsIgnoreCase("video"))
                {
                    //tv_header_text.setText("You can share more than one time the downloaded video as you want to different apps.");

                    try {

                        Glide.with(Share_activity.this)
                                .load(SHARE_IMAGE_URL)
                                .placeholder(R.drawable.placeholder)
                                .into(image_share);
                        image_play.setVisibility(View.VISIBLE);

                    }catch (Exception e)
                    {
                        Log.e("video error",e+"");
                    }

                }else if(type.equalsIgnoreCase("audio"))
                {
                    try {

                        Glide.with(Share_activity.this)
                                .load(SHARE_IMAGE_URL)
                                .placeholder(R.drawable.placeholder)
                                .into(image_share);
                        image_play.setVisibility(View.VISIBLE);
                    }catch (Exception e)
                    {
                        Log.e("audio error",e+"");
                    }
                }
            }


            ly_cancel2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Share_activity.this.finish();
                }
            });

            ly_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(type.equalsIgnoreCase("image"))
                    {
                        File file = new File(SHARE_FILE_PATH.getAbsolutePath());
                        Uri path = FileProvider.getUriForFile(Objects.requireNonNull(Share_activity.this), "com.designmyfeed.app.provider", file);

                        //Uri uri = Uri.parse(f.getAbsolutePath());
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("image/jpeg");
                        intent.putExtra(android.content.Intent.EXTRA_TEXT, "Enjoying our app");
                        intent.putExtra(Intent.EXTRA_STREAM, path);
                        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        //intent.setPackage("com.whatsapp");
                        startActivity(Intent.createChooser(intent, "Share Content"));
                    }else if(type.equalsIgnoreCase("video"))
                    {

                        File file = new File(SHARE_FILE_PATH.getAbsolutePath());
                        Uri path = FileProvider.getUriForFile(Objects.requireNonNull(Share_activity.this), "com.designmyfeed.app.provider", file);

                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("video/*");
                        intent.putExtra(Intent.EXTRA_STREAM, path);
                        intent.putExtra(android.content.Intent.EXTRA_TEXT, "Enjoying our app");
                        startActivity(Intent.createChooser(intent, "Share Video!"));



                    }else if(type.equalsIgnoreCase("audio"))
                    {
                        File file = new File(SHARE_FILE_PATH.getAbsolutePath());
                        Uri path = FileProvider.getUriForFile(Objects.requireNonNull(Share_activity.this), "com.designmyfeed.app.provider", file);

                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("audio/*");
                        intent.putExtra(Intent.EXTRA_STREAM, path);
                        intent.putExtra(android.content.Intent.EXTRA_TEXT, "Enjoying our app");
                        startActivity(Intent.createChooser(intent, "Share Sound File"));

                    }

                }
            });

        }catch (Exception e)
        {

        }
    }
}

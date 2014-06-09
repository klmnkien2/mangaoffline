package com.gaogroup.mangaoffline;

import java.util.List;

import com.gaogroup.mangaoffline.R;
import com.gaogroup.mangaoffline.model.ChapterInfo;
import com.gaogroup.mangaoffline.model.MangaInfo;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

public class HomeActivity extends ActionBarActivity {
    
    private MangaInfo mangaInfo;
    private ChapterAdapter listAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
    
        setContentView(R.layout.activity_home);
        getSupportActionBar().hide(); 
        
        mangaInfo = AppController.getManga();
        
        initImageLoader();        
        admob();
        
        setupMangaInfo();
        setupButtonFunc();        
    }
    
    public void setupMangaInfo() {
        TextView name = (TextView) findViewById(R.id.name_info);
        TextView author = (TextView) findViewById(R.id.author_info);
        TextView year = (TextView) findViewById(R.id.year_info);
        TextView genres = (TextView) findViewById(R.id.genres_info);
        TextView description = (TextView) findViewById(R.id.description_info);
        ImageView image = (ImageView) findViewById(R.id.image_info);
        
        name.setText(mangaInfo.getName());
        author.setText(mangaInfo.getAuthor());
        year.setText(mangaInfo.getYearOfrelease());
        genres.setText(mangaInfo.getGenres());
        description.setText(mangaInfo.getDescription());
        
        //Load image data
        image.setImageResource(R.color.full_transparent);
        imageLoader.displayImage(mangaInfo.getImageUrl(), image);   
    }
    
    public void setupButtonFunc() {    

        findViewById(R.id.button_exit).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                confirmMessage("Confirm", "Do you really want to quit?");       
            }
        });

        findViewById(R.id.button_read).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomeActivity.this, MangaActivity.class); 
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);     
                startActivity(i); 
            }
        });

        findViewById(R.id.button_email).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendEmail();
            }
        });
    }
    
    public void sendEmail() {

        String[] TO = {"gaogroupvn@gmail.com"};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");


        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Hey!");
        
        try {
           startActivity(Intent.createChooser(emailIntent, "Send mail..."));
           finish();
        } catch (ActivityNotFoundException ex) {
           displayAlert("Error", "There is no email client installed.");
        }
     }
    
    private ImageLoader imageLoader;
    public ImageLoader getImageLoader() {
        if(!imageLoader.isInited()) initImageLoader();
        return imageLoader;
    }

    private void initImageLoader() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
        .cacheOnDisc(true)
        .cacheInMemory(true)
        .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
        .considerExifParams(true)
        .bitmapConfig(Bitmap.Config.RGB_565).build();

        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(this)
        .defaultDisplayImageOptions(defaultOptions)
        .discCacheFileCount(50)
        .memoryCache(new WeakMemoryCache());

        ImageLoaderConfiguration config = builder.build();
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
    }
    
    /*
     * Admob coding block
     */
    private AdView adView;   
    
    @Override
    public void onBackPressed() {              
        confirmMessage("Confirm", "Do you really want to quit?");       
    }

    @Override
    public void onPause() {
        super.onPause();
        adView.pause();
        if(imageLoader != null && imageLoader.isInited()) imageLoader.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        adView.resume();
        if(imageLoader != null && imageLoader.isInited()) {
            imageLoader.resume();
            setupMangaInfo();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adView.destroy();
        if(imageLoader != null && imageLoader.isInited()) imageLoader.destroy();
    }

    private void admob() {
        adView = (AdView) findViewById(R.id.adView); 
        adView.setAdListener(new AdListener() {
        
            @Override
            public void onAdLoaded() {
                adView.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
            }
          });
        
        AdRequest adRequest = new AdRequest.Builder()
        .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
        .build();

        adView.loadAd(adRequest);
    }
    
    public void loadChapter(List<ChapterInfo> items) {        
        listAdapter.clear();
        listAdapter.addAll(items);        
    }    
    
    public void confirmMessage(String title, String message) {

        AlertDialog.Builder confirm = new AlertDialog.Builder(this);
        confirm.setTitle(title);
        confirm.setMessage(message);

        confirm.setNegativeButton("OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface downloadDialog, int which) {

                System.exit(0);
            }
        });
        
        confirm.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        confirm.show().show();
    }
    
    public void displayAlert(String title, String message) {

        AlertDialog.Builder confirm = new AlertDialog.Builder(this);
        confirm.setTitle(title);
        confirm.setMessage(message);

        confirm.setNegativeButton("OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        confirm.show().show();
    }
}

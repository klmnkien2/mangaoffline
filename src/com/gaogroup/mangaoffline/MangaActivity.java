package com.gaogroup.mangaoffline;

import java.util.ArrayList;
import java.util.List;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.StringRequest;
import com.crittercism.app.Crittercism;
import com.gaogroup.mangaoffline.ChapterAdapter.ViewHolder;
import com.gaogroup.mangaoffline.R;
import com.gaogroup.mangaoffline.model.ChapterInfo;
import com.gaogroup.mangaoffline.utils.ChapterLoader;
import com.gaogroup.mangaoffline.utils.ChapterParser;
import com.gaogroup.mangaoffline.utils.Downloader;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;

public class MangaActivity extends ActionBarActivity {
    
    private ListView listView;
    private ChapterAdapter listAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crittercism.initialize(getApplicationContext(), "5266204c558d6a630500000c"); 
    
        setContentView(R.layout.activity_manga);
        getSupportActionBar().hide(); 
        
        initImageLoader();        
        admob();
        admobInterstitialAd();
        
        setupButtonFunc();  
        setupListChapters();        
        getChapterData();
    }
    
    public void setupButtonFunc() {        
        findViewById(R.id.buttonBack).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                goPreviousActivity();
            }
        });
        
        findViewById(R.id.buttonRefesh).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getChapterList(AppController.getManga().getMangaUrl());
            }
        });
    }
    
    public void setupListChapters() {
        listView = (ListView)findViewById(R.id.listView);
        listView.setFastScrollEnabled(true);
        listAdapter = new ChapterAdapter(this, R.layout.chapter_list_item, new ArrayList<ChapterInfo>());
        listView.setAdapter(listAdapter);
    }
    
    public void getChapterData() {
        List<ChapterInfo> lst = AppController.getInstance().getDBHelper().getAllChapters();
        if(lst.isEmpty()) {
            List<ChapterInfo> lstChapter = AppController.getListChapters();
            for (ChapterInfo info : lstChapter) {
                AppController.getInstance().getDBHelper().createChapter(info);
            }
            listAdapter.addAll(lstChapter);
        } else {
            List<ChapterInfo> lstChapter = new ArrayList<ChapterInfo>();
            for (ChapterInfo info : lst) {
                lstChapter.add(info);
            }
            listAdapter.addAll(lstChapter);
        }
        
        int lastRead = AppController.getInstance().getDBHelper().getMaxReadChapter();
        if(lastRead > 0) {
        	listView.setSelection(lastRead);
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
    private InterstitialAd interstitial;
    private void admobInterstitialAd() {
        // Create the interstitial.
        interstitial = new InterstitialAd(this);
        interstitial.setAdUnitId("ca-app-pub-1749468049809468/2828142638");

        // Create ad request.
        AdRequest adRequest = new AdRequest.Builder().build();

        // Begin loading your interstitial.
        interstitial.loadAd(adRequest);
    }
    
    @Override
    public void onBackPressed() {
        goPreviousActivity();
    }
    
    public void goPreviousActivity() {
        if (interstitial.isLoaded()) {
            interstitial.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    NavUtils.navigateUpFromSameTask(MangaActivity.this);
                }
            });
            interstitial.show();
        } else {        
            NavUtils.navigateUpFromSameTask(MangaActivity.this);
        }
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
    
    private ProgressDialog dialog;
    
    public void showProgressDialog() {
        dialog = new ProgressDialog(this);
        dialog.setTitle("Loading...");
        dialog.setMessage("This may take a few minutes");
        dialog.setCancelable(false);
        dialog.show();
    }
    
    public void closeProgressDialog() {
        if (dialog!=null) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }
    
    public void getChapterList(final String url)
    {  
        showProgressDialog();
        
        StringRequest strReq = new StringRequest(Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {                
                new ChapterLoader(MangaActivity.this, url).execute(response);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                closeProgressDialog();
                Log.e("chapterloader", "Error to connect network!");          
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, "req_chapter_list");
    }
    
    public void downloadChapter(ChapterInfo info)
    {
        ChapterParser parser = new ChapterParser(this, info);
        parser.execute(info.getChapterUrl());
    }
    
    public void downloadImageLinks(ArrayList<String> links, ChapterInfo info)
    {
        new Downloader(this, info, links).execute();
    }
    
    public void updateDownloadProgress(int listPos, int value) {
        ViewHolder holder = (ViewHolder)listView.getChildAt(listPos).getTag();
        holder.progressBar.setProgress(value);
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

package com.gaogroup.mangaoffline;

import java.util.ArrayList;
import java.util.List;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.StringRequest;
import com.crittercism.app.Crittercism;
import com.gaogroup.mangaoffline.R;
import com.gaogroup.mangaoffline.model.ChapterInfo;
import com.gaogroup.mangaoffline.model.MangaInfo;
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
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MangaActivity extends ActionBarActivity {
    
    private MangaInfo mangaInfo;
    private ListView listView;
    private ChapterAdapter listAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crittercism.initialize(getApplicationContext(), "5266204c558d6a630500000c"); 
    
        setContentView(R.layout.activity_manga);
        getSupportActionBar().hide(); 
        
        mangaInfo = AppController.getManga();
        
        initImageLoader();        
        admob();
        admobInterstitialAd();
        
        setupMangaInfo();
        setupListChapters();
        setupButtonFunc();
        setupProgressDialog();
        
        getChapterData();
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
        
        //collapse - expand
        findViewById(R.id.link_collapse).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				collapseOrExpand();				
			}
		});
        
        findViewById(R.id.item_infomation).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				collapseOrExpand();				
			}
		});
    }
    
    public void setupListChapters() {
        listView = (ListView)findViewById(R.id.listView);
        listView.setFastScrollEnabled(true);
        listAdapter = new ChapterAdapter(this);
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
    
    public void setupButtonFunc() {        
        Button refesh = (Button) findViewById(R.id.button_refesh);
        refesh.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                getChapterList(mangaInfo.getMangaUrl());
            }
        });
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
    
    public void collapseOrExpand() {
    	TextView link = (TextView) findViewById(R.id.link_collapse);
    	String tag = (String)link.getTag();    	
    	TextView description = (TextView) findViewById(R.id.description_info);
    	if(tag.equals("expand")) {
    		description.setVisibility(View.GONE);
    		findViewById(R.id.main_info).setVisibility(View.GONE);
    		link.setTag("collapse");
    		link.setText("TAP TO EXPAND");
    	} else {
    		description.setVisibility(View.VISIBLE);
    		findViewById(R.id.main_info).setVisibility(View.VISIBLE);
    		link.setTag("expand");
    		link.setText("TAP TO COLLAPSE");
    	}
    }
    
    /*
     * Admob coding block
     */
    private AdView adView;
    private InterstitialAd interstitial;
    private void admobInterstitialAd() {
        // Create the interstitial.
        interstitial = new InterstitialAd(this);
        interstitial.setAdUnitId("ca-app-pub-1749468049809468/2224857037");

        // Create ad request.
        AdRequest adRequest = new AdRequest.Builder().build();

        // Begin loading your interstitial.
        interstitial.loadAd(adRequest);
    }
    
    @Override
    public void onBackPressed() {
        if (interstitial.isLoaded()) {
            interstitial.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    confirmMessage("Confirm", "Do you really want to quit?");
                }
            });
            interstitial.show();
        } else {        
            confirmMessage("Confirm", "Do you really want to quit?");
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
    
    public void getChapterList(final String url)
    {  
        showProgressDialog(mangaDialog);
        StringRequest strReq = new StringRequest(Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {                
                new ChapterLoader(MangaActivity.this, url).execute(response);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                closeProgressDialog(mangaDialog);
                Log.e("chapterloader", "Error to connect network!");          
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, "req_chapter_list");
    }
    
    public void downloadChapter(String url)
    {
        ChapterParser parser = new ChapterParser(this);
        parser.execute(url);
    }
    
    public void downloadImageLinks(ArrayList<String> links)
    {
        new Downloader(this, links).execute();
    }
    
    public void updateDownloadProgress(int value) {
        getDownloadDialog().setProgress(value);
    }
    
    public void startDownloading(String message) {
        getDownloadDialog().setProgress(0);
        getDownloadDialog().setMessage(message);
    }
    
    /*
     * Setup a progresDialog
     */
    private ProgressDialog downloadDialog;
    private ProgressDialog mangaDialog;
    
    public void setupProgressDialog() {
    	downloadDialog = new ProgressDialog(this);
    	downloadDialog.setTitle("In progress...");
		downloadDialog.setMessage("Loading...");
		downloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		downloadDialog.setIndeterminate(false);   
		downloadDialog.setMax(100);
		downloadDialog.setCancelable(true);
		
		mangaDialog = new ProgressDialog(this);
		mangaDialog.setTitle("Message");
		mangaDialog.setMessage("Loading...");
		mangaDialog.setCancelable(true);
    }
    
    public ProgressDialog getDownloadDialog() {
        return downloadDialog;
    }
    
    public ProgressDialog getMangaDialog() {
        return mangaDialog;
    }
    
    public void showProgressDialog(ProgressDialog dialog) {
        if (dialog!=null) {
            if (!dialog.isShowing()) {
                dialog.show();
            }
        }
    }
    
    public void closeProgressDialog(ProgressDialog dialog) {
        if (dialog!=null) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
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

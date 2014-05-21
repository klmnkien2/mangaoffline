package com.gaogroup.mangaoffline;

import java.util.ArrayList;
import java.util.List;

import com.crittercism.app.Crittercism;
import com.gaogroup.mangaoffline.R;
import com.gaogroup.mangaoffline.model.ChapterInfo;
import com.gaogroup.mangaoffline.model.MangaInfo;
import com.gaogroup.mangaoffline.utils.ChapterLoader;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
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
        
        setupMangaInfo();
        setupListChapters();
        setupButtonFunc();
        getChapterData();
        setupProgressDialog();
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
            getChapterList(mangaInfo.getMangaUrl());
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
        Button download = (Button) findViewById(R.id.button_download);
        download.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                
            }
        });
        
        Button refesh = (Button) findViewById(R.id.button_refesh);
        refesh.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                listAdapter.clear();
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
    		link.setTag("collapse");
    		link.setText("Click to Expand");
    	} else {
    		description.setVisibility(View.VISIBLE);
    		link.setTag("expand");
    		link.setText("Click to Collapse");
    	}
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
    
    public void loadChapter(ChapterInfo item) {        
        listAdapter.addItem(item);        
    }
    
    public void updateProgressBar(int value) {
        dialog.setProgress(value);
    }
    
    public void finishLoading() {
        listAdapter.notifyDataSetChanged();
        closeProgressDialog();
    }
    
    public void getChapterList(String url)
    {
        listAdapter.clear();
        showProgressDialog();
        new ChapterLoader(this).execute(url);
    }
    
    public void downloadChapter(String url)
    {
        listAdapter.clear();
        showProgressDialog();
    }
    
    /*
     * Setup a progresDialog
     */
    private ProgressDialog dialog;
    
    public void setupProgressDialog() {
    	dialog = new ProgressDialog(this);
    	dialog.setTitle("In progress...");
		dialog.setMessage("Loading...");
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setIndeterminate(false);   
		dialog.setMax(100);
//		dialog.setIcon(R.drawable.arrow_stop_down);
		dialog.setCancelable(true);
    }
    
    public void updateProgress(int percent) {
    	dialog.setProgress(percent);
    }
    
    public void showProgressDialog() {
        if (dialog!=null) {
            if (!dialog.isShowing()) {
                dialog.show();
            }
        }
    }
    
    public void closeProgressDialog() {
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

            public void onClick(DialogInterface dialog, int which) {

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
}

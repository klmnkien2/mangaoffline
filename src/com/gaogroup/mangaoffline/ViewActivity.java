package com.gaogroup.mangaoffline;

import java.util.ArrayList;

import com.crittercism.app.Crittercism;
import com.gaogroup.mangaoffline.model.ChapterInfo;
import com.gaogroup.mangaoffline.model.ViewItem;
import com.gaogroup.mangaoffline.utils.DatabaseHelper;
import com.gaogroup.mangaoffline.utils.ViewController;
import com.gaogroup.mangaoffline.utils.ViewController.ViewChangeListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.graphics.Bitmap;

public class ViewActivity extends ActionBarActivity implements ViewChangeListener {

    private AdView adView;
    private DatabaseHelper dbHelper;
    
    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;
    private ChapterInfo current_chapter;
    
    private ViewController mViewController;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = AppController.getInstance().getDBHelper();
        Crittercism.initialize(getApplicationContext(), "5266204c558d6a630500000c"); 
        setContentView(R.layout.activity_view);
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        addAds();     
        
        setupScreenDimension();
        initImageLoader();
        setupViewPager();
        mViewController = new ViewController(this);
        
        String chapter_url = getIntent().getStringExtra("CHAPTER_URL");
        current_chapter = dbHelper.getChapterByUrl(chapter_url);        
        
        refesh();
    }
    
    public void setupViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.viewPager);        
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setOnPageChangeListener(mViewPagerAdapter);        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_view, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        switch (item.getItemId()) 
        {
        case android.R.id.home: 
            NavUtils.navigateUpFromSameTask(this);
            break;
            
        case R.id.action_prev:
            prev();  
            return true;
            
        case R.id.action_refesh:
            ViewFragment view = (ViewFragment)mViewPagerAdapter.getRegisteredFragment(mViewPagerAdapter.selectedItem);
            if(!view.isLoadingOnly) {
                view.loadImage();
            }
            return true;
            
        case R.id.action_next:
            next();  
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
        return true;
    }
    
    private ImageLoader imageLoader;
    public ImageLoader getImageLoader() {
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
        if(!imageLoader.isInited()) imageLoader.init(config);
    }
    
    public float SCALE_DIP = 0, SCREEN_WIDTH = 0, SCREEN_HEIGHT = 0;
    public void setupScreenDimension() {
        if(SCREEN_WIDTH == 0) { 
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            SCALE_DIP = metrics.density;
            SCREEN_WIDTH = metrics.widthPixels;
            SCREEN_HEIGHT = metrics.heightPixels;
        }
    }

    public void refesh() {
        getSupportActionBar().setTitle("Chapter " + (current_chapter.getNumber() + 1));
        mViewPagerAdapter.resetItems();
        mViewController.getViewLinks(current_chapter.getChapterUrl());
    }
    
    public void prev() {
        cancelNetwork();
        ChapterInfo prev_chapter = dbHelper.getChapterByNumber(current_chapter.getNumber() - 1, current_chapter.getMangaUrl());
        if(prev_chapter != null) {
            current_chapter = prev_chapter;
            refesh();
        }
    }
    
    public void next() {
        cancelNetwork();
        ChapterInfo next_chapter = dbHelper.getChapterByNumber(current_chapter.getNumber() + 1, current_chapter.getMangaUrl());
        if(next_chapter != null) {  
            current_chapter = next_chapter;
            refesh();
        } else {
            displayAlert("Message", "You have read the last chapter.");
        }
    }

    public static class ViewPagerAdapter extends FragmentStatePagerAdapter implements ViewPager.OnPageChangeListener {
        private ArrayList<ViewItem> items = new ArrayList<ViewItem>();
        private int showedItems;
        private int mTotal;
        private final int NUMBER_ITEMS_PER_LOADING = 5;
        private int selectedItem = 0;
        SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

        public ViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            this.showedItems = 0;
        }
        
        public void resetItems() {
            items.clear();
            this.showedItems = 0;

            notifyDataSetChanged();
        }
        
        public void addMoreItems(ArrayList<ViewItem> moreItems, int total) {
            removeLoadingOnly();
            
            this.mTotal = total;
            for (ViewItem item : moreItems) {
                items.add(item);
            }

            addMoreItems();
        }
        
        public void addMoreItem(ViewItem moreItem, int total) {
            removeLoadingOnly();
            
            this.mTotal = total;
            items.add(moreItem);

            addMoreItems();
        }
        
        public void notifyNetworkTrouble() {
            if(!items.isEmpty()) {
                ViewItem loadingOnly = items.get(items.size() - 1);
                if(loadingOnly.isLoadingOnly()) {
                    loadingOnly.setNetworkTrouble(true);
                }
            }
            
            notifyDataSetChanged();
        }

        public void addLoadingOnly() {
            ViewItem loadingOnly = new ViewItem("X");
            loadingOnly.setLoadingOnly(true);
            
            items.add(loadingOnly);
            showedItems++;

            notifyDataSetChanged();
        }

        public void removeLoadingOnly() {
            if(items.get(items.size()-1).isLoadingOnly()) {
                items.remove(items.size()-1);
                showedItems--;
            }
        }
        
        public void finishLoading() {
            removeLoadingOnly();
            notifyDataSetChanged();
        }

        public void addMoreItems() {            

            if(showedItems + NUMBER_ITEMS_PER_LOADING < items.size())
                showedItems = showedItems + NUMBER_ITEMS_PER_LOADING;
            else showedItems = items.size();
           
            notifyDataSetChanged();
        }

        @Override
        public int getItemPosition(Object object){
            return PagerAdapter.POSITION_NONE;
        }

        @Override
        public int getCount() {
            return showedItems;
        }

        @Override
        public Fragment getItem(int position) {
            ViewFragment fragment = null;
            if(registeredFragments != null && registeredFragments.size() > position)
                fragment = (ViewFragment)getRegisteredFragment(position);
            if(fragment == null) {
                ViewItem item = items.get(position);
                item.setTitle((position + 1) + "/" + mTotal);
                fragment = ViewFragment.newInstance( position, items.get(position));
            }
               
            return fragment;
        }
        
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }
        
        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) { 
            selectedItem = position;
            
            if(!items.get(position).isLoadingOnly() && position == showedItems-1 && showedItems < items.size()-1) {
                addMoreItems();
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }

    }
    
    /*
     * Admob coding block 
     */
    
    @Override
    public void onBackPressed() {
        cancelNetwork();
        NavUtils.navigateUpFromSameTask(this);
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
        if(imageLoader != null && imageLoader.isInited()) imageLoader.resume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adView.destroy();
        cancelNetwork();
        
        if (dbHelper != null) {
            dbHelper.closeDB();
        }
        if(imageLoader != null && imageLoader.isInited()) imageLoader.destroy();
    }

    private void addAds() {
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
    
    public void cancelNetwork() {
        AppController.getInstance().cancelPendingRequests("req_view_content");
        AppController.getInstance().cancelPendingRequests("req_view_item");
    }

    @Override
    public void onViewPreLoading() {
        mViewPagerAdapter.addLoadingOnly();
    }

    @Override
    public void onViewLoaded(ViewItem viewItems, int total) {
        mViewPagerAdapter.addMoreItem(viewItems, total);
        if(current_chapter != null) {
            dbHelper.readChapter(current_chapter);
        }
        
        mViewPagerAdapter.addLoadingOnly();
    }
    
    @Override
    public void onViewFinished() {
        mViewPagerAdapter.finishLoading();
    }

    @Override
    public void onViewLoadFail(Exception ex) {
        ex.printStackTrace();
        mViewPagerAdapter.notifyNetworkTrouble();
    }
}

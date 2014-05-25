package com.gaogroup.mangaoffline.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;

import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.StringRequest;
import com.gaogroup.mangaoffline.AppController;
import com.gaogroup.mangaoffline.MangaActivity;
import com.gaogroup.mangaoffline.ViewActivity;
import com.gaogroup.mangaoffline.model.ViewItem;
import com.gaogroup.mangaoffline.utils.ViewController.ViewChangeListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.HttpClientImageDownloader;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class Downloader extends AsyncTask<String, Void, Integer> {
    
    public static String BASE_URL = "http://www.mangaeden.com";

    private MangaActivity activity;
    private String chapterUrl;
    private int mTotal;
    private int mCount;

    public Downloader(MangaActivity activity) {
        this.activity = activity;
    }

    protected Integer doInBackground(String... params) {
        try
        {
            activity.showProgressDialog();
            chapterUrl = params[0];
            executeVolley(chapterUrl);
        }
        catch(Exception ex)
        {     
            activity.closeProgressDialog();
            Log.e("chapterloader", "Error to connect network!");    
        }
        return 0;
    }
    
    public String downloadUrl(String imageUrl) {
        File fileForImage = new File(FileUtils.getSaveDir(activity), FileUtils.getImageNameFromUrl(imageUrl));

        Log.e("filename", fileForImage.getAbsolutePath());
        try {
            
            URL url = new URL(imageUrl);
            URLConnection conection = url.openConnection();
            conection.connect();
            // this will be useful so that you can show a tipical 0-100% progress bar
            int lenghtOfFile = conection.getContentLength();
            
            InputStream sourceStream;
            File cachedImage = ImageLoader.getInstance().getDiscCache().get(imageUrl);
            if (cachedImage.exists()) { 
                sourceStream = new FileInputStream(cachedImage);
                OutputStream targetStream = new FileOutputStream(fileForImage);
                copyStream(sourceStream, targetStream, lenghtOfFile);
                targetStream.close();
                sourceStream.close();
            } else { 
                HttpClientImageDownloader downloader = new HttpClientImageDownloader(activity, new DefaultHttpClient());
                sourceStream = downloader.getStream(imageUrl, null);
                OutputStream targetStream = new FileOutputStream(fileForImage);
                copyStream(sourceStream, targetStream, lenghtOfFile);
                targetStream.close();
                sourceStream.close();
            }
            return fileForImage.getAbsolutePath();
            
        } catch(Exception e) {
            return null;
        }
    }
    
    public void copyStream(InputStream is, OutputStream os, int lenghtOfFile) throws IOException {
        byte[] bytes = new byte[FileUtils.BUFFER_SIZE];
        long total = 0;
        while (true) {
            int count = is.read(bytes, 0, FileUtils.BUFFER_SIZE);
            activity.updateProgress((int)((total*100)/lenghtOfFile));
            if (count == -1) {
                break;
            }
            os.write(bytes, 0, count);
        }
    }
    
    public void executeVolley(String url) {
        
        StringRequest strReq = new StringRequest(Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                parseHtml(response);
                getMoreViewItem();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("downloader", "Error to connect network!");   
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, "req_view_content");
    }
    
    private Document doc;
    private Elements elements;
    private Iterator<Element> mIterator;
    
    public void parseHtml(String html) {
        doc = Jsoup.parse(html);
        elements = doc.select(".pagination > a");
    }

    public void getMoreViewItem()
    {
        mTotal = elements.size() - 2;
        mIterator = elements.iterator();
        parseImageUrl();
    }
    
    public void parseImageUrl() {
        if(mIterator.hasNext())
        {
            if(this.mCount == 0 || this.mCount == mTotal) return;
            
            activity.updateProgressMessage("Downloading " + (mCount + 1) + "/" + mTotal);
            Element e = mIterator.next();
            
            String sourceUrl = e.attr("value");      
            
            StringRequest strReq = new StringRequest(Method.GET, BASE_URL + sourceUrl, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    try {
                        Document doc = Jsoup.parse(response);
                        Element element = doc.select("img#mainImg").get(0);        
                        String imageUrl = element.attr("src");

                        ViewItem item = new ViewItem(imageUrl);
                        item.setOrder(mCount);
                        item.setChapterUrl(chapterUrl);                        
                        String saveFile = downloadUrl(imageUrl);
                        if(saveFile != null) {
                            item.setFileUrl(saveFile);
                            ViewItem exist = AppController.getInstance().getDBHelper().getPage(imageUrl);
                            if(exist != null) {
                                AppController.getInstance().getDBHelper().updatePage(item);
                            } else {
                                AppController.getInstance().getDBHelper().createPage(item);
                            }
                        }
                        
                        parseImageUrl();
                    } catch(Exception e) {
                        Log.e("oh yeah", "volley response");  
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    parseImageUrl();
                    Log.e("Oh yeah", "volley request error");  
                }
            });

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, "req_view_item");
            this.mCount ++;
        } else {
            activity.closeProgressDialog();
        }
    }

}


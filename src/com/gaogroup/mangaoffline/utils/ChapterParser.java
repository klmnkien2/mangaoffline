package com.gaogroup.mangaoffline.utils;

import java.util.ArrayList;
import java.util.Iterator;

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
import com.gaogroup.mangaoffline.model.ChapterInfo;

import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ChapterParser {

    public static String BASE_URL = "http://www.mangaeden.com";

    private MangaActivity activity;
    private String chapterUrl;
    private int mTotal;
    private int mCount;
    private ChapterInfo info;
    private ArrayList<String> links = new ArrayList<>(); 

    public ChapterParser(MangaActivity activity, ChapterInfo info) {
        this.activity = activity;
        this.info = info;
        this.mTotal = 0;
        this.mCount = 0;
    }

    public void execute(String url) {
        updateProgress(0);

        chapterUrl = url;
        StringRequest strReq = new StringRequest(Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                parseHtml(response);
                getMoreViewItem();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                info.setDownloading(false);
                ProgressBar bar = info.getProgressBar();
                TextView text = info.getProgressText();
                if(bar != null) {
                    bar.setVisibility(View.GONE);
                    bar.invalidate();
                    
                    text.setVisibility(View.GONE);
                    text.invalidate();
                }

                activity.displayAlert("Message", "Image links loaded unsuccessfullly");
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, "ChapterParser");
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
    
    public void updateProgress(int value) {
        info.setProgress(value);
        ProgressBar bar = info.getProgressBar();
        TextView text = info.getProgressText();
        if(bar != null) {
            bar.setProgress(info.getProgress());
            bar.invalidate();
            
            text.setText(info.getProgress() + "/" + bar.getMax());
            text.invalidate();
        }
    }

    public void parseImageUrl() {
        if(mIterator.hasNext())
        {

            Element e = mIterator.next();
            this.mCount ++;
            updateProgress(this.mCount * 50 / this.mTotal);

            if(this.mCount > 1 && this.mCount < elements.size()) {

                String sourceUrl = BASE_URL + e.attr("href");   
                if(this.mCount == 2) sourceUrl = chapterUrl;

                StringRequest strReq = new StringRequest(Method.GET,  sourceUrl, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        try {
                            Document doc = Jsoup.parse(response);
                            Element element = doc.select("img#mainImg").get(0);        
                            String imageUrl = element.attr("src");
                            if(!imageUrl.startsWith("http:")) {
                                imageUrl = "http:" + imageUrl;
                            }

                            links.add(imageUrl);   

                        } catch(Exception e) {
                            Log.e("oh yeah", "Parser loi link stt : " + mCount);  
                        }

                        parseImageUrl();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        parseImageUrl();
                        Log.e("oh yeah", "Volley loi link stt : " + mCount);  
                    }
                });

                // Adding request to request queue
                AppController.getInstance().addToRequestQueue(strReq, "ChapterParser");
            }
            else {
                parseImageUrl();
            }

        } else {
            activity.downloadImageLinks(links, info);
        }
    }

}


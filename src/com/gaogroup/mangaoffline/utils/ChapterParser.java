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
import com.gaogroup.mangaoffline.model.ViewItem;

import android.util.Log;

public class ChapterParser {
    
    public static String BASE_URL = "http://www.mangaeden.com";

    private MangaActivity activity;
    private String chapterUrl;
    private int mTotal;
    private int mCount;
    private ArrayList<String> links = new ArrayList<>(); 

    public ChapterParser(MangaActivity activity) {
        this.activity = activity;
        this.mTotal = 0;
        this.mCount = 0;
    }

    public void execute(String url) {
        activity.getDownloadDialog().setMessage("Loading image links ...");
        activity.getDownloadDialog().setProgress(0);
        activity.showProgressDialog(activity.getDownloadDialog());
        
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
                activity.closeProgressDialog(activity.getDownloadDialog());
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

    public void parseImageUrl() {
        if(mIterator.hasNext())
        {

            Element e = mIterator.next();
            this.mCount ++;
            activity.getDownloadDialog().setProgress(this.mCount * 100 / this.mTotal);

            if(this.mCount > 1 && this.mCount < elements.size() - 1) {
                
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
                            
                            ViewItem exist = AppController.getInstance().getDBHelper().getPage(imageUrl);
                            if(exist == null) {
                                ViewItem item = new ViewItem(imageUrl);
                                item.setOrder(mCount);
                                item.setChapterUrl(chapterUrl);
                                AppController.getInstance().getDBHelper().createPage(item);
                            }
                            
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
            activity.downloadImageLinks(links);
        }
    }

}


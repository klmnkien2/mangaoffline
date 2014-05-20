package com.gaogroup.mangaoffline.utils;

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
import com.gaogroup.mangaoffline.model.ViewItem;
import com.gaogroup.mangaoffline.utils.ViewController.ViewChangeListener;

import android.util.Log;

public class ViewLoader
{    
    public static String BASE_URL = "http://www.mangaeden.com";

    private ViewChangeListener listener;
    private int mTotal;
    private int mCount;

    public ViewLoader(ViewChangeListener listener)
    {
        this.listener = listener;
        this.mTotal = 0;
        this.mCount = 0;
    }
    
    public void execute(String url) {
        listener.onViewPreLoading();
        StringRequest strReq = new StringRequest(Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                parseHtml(response);
                getMoreViewItem();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onViewLoadFail(new Exception("Error to connect network!"));
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
        	
            Element e = mIterator.next();
            
            String sourceUrl = e.attr("value");      
            
            StringRequest strReq = new StringRequest(Method.GET, BASE_URL + sourceUrl, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    try {
                        Document doc = Jsoup.parse(response);
                        Element element = doc.select("img#mainImg").get(0);        
                        String imageUrl = element.attr("src");

                        listener.onViewLoaded(new ViewItem(imageUrl), mTotal);
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
            listener.onViewFinished();
        }
    }
    
    public String getThumbName(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

}

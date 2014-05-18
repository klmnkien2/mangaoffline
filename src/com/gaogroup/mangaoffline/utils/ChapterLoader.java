package com.gaogroup.mangaoffline.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.StringRequest;
import com.gaogroup.mangaoffline.AppController;
import com.gaogroup.mangaoffline.model.ChapterInfo;
import com.gaogroup.mangaoffline.utils.NetworkController.NetworkChangeListener;

import android.os.AsyncTask;
import android.util.Log;

public class ChapterLoader extends AsyncTask<String, Integer, Void>
{    
    private static int PRE_LOADING = 1;
    private static int SUCCESS_STATUS = 2;
    private static int ERROR_STATUS = 3;
    public static String BASE_URL = "http://www.mangaeden.com";

    private NetworkChangeListener listener;
    private List<ChapterInfo> items = new ArrayList<ChapterInfo>();
    private String mangaUrl;

    public ChapterLoader(NetworkChangeListener listener)
    {
        this.listener = listener;        
    }
    
    @Override
    protected Void doInBackground(String... params)
    {
        try
        {
            publishProgress(PRE_LOADING);
            this.mangaUrl = params[0];
            executeVolley(params[0]);
        }
        catch(Exception ex)
        {     
            publishProgress(ERROR_STATUS);            
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values)
    {
        if(values[0].intValue() == PRE_LOADING)
        {
            listener.onPreLoading();
        }
        else if(values[0].intValue() == SUCCESS_STATUS)
        {
            listener.onLoadedChapter(items, null);
        } 
        else if(values[0].intValue() == ERROR_STATUS)
        {
            listener.onLoadedFail(new Exception("Error to connect network!"));
        }
    }
    
    public void executeVolley(String url) {
        
        if(url != null && !url.equals("")) {
            
            StringRequest strReq = new StringRequest(Method.GET, url, new Response.Listener<String>() {
    
                @Override
                public void onResponse(String response) {
                    parseHtml(response);
                    items = getMoreItem();
                    publishProgress(SUCCESS_STATUS); 
                }
            }, new Response.ErrorListener() {
    
                @Override
                public void onErrorResponse(VolleyError error) {
                    publishProgress(ERROR_STATUS);                    
                }
            });
    
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, "req_chapter_list");
        } else {
            publishProgress(SUCCESS_STATUS); 
        }
    }
    
    private Document doc;
    private Elements elements;
    private Element content;
    
    public void parseHtml(String html) {
        doc = Jsoup.parse(html);
        content = doc.select("#leftContent > table > tbody").get(0);        
        elements = content.select("tr");
    }
    
    public List<ChapterInfo> getMoreItem()
    {
        ArrayList<ChapterInfo> items = new ArrayList<ChapterInfo>();
        Iterator<Element> i = elements.iterator();

        int number = 0;
        while(i.hasNext())
        {
            Element e = i.next();
            ChapterInfo item = getItem(e);
            
            if(item != null) {
                item.setNumber(number);
                
                ChapterInfo existInfo = AppController.getInstance().getDBHelper().getChapterByUrl(item.getChapterUrl());
                if(existInfo == null) AppController.getInstance().getDBHelper().createChapter(item);
                else item = existInfo;
                
                items.add(item); 
                number ++;
            }
        }

        return items;
    }

    private ChapterInfo getItem(Element e)
    {
        try {      
            String title = e.select("td > a").get(0).text();
            String url = e.select("td > a").get(0).attr("href");
            String sub = e.select("td.chapterDate").get(0).text();
    
            return new ChapterInfo(mangaUrl, BASE_URL + url, stripString(title), stripString(sub), 0, -1);
        } catch (Exception ex) {
            Log.e("Boc tach loi element", ex.getMessage());  
            return null;
        }
    }

    public String getAttributeValue(Element e, String query, String attr)
    {
        Elements elem = e.select(query);
        return elem.get(0).attr(attr);
    }

    public String stripString(String str) {
        return str.replaceAll("&nbsp;", " ").replaceAll("&quot;", "\"").replaceAll("&amp;", "&")
                .replaceAll("&lt;", "<").replaceAll("&gt;", ">");
    }
}

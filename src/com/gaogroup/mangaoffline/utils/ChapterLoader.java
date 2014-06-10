package com.gaogroup.mangaoffline.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.gaogroup.mangaoffline.AppController;
import com.gaogroup.mangaoffline.MangaActivity;
import com.gaogroup.mangaoffline.model.ChapterInfo;

import android.os.AsyncTask;
import android.util.Log;

public class ChapterLoader extends AsyncTask<String, Integer, Void>
{    
    public static String BASE_URL = "http://www.mangaeden.com";

    private static int LOAD_ITEM = 1;
    private static int FINISH_JOB = 2;
    
    private MangaActivity activity;
    private String mangaUrl;
    List<ChapterInfo> items = new ArrayList<ChapterInfo>();

    public ChapterLoader(MangaActivity activity, String mangaUrl)
    {
        this.activity = activity;
        this.mangaUrl = mangaUrl;
    }
    
    @Override
    protected Void doInBackground(String... params)
    {
        try
        {
            parseHtml(params[0]);
            getMoreItem();
            publishProgress(FINISH_JOB);
        }
        catch(Exception ex)
        {
        	Log.e("chapterloader", "Boc tach chapter loi"); 
        	ex.printStackTrace();
        }

        return null;
    }
    
    @Override
    protected void onProgressUpdate(Integer... values)
    {
        if(values[0].intValue() == LOAD_ITEM)
        {
            activity.loadChapter(items); 
        }
        else if(values[0].intValue() == FINISH_JOB)
        {
            activity.closeProgressDialog();
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
    
    public void getMoreItem()
    {
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
                else {
                    item.setIsRead(existInfo.getIsRead());
                    item.setDownloaded(existInfo.getDownloaded());
                    AppController.getInstance().getDBHelper().updateChapter(item);
                }
                
                items.add(0, item);
                number ++;
            }
        }
 
        publishProgress(LOAD_ITEM);
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

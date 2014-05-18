package com.gaogroup.mangaoffline.utils;

import java.util.List;

import com.gaogroup.mangaoffline.model.ChapterInfo;
import com.gaogroup.mangaoffline.model.MangaInfo;


public class NetworkController
{ 
    private NetworkChangeListener listener;
    
    
    public static interface NetworkChangeListener
    {
        void onPreLoading();
        
        void onLoadedManga(List<MangaInfo> items, String nextUrl);
        
        void onLoadedChapter(List<ChapterInfo> items, String nextUrl);
        
        void onLoadedFail(Exception ex);
    }

    public NetworkController(NetworkChangeListener listener)
    {
        this.listener = listener;
    }
    
    public void getChapterList(String url)
    {
        new ChapterLoader(listener).execute(url);
    }
}

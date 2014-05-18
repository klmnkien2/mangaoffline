package com.gaogroup.mangaoffline.utils;

import com.gaogroup.mangaoffline.model.ViewItem;


public class ViewController
{ 
    private ViewChangeListener listener;
    
    
    public static interface ViewChangeListener
    {
        void onViewPreLoading();
        
        void onViewLoaded(ViewItem viewItems, int total);
        
        void onViewFinished();
        
        void onViewLoadFail(Exception ex);
    }

    public ViewController(ViewChangeListener listener)
    {
        this.listener = listener;
    }

    public void getViewLinks(String url)
    {
        new ViewLoader(listener).execute(url);
    }
}

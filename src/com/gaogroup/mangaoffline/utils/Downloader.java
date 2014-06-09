package com.gaogroup.mangaoffline.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.http.impl.client.DefaultHttpClient;

import com.gaogroup.mangaoffline.AppController;
import com.gaogroup.mangaoffline.MangaActivity;
import com.gaogroup.mangaoffline.model.ChapterInfo;
import com.gaogroup.mangaoffline.model.ViewItem;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.HttpClientImageDownloader;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Downloader extends AsyncTask<Void, Integer, Void> {
    
    public static String BASE_URL = "http://www.mangaeden.com";

    private MangaActivity activity;    
    private ChapterInfo info;
    private ArrayList<String> links = new ArrayList<>(); 

    public Downloader(MangaActivity activity, ChapterInfo info, ArrayList<String> links) {
        this.activity = activity;
        this.info = info;
        this.links = links;
    }

    @Override
    protected Void doInBackground(Void... params) {
        
        for(int i=0; i < links.size(); i++) {
            publishProgress((i+1) * 50 / links.size());
            
            String fileUrl = downloadUrl(links.get(i));
            if(fileUrl != null) {
                ViewItem exist = AppController.getInstance().getDBHelper().getPage(links.get(i));
                if(exist != null) {
                    exist.setFileUrl("file://" + fileUrl);
                    AppController.getInstance().getDBHelper().updatePage(exist);
                } else {
                    ViewItem item = new ViewItem(links.get(i));
                    item.setOrder(i);
                    item.setChapterUrl(info.getChapterUrl());
                    item.setFileUrl("file://" + fileUrl);
                    AppController.getInstance().getDBHelper().createPage(item);
                }
            }
            
        }        
        
        AppController.getInstance().getDBHelper().updateChapter(info);
        
        return null;
    }
    
    @Override
    protected void onProgressUpdate(Integer... values)
    {
        int progress = values[0].intValue() + 50;
        info.setProgress(progress);
        info.setDownloaded(1);
        ProgressBar bar = info.getProgressBar();
        TextView text = info.getProgressText();
        if(bar != null) {
            bar.setProgress(info.getProgress());
            bar.invalidate();
            text.setText(info.getProgress() + "/" + bar.getMax());
            text.invalidate();
        }
    }
    
    @Override
    protected void onPostExecute(Void voids) {
        info.setDownloading(false);
        ProgressBar bar = info.getProgressBar();
        ImageView button = info.getDownloadButton();
        TextView text = info.getProgressText();
        if(bar != null) {
            bar.setVisibility(View.GONE);
            bar.invalidate();
            button.setVisibility(View.VISIBLE);
            button.invalidate();
            text.setText("Downloaded");
            text.invalidate();
        }
    }
    
    public String downloadUrl(String imageUrl) {
        File fileForImage = new File(FileUtils.getSaveDir(activity), FileUtils.getImageNameFromUrl(imageUrl));
     
        try {
            
//            URL url = new URL(imageUrl);
//            URLConnection conection = url.openConnection();
//            conection.connect();
            // this will be useful so that you can show a tipical 0-100% progress bar
//            int lenghtOfFile = conection.getContentLength();
            
            InputStream sourceStream;
            File cachedImage = ImageLoader.getInstance().getDiscCache().get(imageUrl);
            if (cachedImage.exists()) { 
                sourceStream = new FileInputStream(cachedImage);
                OutputStream targetStream = new FileOutputStream(fileForImage);
                copyStream(sourceStream, targetStream, 0);
                targetStream.close();
                sourceStream.close();
            } else { 
                HttpClientImageDownloader downloader = new HttpClientImageDownloader(activity, new DefaultHttpClient());
                sourceStream = downloader.getStream(imageUrl, null);
                OutputStream targetStream = new FileOutputStream(fileForImage);
                copyStream(sourceStream, targetStream, 0);
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
        while (true) {
            int count = is.read(bytes, 0, FileUtils.BUFFER_SIZE);            
            if (count == -1) {
                break;
            }
            os.write(bytes, 0, count);
        }
    }

}


package com.gaogroup.mangaoffline.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.http.impl.client.DefaultHttpClient;

import com.gaogroup.mangaoffline.AppController;
import com.gaogroup.mangaoffline.MangaActivity;
import com.gaogroup.mangaoffline.model.ViewItem;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.HttpClientImageDownloader;

import android.os.AsyncTask;

public class Downloader extends AsyncTask<Void, Integer, Void> {
    
    public static String BASE_URL = "http://www.mangaeden.com";

    private MangaActivity activity;
    private int listPos;
    private String chapterUrl;
    private ArrayList<String> links = new ArrayList<>(); 

    public Downloader(MangaActivity activity, int listPos, ArrayList<String> links, String chapterUrl) {
        this.activity = activity;
        this.listPos = listPos;
        this.links = links;
        this.chapterUrl = chapterUrl;
    }

    @Override
    protected Void doInBackground(Void... params) {
        publishProgress(0, 0);
        for(int i=0; i < links.size(); i++) {
            publishProgress(-1, i+1, links.size());
            
            String fileUrl = downloadUrl(links.get(i));
            if(fileUrl != null) {
                ViewItem exist = AppController.getInstance().getDBHelper().getPage(links.get(i));
                if(exist != null) {
                    exist.setFileUrl("file://" + fileUrl);
                    AppController.getInstance().getDBHelper().updatePage(exist);
                } else {
                    ViewItem item = new ViewItem(links.get(i));
                    item.setOrder(i);
                    item.setChapterUrl(chapterUrl);
                    item.setFileUrl("file://" + fileUrl);
                    AppController.getInstance().getDBHelper().createPage(item);
                }
            }
            
        }
        
        return null;
    }
    
    @Override
    protected void onProgressUpdate(Integer... values)
    {
        if(values[0].intValue() == 0) { 
            activity.updateDownloadProgress(listPos, values[0].intValue());
        } else if(values[0].intValue() == -1) { 
            activity.updateDownloadProgress(listPos, 0);
            activity.updateDownloadText(listPos, "Downloading " + values[1].intValue() + "/" + values[2].intValue());
        }
    }
    
    @Override
    protected void onPostExecute(Void voids) {
        activity.closeDownloadProgress(listPos);
    }
    
    public String downloadUrl(String imageUrl) {
        File fileForImage = new File(FileUtils.getSaveDir(activity), FileUtils.getImageNameFromUrl(imageUrl));
     
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
        while (true) {
            int count = is.read(bytes, 0, FileUtils.BUFFER_SIZE);            
            if (count == -1) {
                break;
            }
            os.write(bytes, 0, count);
        }
    }

}


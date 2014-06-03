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
import android.util.Log;

public class Downloader extends AsyncTask<Void, Integer, Void> {
    
    public static String BASE_URL = "http://www.mangaeden.com";

    private MangaActivity activity;
    private ArrayList<String> links = new ArrayList<>(); 

    public Downloader(MangaActivity activity, ArrayList<String> links) {
        this.activity = activity;
        this.links = links;
    }

    @Override
    protected Void doInBackground(Void... params) {
        for(int i=0; i < links.size(); i++) {
//            activity.startDownloading("Downloading file " + (i+1) + "/" + links.size());
            
            String fileUrl = downloadUrl(links.get(i));
            if(fileUrl != null) {
                ViewItem exist = AppController.getInstance().getDBHelper().getPage(links.get(i));
                if(exist != null) {
                    exist.setFileUrl(fileUrl);
                    AppController.getInstance().getDBHelper().updatePage(exist);
                }
            }
            
        }
        
        return null;
    }
    
    @Override
    protected void onProgressUpdate(Integer... values)
    {
        activity.updateDownloadProgress(values[0].intValue());
    }
    
    @Override
    protected void onPostExecute(Void voids) {
        activity.closeProgressDialog(activity.getDownloadDialog());
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
            publishProgress((int)((total*100)/lenghtOfFile));
            if (count == -1) {
                break;
            }
            os.write(bytes, 0, count);
        }
    }

}


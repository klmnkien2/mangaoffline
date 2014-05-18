package com.gaogroup.mangaoffline;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.gaogroup.mangaoffline.model.MangaInfo;
import com.gaogroup.mangaoffline.utils.DatabaseHelper;

public class AppController extends Application {

    public static final String TAG = AppController.class.getSimpleName();

    public static final String DATABASE_NAME = "NarutoOffline";
    
    private static MangaInfo manga;
    
    private DatabaseHelper dbHelper;
    
    private RequestQueue mRequestQueue;

    private static AppController mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }
    
    public static MangaInfo getManga() {
    	if(manga == null) {
    		manga  = new MangaInfo(
				"http://www.mangaeden.com/en-manga/naruto/", 
				"http://cdn.mangaeden.com/mangasimg/3c/3c79cf1edfd64e280a3d2d6deaa2e43cbea050cc4b2b491498cf2f85.png",
				"Naruto",
				"Author: KISHIMOTO Masashi",
				"Year of release: 1999",
				"Genres: Shounen, Comedy, Fantasy, Adventure, Drama, Action",
				"Twelve years ago, the powerful Nine-Tailed Demon Fox attacked the ninja village of Konohagakure. "
				+ "The demon was quickly defeated and sealed into the infant Naruto Uzumaki, by the Fourth Hokage "
				+ "who sacrificed his life to protect the village. Now Naruto is the number one knucklehead ninja "
				+ "who's determined to become the next Hokage and gain recognition by everyone who ever doubted him!"
			);
    	}
    	
    	return manga;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }
    
    public DatabaseHelper getDBHelper() {
    	if (dbHelper == null) {
    		dbHelper = new DatabaseHelper(getApplicationContext());
        }

        return dbHelper;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}
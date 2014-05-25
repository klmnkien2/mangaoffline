package com.gaogroup.mangaoffline.utils;

import java.io.File;
import android.content.Context;

/**
 * Provides operations with files
 * 
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public final class FileUtils {

	public static final int BUFFER_SIZE = 8 * 1024; // 8 KB
	private static final String SAVE_FOLDER = "MangaOffline";

	private FileUtils() {
	}
	
	public static String getImageNameFromUrl(String url) {
	    String imageName = url.replaceAll(" ", "_")
	            .replaceAll(":", "_")
	            .replaceAll("/", "_");
	    return imageName;
	}

	public static File getSaveDir(Context context) {
	    File cacheDir;
	    
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), SAVE_FOLDER);
        else
            cacheDir = context.getCacheDir();
        if(!cacheDir.exists())
            cacheDir.mkdirs();
        
        return cacheDir;
    }
}

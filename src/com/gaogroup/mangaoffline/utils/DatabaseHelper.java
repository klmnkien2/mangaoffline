package com.gaogroup.mangaoffline.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.gaogroup.mangaoffline.AppController;
import com.gaogroup.mangaoffline.model.ChapterInfo;
import com.gaogroup.mangaoffline.model.ViewItem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = AppController.DATABASE_NAME;

	// Table Names
	private static final String TABLE_CHAPTER = "chapters";
	private static final String TABLE_PAGE = "pages";

	// Common column names
	private static final String KEY_ID = "id";
	private static final String KEY_CREATED_AT = "created_at";

	// Table Create Statements	
	private static final String CREATE_TABLE_CHAPTER = "CREATE TABLE IF NOT EXISTS "
            + TABLE_CHAPTER + "(" + KEY_ID + " INTEGER PRIMARY KEY," 
            + "mangaUrl  TEXT,"
            + "chapterUrl  TEXT,"
            + "title  TEXT,"
            + "sub  TEXT,"
            + "isRead  INTEGER,"
            + "number  INTEGER,"
            + KEY_CREATED_AT + " DATETIME" + ")";
	
	private static final String CREATE_TABLE_PAGE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_PAGE + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + "chapterUrl  TEXT,"
            + "imageUrl  TEXT,"
            + "fileUrl  TEXT,"
            + "stt  INTEGER" + ")";
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// creating required tables
		db.execSQL(CREATE_TABLE_CHAPTER);
		db.execSQL(CREATE_TABLE_PAGE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// on upgrade drop older tables
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAPTER);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PAGE);

		// create new tables
		onCreate(db);
	}
    
    public long createChapter(ChapterInfo info) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("mangaUrl", info.getMangaUrl());
        values.put("chapterUrl", info.getChapterUrl());
        values.put("title", info.getTitle());
        values.put("sub", info.getSub());
        values.put("isRead", info.getIsRead());
        values.put("number", info.getNumber());

        // insert row
        long inserted_id = db.insert(TABLE_CHAPTER, null, values);

        return inserted_id;
    }
    
    public int readChapter(ChapterInfo info) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("isRead", 1);
        values.put("mangaUrl", info.getMangaUrl());
        values.put("title", info.getTitle());
        values.put("sub", info.getSub());
        values.put("number", info.getNumber());
        values.put("chapterUrl", info.getChapterUrl());
        
        // updating row
        return db.update(TABLE_CHAPTER, values, "chapterUrl = ?",
                new String[] { String.valueOf(info.getChapterUrl()) });
    }
    
    public ChapterInfo getChapterByUrl(String chapterUrl) {
        try {
            String selectQuery = "SELECT  * FROM " + TABLE_CHAPTER + " r  WHERE r.chapterUrl = '" + chapterUrl + "'";

            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery(selectQuery, null);

            if (c.moveToFirst()) {

                ChapterInfo info = new ChapterInfo(
                        c.getString(c.getColumnIndex("mangaUrl")),
                        c.getString(c.getColumnIndex("chapterUrl")),
                        c.getString(c.getColumnIndex("title")),
                        c.getString(c.getColumnIndex("sub")),
                        c.getInt(c.getColumnIndex("isRead")),
                        c.getInt(c.getColumnIndex("number")));

                return info;
            }
            c.close();
        }catch(Exception e) {
            
        }
        return null;
    }
    
    public ChapterInfo getChapterByNumber(int number, String mangaUrl) {
        try {
            String selectQuery = "SELECT  * FROM " + TABLE_CHAPTER + " r  " +
                    "WHERE r.number = " + number + " AND r.mangaUrl = '" + mangaUrl + "'";

            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery(selectQuery, null);

            if (c.moveToFirst()) {

                ChapterInfo info = new ChapterInfo(
                        c.getString(c.getColumnIndex("mangaUrl")),
                        c.getString(c.getColumnIndex("chapterUrl")),
                        c.getString(c.getColumnIndex("title")),
                        c.getString(c.getColumnIndex("sub")),
                        c.getInt(c.getColumnIndex("isRead")),
                        c.getInt(c.getColumnIndex("number")));

                return info;
            }
            c.close();
        }catch(Exception e) {
            
        }
        return null;
    }
    
    public List<ChapterInfo> getAllChapters() {
        List<ChapterInfo> list = new ArrayList<ChapterInfo>();

        String selectQuery = "SELECT  * FROM " + TABLE_CHAPTER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                ChapterInfo info = new ChapterInfo(
                        c.getString(c.getColumnIndex("mangaUrl")),
                        c.getString(c.getColumnIndex("chapterUrl")),
                        c.getString(c.getColumnIndex("title")),
                        c.getString(c.getColumnIndex("sub")),
                        c.getInt(c.getColumnIndex("isRead")),
                        c.getInt(c.getColumnIndex("number")));
                
                list.add(info);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }
    
    public int getMaxReadChapter()
    {
    	SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT MAX(number) AS max_id FROM " + TABLE_CHAPTER + " WHERE isRead = 1";
        Cursor cursor = db.rawQuery(query, null);

        int id = -1;     
        if (cursor.moveToFirst())
        {
            do
            {           
                id = cursor.getInt(0);                  
            } while(cursor.moveToNext());           
        }
        return id;
    }
    
    public long createPage(ViewItem item) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("chapterUrl", item.getChapterUrl());
        values.put("imageUrl", item.getImageUrl());
        values.put("fileUrl", item.getFileUrl());
        values.put("stt", item.getOrder());

        // insert row
        long inserted_id = db.insert(TABLE_PAGE, null, values);

        return inserted_id;
    }
    
    public int updatePage(ViewItem item) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("chapterUrl", item.getChapterUrl());
        values.put("imageUrl", item.getImageUrl());
        values.put("fileUrl", item.getFileUrl());
        values.put("stt", item.getOrder());
        
        // updating row
        return db.update(TABLE_PAGE, values, "chapterUrl = ?",
                new String[] { item.getChapterUrl() });
    }
    
    public ViewItem getPage(String imageUrl) {
        try {
            String selectQuery = "SELECT  * FROM " + TABLE_PAGE + " r  " +
                    "WHERE r.imageUrl = '" + imageUrl + "'";

            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery(selectQuery, null);

            if (c.moveToFirst()) {

                ViewItem item = new ViewItem(c.getString(c.getColumnIndex("imageUrl")));
                item.setChapterUrl(c.getString(c.getColumnIndex("chapterUrl")));
                item.setFileUrl(c.getString(c.getColumnIndex("fileUrl")));
                item.setOrder(c.getInt(c.getColumnIndex("stt")));

                return item;
            }
            c.close();
        }catch(Exception e) {
            
        }
        return null;
    }
    
    public List<ViewItem> getPageInChapter(String chapterUrl) {
    	List<ViewItem> list = new ArrayList<ViewItem>();
        try {
            String selectQuery = "SELECT  * FROM " + TABLE_PAGE + " r  " +
                    "WHERE r.chapterUrl = '" + chapterUrl + "'";

            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery(selectQuery, null);

            if (c.moveToFirst()) {
            	do {
            		ViewItem item = new ViewItem(c.getString(c.getColumnIndex("imageUrl")));
                	item.setChapterUrl(c.getString(c.getColumnIndex("chapterUrl")));
                	item.setFileUrl(c.getString(c.getColumnIndex("fileUrl")));
                	item.setOrder(c.getInt(c.getColumnIndex("stt")));
                    
                    list.add(item);
                } while (c.moveToNext());
            }
            c.close();
        }catch(Exception e) {
            
        }
        return list;
    }
	
	public void deleteChapter(String mangaUrl) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // now delete the tag
        db.delete(TABLE_CHAPTER, "mangaUrl = ?",
                new String[] { mangaUrl });
    }
	

	// closing database
	public void closeDB() {
		SQLiteDatabase db = this.getReadableDatabase();
		if (db != null && db.isOpen())
			db.close();
	}

	/**
	 * get datetime
	 * */
	public String getDateTime() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		Date date = new Date();
		return dateFormat.format(date);
	}
}

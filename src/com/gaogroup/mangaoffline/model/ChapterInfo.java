package com.gaogroup.mangaoffline.model;

public class ChapterInfo
{
    private String mangaUrl;
    private String chapterUrl;
    private String title;
    private String sub;
    private int isRead;
    private int number;
    
    public ChapterInfo(String mangaUrl, String chapterUrl, String title, String sub, int isRead, int number) {
        super();
        this.mangaUrl = mangaUrl;
        this.chapterUrl = chapterUrl;
        this.title = title;
        this.sub = sub;
        this.isRead = isRead;
        this.number = number;
    }
    public String getMangaUrl() {
        return mangaUrl;
    }
    public void setMangaUrl(String mangaUrl) {
        this.mangaUrl = mangaUrl;
    }
    public String getChapterUrl() {
        return chapterUrl;
    }
    public void setChapterUrl(String chapterUrl) {
        this.chapterUrl = chapterUrl;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getSub() {
        return sub;
    }
    public void setSub(String sub) {
        this.sub = sub;
    }
    public int getIsRead() {
        return isRead;
    }
    public void setIsRead(int isRead) {
        this.isRead = isRead;
    }
    public int getNumber() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
    }    
    
}

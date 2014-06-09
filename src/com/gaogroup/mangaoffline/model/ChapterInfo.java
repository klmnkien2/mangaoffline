package com.gaogroup.mangaoffline.model;

import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ChapterInfo
{
    private String mangaUrl;
    private String chapterUrl;
    private String title;
    private String sub;
    private int isRead;
    private int downloaded;
    private int number;

    private volatile boolean isDownloading = false;
    private volatile Integer mProgress;
    private volatile ProgressBar mProgressBar;
    private volatile TextView mProgressText;
    private volatile ImageView downloadButton;

    public ChapterInfo(String mangaUrl, String chapterUrl, String title, String sub, int isRead, int number) {
        super();
        this.mangaUrl = mangaUrl;
        this.chapterUrl = chapterUrl;
        this.title = title;
        this.sub = sub;
        this.isRead = isRead;
        this.number = number;
        downloaded = 0;

        mProgress = 0;
        mProgressBar = null;
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
    public boolean isDownloading() {
        return isDownloading;
    }
    public void setDownloading(boolean isDownloading) {
        this.isDownloading = isDownloading;
    }
    public Integer getProgress() {
        return mProgress;
    }
    public void setProgress(Integer mProgress) {
        this.mProgress = mProgress;
    }
    public ProgressBar getProgressBar() {
        return mProgressBar;
    }
    public void setProgressBar(ProgressBar mProgressBar) {
        this.mProgressBar = mProgressBar;
    }
    public int getDownloaded() {
        return downloaded;
    }
    public void setDownloaded(int downloaded) {
        this.downloaded = downloaded;
    }
    public ImageView getDownloadButton() {
        return downloadButton;
    }
    public void setDownloadButton(ImageView downloadButton) {
        this.downloadButton = downloadButton;
    }    
    public TextView getProgressText() {
        return mProgressText;
    }
    public void setProgressText(TextView mProgressText) {
        this.mProgressText = mProgressText;
    }
}

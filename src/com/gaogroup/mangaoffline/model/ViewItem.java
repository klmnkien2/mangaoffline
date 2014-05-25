package com.gaogroup.mangaoffline.model;

public class ViewItem
{
    private String title;
    private String chapterUrl;
    private String imageUrl;
    private String fileUrl;
    private int order;
    private boolean loadingOnly;
    private boolean networkTrouble;

    public ViewItem(String imageUrl)
    {
        this.imageUrl = imageUrl;
        this.loadingOnly = false;
        this.networkTrouble = false;
    }

    public String getTitle()
    {
        return title;
    }
    
    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }

    public boolean isLoadingOnly() {
        return loadingOnly;
    }

    public void setLoadingOnly(boolean loadingOnly) {
        this.loadingOnly = loadingOnly;
    }

    public boolean isNetworkTrouble() {
        return networkTrouble;
    }

    public void setNetworkTrouble(boolean networkTrouble) {
        this.networkTrouble = networkTrouble;
    }

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getChapterUrl() {
		return chapterUrl;
	}

	public void setChapterUrl(String chapterUrl) {
		this.chapterUrl = chapterUrl;
	}    
    
}

package com.gaogroup.mangaoffline.model;

public class MangaInfo
{
    private String mangaUrl;
    private String imageUrl;
    private String name;
    private String author;
    private String yearOfrelease;
    private String genres;
    private String description;
	public MangaInfo(String mangaUrl, String imageUrl, String name,
			String author, String yearOfrelease, String genres,
			String description) {
		super();
		this.mangaUrl = mangaUrl;
		this.imageUrl = imageUrl;
		this.name = name;
		this.author = author;
		this.yearOfrelease = yearOfrelease;
		this.genres = genres;
		this.description = description;
	}
	public String getMangaUrl() {
		return mangaUrl;
	}
	public void setMangaUrl(String mangaUrl) {
		this.mangaUrl = mangaUrl;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getYearOfrelease() {
		return yearOfrelease;
	}
	public void setYearOfrelease(String yearOfrelease) {
		this.yearOfrelease = yearOfrelease;
	}
	public String getGenres() {
		return genres;
	}
	public void setGenres(String genres) {
		this.genres = genres;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
    
    
    
}

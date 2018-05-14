package org.checklist.comics.comicschecklist.database.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import org.checklist.comics.comicschecklist.model.Comic;

import java.util.Date;

@Entity(tableName = "comics", indices = {@Index(value = {"comic_name", "release_date"},
        unique = true)})
public class ComicEntity implements Comic {

    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "comic_name")
    private String name;
    @ColumnInfo(name = "release_date")
    private Date releaseDate;
    private String description;
    private String price;
    private String feature;
    private String cover;
    private String editor;
    private boolean isFavorite;
    private boolean isOnCart;
    private String URL;

    public ComicEntity(String name, Date releaseDate,
                       String description, String price, String feature,
                       String cover, String editor, boolean isFavorite,
                       boolean isOnCart, String URL) {
        this.name = name;
        this.releaseDate = releaseDate;
        this.description = description;
        this.price = price;
        this.feature = feature;
        this.cover = cover;
        this.editor = editor;
        this.isFavorite = isFavorite;
        this.isOnCart = isOnCart;
        this.URL = URL;
    }

    public ComicEntity(Comic comic) {
        this.name = comic.getName();
        this.releaseDate = comic.getReleaseDate();
        this.description = comic.getDescription();
        this.price = comic.getPrice();
        this.feature = comic.getFeature();
        this.cover = comic.getCover();
        this.editor = comic.getEditor();
        this.isFavorite = comic.isFavorite();
        this.isOnCart = comic.isOnCart();
        this.URL = comic.getURL();
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Date getReleaseDate() {
        return releaseDate;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getPrice() {
        return price;
    }

    @Override
    public String getFeature() {
        return feature;
    }

    @Override
    public String getCover() {
        return cover;
    }

    @Override
    public String getEditor() {
        return editor;
    }

    @Override
    public boolean isFavorite() {
        return isFavorite;
    }

    @Override
    public boolean isOnCart() {
        return isOnCart;
    }

    @Override
    public String getURL() {
        return URL;
    }
}

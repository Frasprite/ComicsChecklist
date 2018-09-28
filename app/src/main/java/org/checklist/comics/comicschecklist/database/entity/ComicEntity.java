package org.checklist.comics.comicschecklist.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "comics", indices = {@Index(value = {"comic_name", "release_date"},
        unique = true)})
public class ComicEntity {

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

    public ComicEntity(ComicEntity comic) {
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return price;
    }

    public String getFeature() {
        return feature;
    }

    public String getCover() {
        return cover;
    }

    public String getEditor() {
        return editor;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public boolean isOnCart() {
        return isOnCart;
    }

    public String getURL() {
        return URL;
    }

    public void setFavorite(boolean favorite) {
        this.isFavorite = favorite;
    }

    public void setToCart(boolean favorite) {
        this.isOnCart = favorite;
    }
}

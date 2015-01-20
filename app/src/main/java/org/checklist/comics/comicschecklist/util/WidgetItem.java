package org.checklist.comics.comicschecklist.util;

/**
 * Created by Francesco Bevilacqua on 05/01/2015.
 * This code is part of Comics Checklist project.
 */
public class WidgetItem {
    public Integer _comicID;
    public String _name;
    public String _release;

    public WidgetItem(Integer _comicID, String _name, String _release) {
        this._comicID = _comicID;
        this._name = _name;
        this._release = _release;
    }
}

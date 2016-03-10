package org.checklist.comics.comicschecklist.util;

/**
 * Created by Francesco Bevilacqua on 05/01/2015.
 * This code is part of Comics Checklist project.
 */
public class WidgetItem {
    public final Integer _comicID;
    public final String _name;
    public final String _release;

    public WidgetItem(Integer _comicID, String _name, String _release) {
        this._comicID = _comicID;
        this._name = _name;
        this._release = _release;
    }
}

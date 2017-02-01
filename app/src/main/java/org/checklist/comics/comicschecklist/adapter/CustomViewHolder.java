package org.checklist.comics.comicschecklist.adapter;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import org.checklist.comics.comicschecklist.database.ComicDatabase;

public class CustomViewHolder extends RecyclerView.ViewHolder {

    public TextView textViewTitle;
    public TextView textViewReleaseDate;

    public CustomViewHolder(View itemView) {
        super(itemView);
        textViewTitle = (TextView) itemView.findViewById(android.R.id.text1);
        textViewReleaseDate = (TextView) itemView.findViewById(android.R.id.text2);
    }

    public void setData(Cursor cursor) {
        textViewTitle.setText(cursor.getString(cursor.getColumnIndex(ComicDatabase.COMICS_NAME_KEY)));
        textViewReleaseDate.setText(cursor.getString(cursor.getColumnIndex(ComicDatabase.COMICS_RELEASE_KEY)));
    }
}

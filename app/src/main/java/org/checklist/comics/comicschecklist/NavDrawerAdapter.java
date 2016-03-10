package org.checklist.comics.comicschecklist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Adapter used for the list in DrawerLayout.
 **/
public class NavDrawerAdapter extends ArrayAdapter<String> {
    private final int resource;
    private final LayoutInflater inflater;

    public NavDrawerAdapter(Context context, int resourceId, String[] mEditorTitles) {
        super(context, resourceId, mEditorTitles);
        resource = resourceId;
        inflater = LayoutInflater.from(context);
    }

    /**
     * This method set the section info.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        String editore = getItem(position);

        EditorViewCache viewCache;

        if (convertView == null) {
            convertView = inflater.inflate(resource, null);
            viewCache = new EditorViewCache(convertView);
            convertView.setTag(viewCache);
        } else {
            viewCache = (EditorViewCache) convertView.getTag();
        }

        // Set icon with related method
        TextView editorName = viewCache.getTextViewName();
        editorName.setCompoundDrawablesWithIntrinsicBounds(setIcon(editore), 0, 0, 0);
        editorName.setCompoundDrawablePadding(20);
        editorName.setText(editore);

        View separator = viewCache.getSeparatorView();
        if (editore.equalsIgnoreCase(getContext().getResources().getString(R.string.title_section3)) ||
                editore.equalsIgnoreCase(getContext().getResources().getString(R.string.title_section9)))
            separator.setVisibility(View.VISIBLE);

        return convertView;
    }

    /**
     * This method set the icon for a specific section.
     */
    private int setIcon(String editorName) {
        int resId = R.drawable.ic_action_book;
        if (editorName.equalsIgnoreCase(getContext().getResources().getString(R.string.title_section1)))
            resId = R.drawable.ic_action_star_10;
        else if (editorName.equalsIgnoreCase(getContext().getResources().getString(R.string.title_section2)))
            resId = R.drawable.ic_action_cart;
        else if (editorName.equalsIgnoreCase(getContext().getResources().getString(R.string.title_section9)))
            resId = R.drawable.ic_action_settings;
        else if (editorName.equalsIgnoreCase(getContext().getResources().getString(R.string.title_section10)))
            resId = R.drawable.ic_action_plus_1;
        else if (editorName.equalsIgnoreCase(getContext().getResources().getString(R.string.title_section11)))
            resId = R.drawable.ic_action_help;
        else if (editorName.equalsIgnoreCase(getContext().getResources().getString(R.string.title_section12)))
            resId = R.drawable.ic_action_info;
        return resId;
    }

    /**
     * Cache of elements which compose the list.
     */
    public class EditorViewCache {

        private View baseView;
        private TextView textViewName;
        private View separatorView;

        public EditorViewCache(View baseView) {
            this.baseView = baseView;
        }

        public TextView getTextViewName() {
            if (textViewName == null) {
                textViewName = (TextView) baseView.findViewById(R.id.editorName);
            }
            return textViewName;
        }

        public View getSeparatorView() {
            if (separatorView == null) {
                separatorView = baseView.findViewById(R.id.separator);
            }
            return separatorView;
        }
    }
}

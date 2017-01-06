package org.checklist.comics.comicschecklist;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.checklist.comics.comicschecklist.database.ComicDatabaseManager;
import org.checklist.comics.comicschecklist.provider.ComicContentProvider;
import org.checklist.comics.comicschecklist.database.ComicDatabase;
import org.checklist.comics.comicschecklist.service.WidgetService;
import org.checklist.comics.comicschecklist.util.Constants;
import org.checklist.comics.comicschecklist.util.DateCreator;

import java.util.Calendar;
import java.util.Date;

/**
 * A fragment representing a single Comic detail screen.
 * This fragment is either contained in a {@link ActivityMain}
 * in two-pane mode (on tablets) or a {@link ActivityDetail}
 * on handsets.
 */
public class FragmentDetail extends Fragment {

    private static final String TAG = FragmentDetail.class.getSimpleName();

    /**
     * The fragment arguments representing the item ID that this fragment represents.
     */
    public static final String ARG_ACTIVITY_LAUNCHED = "activity_launched";

    // The comic content this fragment is presenting.
    private String mComicName;
    private String mComicRelease;
    private String mFavorite;
    private String mCart;
    private String mComicEditor;
    private String mComicURL;
    private long mComicId = -1;
    private boolean mActivityDetailLaunched = false;

    private Menu mMenu;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FragmentDetail() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate - start");

        if (getArguments().containsKey(Constants.ARG_COMIC_ID)) {
            // Load comic content specified by the fragment arguments from ComicContentProvider.
            // For better performance, use a Loader to load content from a content provider.
            mComicId = getArguments().getLong(Constants.ARG_COMIC_ID);
            mActivityDetailLaunched = getArguments().getBoolean(ARG_ACTIVITY_LAUNCHED);
            Log.i(TAG, "Loading comic with ID " + mComicId);
        }

        Log.i(TAG, "Fragment launched by ActivityDetail " + mActivityDetailLaunched);
        setHasOptionsMenu(mActivityDetailLaunched);
        Log.v(TAG, "onCreate - end");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView - start");
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        // Show the comic contents
        if (mComicId > -1) {
            Uri uri = Uri.parse(ComicContentProvider.CONTENT_URI + "/" + mComicId);
            String[] projection = {ComicDatabase.ID, ComicDatabase.COMICS_NAME_KEY, ComicDatabase.COMICS_RELEASE_KEY,
                    ComicDatabase.COMICS_DATE_KEY, ComicDatabase.COMICS_DESCRIPTION_KEY, ComicDatabase.COMICS_PRICE_KEY,
                    ComicDatabase.COMICS_FEATURE_KEY, ComicDatabase.COMICS_COVER_KEY, ComicDatabase.COMICS_EDITOR_KEY,
                    ComicDatabase.COMICS_FAVORITE_KEY, ComicDatabase.COMICS_CART_KEY, ComicDatabase.COMICS_URL_KEY};
            Cursor mCursor = ComicDatabaseManager.query(getActivity(), uri, projection, null, null, null);
            mCursor.moveToFirst();
            mComicName = mCursor.getString(mCursor.getColumnIndex(ComicDatabase.COMICS_NAME_KEY));
            mComicRelease = mCursor.getString(mCursor.getColumnIndex(ComicDatabase.COMICS_RELEASE_KEY));
            String mComicDescription = mCursor.getString(mCursor.getColumnIndex(ComicDatabase.COMICS_DESCRIPTION_KEY));
            if (mComicDescription.length() == 0)
                mComicDescription = "N.D";
            String mComicPrice = mCursor.getString(mCursor.getColumnIndex(ComicDatabase.COMICS_PRICE_KEY));
            if (mComicPrice.length() == 0)
                mComicPrice = "N.D.";
            String mComicFeature = mCursor.getString(mCursor.getColumnIndex(ComicDatabase.COMICS_FEATURE_KEY));
            if (mComicFeature.length() == 0)
                mComicFeature = "N.D.";
            mFavorite = mCursor.getString(mCursor.getColumnIndex(ComicDatabase.COMICS_FAVORITE_KEY));
            mCart = mCursor.getString(mCursor.getColumnIndex(ComicDatabase.COMICS_CART_KEY));
            mComicEditor = mCursor.getString(mCursor.getColumnIndex(ComicDatabase.COMICS_EDITOR_KEY));
            mComicURL = mCursor.getString(mCursor.getColumnIndex(ComicDatabase.COMICS_URL_KEY));
            mCursor.close();
            // Populating view
            TextView titleTextView = ((TextView) rootView.findViewById(R.id.title_text_view));
            titleTextView.setText(mComicName);
            ((TextView) rootView.findViewById(R.id.release_text_view)).setText(mComicRelease);
            ((TextView) rootView.findViewById(R.id.description_text_view)).setText(mComicDescription);
            ((TextView) rootView.findViewById(R.id.feature_text_view)).setText(mComicFeature);
            ((TextView) rootView.findViewById(R.id.price_text_view)).setText(mComicPrice);
            ImageView coverView = (ImageView) rootView.findViewById(R.id.cover_image_view);
            Picasso.with(getActivity())
                    .load(R.drawable.comic_placeholder)
                    .placeholder(R.drawable.comic_placeholder)
                    .error(R.drawable.comic_placeholder)
                    .resize(250, 383)
                    .into(coverView);
        }

        if (!mActivityDetailLaunched) {
            Log.d(TAG, "Inflating detail toolbar");
            // Create detail toolbar
            Toolbar toolbarDetail = (Toolbar) getActivity().findViewById(R.id.toolbarDetail);
            // Inflate a menu to be displayed in the toolbar
            if (toolbarDetail != null) {
                toolbarDetail.inflateMenu(R.menu.menu_detail);
                mMenu = toolbarDetail.getMenu();
                updateMenuItems(mFavorite, mCart);

                toolbarDetail.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        return manageItemSelected(item);
                    }
                });
            }
        }

        Log.v(TAG, "onCreateView - end");
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mActivityDetailLaunched) {
            // Fragment is launched from ActivityDetail, inflate own menu
            inflater.inflate(R.menu.menu_detail, menu);
            mMenu = menu;
            updateMenuItems(mFavorite, mCart);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return manageItemSelected(item);
    }

    private void updateMenuItems(String favoriteFlag, String cartFlag) {
        if (favoriteFlag.equalsIgnoreCase("no")) {
            // Comic can be added to favorite
            mMenu.getItem(2).setVisible(true);
            mMenu.getItem(3).setVisible(false);
        } else {
            // Comic can be removed from favorite
            mMenu.getItem(2).setVisible(false);
            mMenu.getItem(3).setVisible(true);
        }

        if (cartFlag.equalsIgnoreCase("no")) {
            // Comic can be added to cart
            mMenu.getItem(4).setVisible(true);
            mMenu.getItem(5).setVisible(false);
        } else {
            // Comic can be removed from cart
            mMenu.getItem(4).setVisible(false);
            mMenu.getItem(5).setVisible(true);
        }

        if (Constants.Sections.getEditorFromName(mComicEditor) == Constants.Sections.CART) {
            mMenu.getItem(1).setVisible(false);
        } else {
            mMenu.getItem(1).setVisible(true);
        }
    }

    private boolean manageItemSelected(MenuItem item) {
        String mSelectionClause;
        String[] mSelectionArgs;
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.calendar:
                try {
                    Log.i(TAG, "Add event on calendar");
                    // ACTION_INSERT does not work on all phones; use Intent.ACTION_EDIT in this case
                    Intent intent = new Intent(Intent.ACTION_INSERT);
                    intent.setType("vnd.android.cursor.item/event");
                    intent.putExtra(CalendarContract.Events.TITLE, mComicName);
                    intent.putExtra(CalendarContract.Events.DESCRIPTION, getString(R.string.calendar_release));

                    // Setting dates
                    long timeInMillis = DateCreator.getTimeInMillis(mComicRelease);
                    intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, timeInMillis);
                    intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, timeInMillis);

                    // Make it a full day event
                    intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);

                    // Making it private and shown as busy
                    intent.putExtra(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_PRIVATE);
                    intent.putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), R.string.calendar_error, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.comicSite:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mComicURL));
                startActivity(browserIntent);
                return true;
            case R.id.favorite:
                if (mFavorite.equalsIgnoreCase("no")) {
                    Log.i(TAG, "Add comic to favorite");
                    // Add comic to favorite
                    ContentValues mUpdateValues = new ContentValues();
                    mUpdateValues.put(ComicDatabase.COMICS_FAVORITE_KEY, "yes");
                    mFavorite = "yes";
                    // Defines selection criteria for the rows you want to update
                    mSelectionClause = ComicDatabase.ID +  "=?";
                    mSelectionArgs = new String[]{String.valueOf(mComicId)};
                    ComicDatabaseManager.update(getActivity(), mUpdateValues, mSelectionClause, mSelectionArgs);
                    Toast.makeText(getActivity(), getResources().getString(R.string.comic_added_favorite), Toast.LENGTH_SHORT).show();
                } else {
                    Log.v(TAG, "Comic already on favorite");
                }

                updateMenuItems(mFavorite, mCart);

                WidgetService.updateWidget(getActivity());
                return true;
            case R.id.remove_favorite:
                if (mFavorite.equalsIgnoreCase("yes")) {
                    Log.i(TAG, "Delete from favorite");
                    // Delete from favorite
                    ContentValues mUpdateValues = new ContentValues();
                    mUpdateValues.put(ComicDatabase.COMICS_FAVORITE_KEY, "no");
                    mFavorite = "no";
                    // Defines selection criteria for the rows you want to update
                    mSelectionClause = ComicDatabase.ID +  "=?";
                    mSelectionArgs = new String[]{String.valueOf(mComicId)};
                    ComicDatabaseManager.update(getActivity(), mUpdateValues, mSelectionClause, mSelectionArgs);
                    Toast.makeText(getActivity(), getResources().getString(R.string.comic_deleted_favorite), Toast.LENGTH_SHORT).show();
                } else {
                    Log.v(TAG, "Comic already deleted from favorite");
                }

                updateMenuItems(mFavorite, mCart);

                WidgetService.updateWidget(getActivity());
                return true;
            case R.id.buy:
                if (mCart.equalsIgnoreCase("no")) {
                    Log.i(TAG, "Update entry on comic database: add to cart");
                    // Update entry on comic database
                    ContentValues mUpdateValues = new ContentValues();
                    mUpdateValues.put(ComicDatabase.COMICS_CART_KEY, "yes");
                    mCart = "yes";
                    // Defines selection criteria for the rows you want to update
                    mSelectionClause = ComicDatabase.ID +  "=?";
                    mSelectionArgs = new String[]{String.valueOf(mComicId)};
                    ComicDatabaseManager.update(getActivity(), mUpdateValues, mSelectionClause, mSelectionArgs);
                    Toast.makeText(getActivity(), getResources().getString(R.string.comic_added_cart), Toast.LENGTH_SHORT).show();
                } else {
                    Log.v(TAG, "Comic already on cart");
                }

                updateMenuItems(mFavorite, mCart);

                WidgetService.updateWidget(getActivity());
                return true;
            case R.id.remove_buy:
                if (mCart.equalsIgnoreCase("yes")) {
                    Log.i(TAG, "Update entry on comic database: remove from cart");
                    // Update entry on comic database
                    ContentValues mUpdateValues = new ContentValues();
                    mUpdateValues.put(ComicDatabase.COMICS_CART_KEY, "no");
                    mCart = "no";
                    // Defines selection criteria for the rows you want to update
                    mSelectionClause = ComicDatabase.ID +  "=?";
                    mSelectionArgs = new String[]{String.valueOf(mComicId)};
                    ComicDatabaseManager.update(getActivity(), mUpdateValues, mSelectionClause, mSelectionArgs);
                    Toast.makeText(getActivity(), getResources().getString(R.string.comic_deleted_cart), Toast.LENGTH_SHORT).show();
                } else {
                    Log.v(TAG, "Comic already removed from cart");
                }

                updateMenuItems(mFavorite, mCart);

                WidgetService.updateWidget(getActivity());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

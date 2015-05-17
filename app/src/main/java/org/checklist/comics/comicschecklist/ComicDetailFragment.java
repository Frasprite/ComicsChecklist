package org.checklist.comics.comicschecklist;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;

import org.checklist.comics.comicschecklist.provider.ComicContentProvider;
import org.checklist.comics.comicschecklist.database.ComicDatabase;
import org.checklist.comics.comicschecklist.provider.WidgetProvider;
import org.checklist.comics.comicschecklist.util.Constants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * A fragment representing a single Comic detail screen.
 * This fragment is either contained in a {@link ComicListActivity}
 * in two-pane mode (on tablets) or a {@link ComicDetailActivity}
 * on handsets.
 */
public class ComicDetailFragment extends Fragment {
    /**
     * The fragment arguments representing the item ID that this fragment represents.
     */
    public static final String ARG_COMIC_ID = "comic_id";
    public static final String ARG_SECTION = "section";

    // The comic content this fragment is presenting.
    private String mComicName;
    private String mComicRelease;
    private String mComicDescription;
    private String mComicPrice;
    private String mComicFeature;
    private String mComicCover;
    private String mComicEditor;
    private String mFavorite;
    private String mCart;
    private boolean mUserLearnedSliding;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ComicDetailFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_COMIC_ID)) {
            // Load comic content specified by the fragment arguments from ComicContentProvider.
            // For better performance, use a Loader to load content from a content provider.
            long mComicId = getArguments().getLong(ARG_COMIC_ID);
            Uri uri = Uri.parse(ComicContentProvider.CONTENT_URI + "/" + mComicId);
            String[] projection = {ComicDatabase.ID, ComicDatabase.COMICS_NAME_KEY, ComicDatabase.COMICS_RELEASE_KEY,
                    ComicDatabase.COMICS_DATE_KEY, ComicDatabase.COMICS_DESCRIPTION_KEY, ComicDatabase.COMICS_PRICE_KEY,
                    ComicDatabase.COMICS_FEATURE_KEY, ComicDatabase.COMICS_COVER_KEY, ComicDatabase.COMICS_EDITOR_KEY,
                    ComicDatabase.COMICS_FAVORITE_KEY, ComicDatabase.COMICS_CART_KEY};
            Cursor mCursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
            mCursor.moveToFirst();
            mComicName = mCursor.getString(mCursor.getColumnIndex(ComicDatabase.COMICS_NAME_KEY));
            mComicRelease = mCursor.getString(mCursor.getColumnIndex(ComicDatabase.COMICS_RELEASE_KEY));
            //mComicDate = ;
            mComicDescription = mCursor.getString(mCursor.getColumnIndex(ComicDatabase.COMICS_DESCRIPTION_KEY));
            if (mComicDescription.length() == 0)
                mComicDescription = "N.D";
            mComicPrice = mCursor.getString(mCursor.getColumnIndex(ComicDatabase.COMICS_PRICE_KEY));
            if (mComicPrice.length() == 0)
                mComicPrice = "N.D.";
            mComicFeature = mCursor.getString(mCursor.getColumnIndex(ComicDatabase.COMICS_FEATURE_KEY));
            if (mComicFeature.length() == 0)
                mComicFeature = "N.D.";
            mComicCover = mCursor.getString(mCursor.getColumnIndex(ComicDatabase.COMICS_COVER_KEY));
            mComicEditor = mCursor.getString(mCursor.getColumnIndex(ComicDatabase.COMICS_EDITOR_KEY));
            mFavorite = mCursor.getString(mCursor.getColumnIndex(ComicDatabase.COMICS_FAVORITE_KEY));
            mCart = mCursor.getString(mCursor.getColumnIndex(ComicDatabase.COMICS_CART_KEY));
            mCursor.close();
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedSliding = sp.getBoolean(Constants.PREF_USER_LEARNED_SLIDING_UP, false);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_comic_detail, container, false);

        // Show the comic contents.
        if (mComicName != null) {
            ((TextView) rootView.findViewById(R.id.title_text_view)).setText(mComicName);
            ((TextView) rootView.findViewById(R.id.release_text_view)).setText(mComicRelease);
            ((TextView) rootView.findViewById(R.id.description_text_view)).setText(mComicDescription);
            ((TextView) rootView.findViewById(R.id.feature_text_view)).setText(mComicFeature);
            ((TextView) rootView.findViewById(R.id.price_text_view)).setText(mComicPrice);
            ImageView coverView = (ImageView) rootView.findViewById(R.id.cover_image_view);
            Picasso.with(getActivity())
                   .load(mComicCover)
                   .placeholder(R.drawable.comic_placeholder)
                   .error(R.drawable.comic_placeholder)
                   //.fit()//.centerCrop()
                   .resize(250,383)//.resize(300, 394)
                   .into(coverView);
        }

        SlidingUpPanelLayout layout = (SlidingUpPanelLayout) rootView.findViewById(R.id.sliding_layout);
        layout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {

            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                // Anything you put here is going to happen if user do a slide
            }

            @Override
            public void onPanelCollapsed(View panel) {
                // Anything you put here is going to happen if the view is closed
            }

            @Override
            public void onPanelExpanded(View panel) {
                // Anything you put here is going to happen if the view is open
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                sp.edit().putBoolean(Constants.PREF_USER_LEARNED_SLIDING_UP, true).apply();
            }

            @Override
            public void onPanelAnchored(View panel) {
                // Anything you put here is going to happen if the view is anchored
            }

            @Override
            public void onPanelHidden(View view) {
                // Anything you put here is going to happen if the view is hidden
            }
        });

        ImageView arrowSx = (ImageView) rootView.findViewById(R.id.go_up_sx);
        ImageView arrowDx = (ImageView) rootView.findViewById(R.id.go_up_dx);

        if (!mUserLearnedSliding) {
            int imagesToShow[] = {R.drawable.ic_action_drag_up, R.drawable.ic_action_orange_drag_up};

            animate(arrowSx, imagesToShow, 0, true);
            animate(arrowDx, imagesToShow, 0, true);
        } else {
            arrowDx.setVisibility(View.GONE);
            arrowSx.setVisibility(View.GONE);
        }

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_preview, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.calendar:
                try {
                    // ACTION_INSERT does not work on all phones; use Intent.ACTION_EDIT in this case
                    Intent intent = new Intent(Intent.ACTION_INSERT);
                    intent.setType("vnd.android.cursor.item/event");
                    intent.putExtra(CalendarContract.Events.TITLE, mComicName);
                    intent.putExtra(CalendarContract.Events.DESCRIPTION, getString(R.string.calendar_release));

                    // Setting dates
                    Date date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(mComicRelease);
                    GregorianCalendar calDate = new GregorianCalendar();
                    calDate.setTime(date);
                    intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                            calDate.getTimeInMillis());
                    intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                            calDate.getTimeInMillis());

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
            case R.id.favorite:
                if (mFavorite.equalsIgnoreCase("no")) {
                    // Add comic to favorite
                    ContentValues mUpdateValues = new ContentValues();
                    mUpdateValues.put(ComicDatabase.COMICS_FAVORITE_KEY, "yes");
                    mFavorite = "yes";
                    // Defines selection criteria for the rows you want to update
                    String mSelectionClause = ComicDatabase.COMICS_NAME_KEY +  "=?";
                    String[] mSelectionArgs = {mComicName};
                    getActivity().getContentResolver().update(ComicContentProvider.CONTENT_URI, mUpdateValues, mSelectionClause, mSelectionArgs);
                    Toast.makeText(getActivity(), getResources().getString(R.string.comic_added_favorite), Toast.LENGTH_SHORT).show();
                } else {
                    // Delete from favorite
                    ContentValues mUpdateValues = new ContentValues();
                    mUpdateValues.put(ComicDatabase.COMICS_FAVORITE_KEY, "no");
                    mFavorite = "no";
                    // Defines selection criteria for the rows you want to update
                    String mSelectionClause = ComicDatabase.COMICS_NAME_KEY +  "=?";
                    String[] mSelectionArgs = {mComicName};
                    getActivity().getContentResolver().update(ComicContentProvider.CONTENT_URI, mUpdateValues, mSelectionClause, mSelectionArgs);
                    Toast.makeText(getActivity(), getResources().getString(R.string.comic_deleted_favorite), Toast.LENGTH_SHORT).show();
                }

                // TODO Update widget
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getActivity());
                int appWidgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(getActivity(), WidgetProvider.class));
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.list);

                return true;
            case R.id.buy:
                if (mCart.equalsIgnoreCase("no")) {
                    // Update entry on comic database
                    ContentValues mUpdateValues = new ContentValues();
                    mUpdateValues.put(ComicDatabase.COMICS_CART_KEY, "yes");
                    mCart = "yes";
                    // Defines selection criteria for the rows you want to update
                    String mSelectionClause = ComicDatabase.COMICS_NAME_KEY +  "=?";
                    String[] mSelectionArgs = {mComicName};
                    getActivity().getContentResolver().update(ComicContentProvider.CONTENT_URI, mUpdateValues, mSelectionClause, mSelectionArgs);

                    Toast.makeText(getActivity(), getResources().getString(R.string.comic_added_cart), Toast.LENGTH_SHORT).show();
                } else {
                    // Update entry on comic database
                    ContentValues mUpdateValues = new ContentValues();
                    mUpdateValues.put(ComicDatabase.COMICS_CART_KEY, "no");
                    mCart = "no";
                    // Defines selection criteria for the rows you want to update
                    String mSelectionClause = ComicDatabase.COMICS_NAME_KEY +  "=?";
                    String[] mSelectionArgs = {mComicName};
                    getActivity().getContentResolver().update(ComicContentProvider.CONTENT_URI, mUpdateValues, mSelectionClause, mSelectionArgs);

                    Toast.makeText(getActivity(), getResources().getString(R.string.comic_deleted_cart), Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This method will fade out the arrow on info panel.
     * @param imageView The View which displays the images
     * @param images Holds R references to the images to display
     * @param imageIndex index of the first image to show in images[]
     * @param forever If true then after the last image it starts all over again with the first image resulting in an infinite loop
     */
    private void animate(final ImageView imageView, final int images[], final int imageIndex, final boolean forever) {

        int fadeInDuration = 500; // Configure time values
        int timeBetween = 3000;
        int fadeOutDuration = 1000;

        imageView.setVisibility(View.INVISIBLE);    // Visible or invisible by default - this will apply when the animation ends
        imageView.setImageResource(images[imageIndex]);

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(fadeInDuration);

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setStartOffset(fadeInDuration + timeBetween);
        fadeOut.setDuration(fadeOutDuration);

        AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(fadeIn);
        animation.addAnimation(fadeOut);
        animation.setRepeatCount(1);
        imageView.setAnimation(animation);

        animation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                if (images.length - 1 > imageIndex) {
                    animate(imageView, images, imageIndex + 1, forever); // Calls itself until it gets to the end of the array
                } else {
                    if (forever) {
                        animate(imageView, images, 0, true);  // Calls itself to start the animation all over again in a loop if forever = true
                    }
                }
            }

            public void onAnimationRepeat(Animation animation) {}

            public void onAnimationStart(Animation animation) {}
        });
    }
}

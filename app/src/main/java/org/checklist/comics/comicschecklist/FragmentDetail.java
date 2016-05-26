package org.checklist.comics.comicschecklist;

import android.animation.ValueAnimator;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import org.checklist.comics.comicschecklist.database.ComicDatabaseManager;
import org.checklist.comics.comicschecklist.provider.ComicContentProvider;
import org.checklist.comics.comicschecklist.database.ComicDatabase;
import org.checklist.comics.comicschecklist.service.WidgetService;
import org.checklist.comics.comicschecklist.util.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * A fragment representing a single Comic detail screen.
 * This fragment is either contained in a {@link ActivityMain}
 * in two-pane mode (on tablets) or a {@link ActivityDetail}
 * on handsets.
 */
public class FragmentDetail extends Fragment implements SlidingUpPanelLayout.PanelSlideListener {

    private static final String TAG = FragmentDetail.class.getSimpleName();

    /**
     * The fragment arguments representing the item ID that this fragment represents.
     */
    public static final String ARG_ACTIVITY_LAUNCHED = "activity_launched";

    // The comic content this fragment is presenting.
    private String mComicName, mComicDescription, mComicCover, mComicFeature, mComicPrice, mComicRelease, mFavorite, mCart;
    private boolean mUserLearnedSliding;
    private long mComicId = -1;
    private boolean mActivityDetailLaunched = false;

    private Menu mMenu;
    private TextView titleTextView;

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

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedSliding = sp.getBoolean(Constants.PREF_USER_LEARNED_SLIDING_UP, false);

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
                    ComicDatabase.COMICS_FAVORITE_KEY, ComicDatabase.COMICS_CART_KEY};
            Cursor mCursor = ComicDatabaseManager.query(getActivity(), uri, projection, null, null, null);
            mCursor.moveToFirst();
            mComicName = mCursor.getString(mCursor.getColumnIndex(ComicDatabase.COMICS_NAME_KEY));
            mComicRelease = mCursor.getString(mCursor.getColumnIndex(ComicDatabase.COMICS_RELEASE_KEY));
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
            mFavorite = mCursor.getString(mCursor.getColumnIndex(ComicDatabase.COMICS_FAVORITE_KEY));
            mCart = mCursor.getString(mCursor.getColumnIndex(ComicDatabase.COMICS_CART_KEY));
            mCursor.close();
            // Populating view
            titleTextView = ((TextView) rootView.findViewById(R.id.title_text_view));
            titleTextView.setText(mComicName);
            ((TextView) rootView.findViewById(R.id.release_text_view)).setText(mComicRelease);
            ((TextView) rootView.findViewById(R.id.description_text_view)).setText(mComicDescription);
            ((TextView) rootView.findViewById(R.id.feature_text_view)).setText(mComicFeature);
            ((TextView) rootView.findViewById(R.id.price_text_view)).setText(mComicPrice);
            ImageView coverView = (ImageView) rootView.findViewById(R.id.cover_image_view);
            Picasso.with(getActivity())
                    .load(mComicCover)
                    .placeholder(R.drawable.comic_placeholder)
                    .error(R.drawable.comic_placeholder)
                    .resize(250, 383)
                    .into(coverView);

            SlidingUpPanelLayout layout = (SlidingUpPanelLayout) rootView.findViewById(R.id.sliding_layout);
            layout.setPanelSlideListener(this);

            ImageView arrowSx = (ImageView) rootView.findViewById(R.id.go_up_sx);
            ImageView arrowDx = (ImageView) rootView.findViewById(R.id.go_up_dx);

            if (!mUserLearnedSliding) {
                Drawable image1 = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_arrow_upward, null);
                Drawable image2 = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_arrow_upward, null);
                if (image1 != null && image2 != null) {
                    image1.setColorFilter(ContextCompat.getColor(getContext(), R.color.divider), PorterDuff.Mode.MULTIPLY);
                    image2.setColorFilter(ContextCompat.getColor(getContext(), R.color.primary), PorterDuff.Mode.MULTIPLY);

                    Drawable imagesToShow[] = {image1, image2};

                    animate(arrowSx, imagesToShow, 0, true);
                    animate(arrowDx, imagesToShow, 0, true);
                } else {
                    arrowDx.setVisibility(View.GONE);
                    arrowSx.setVisibility(View.GONE);
                }
            } else {
                arrowDx.setVisibility(View.GONE);
                arrowSx.setVisibility(View.GONE);
            }
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
            mMenu.getItem(1).setVisible(true);
            mMenu.getItem(2).setVisible(false);
        } else {
            // Comic can be removed from favorite
            mMenu.getItem(1).setVisible(false);
            mMenu.getItem(2).setVisible(true);
        }

        if (cartFlag.equalsIgnoreCase("no")) {
            // Comic can be added to cart
            mMenu.getItem(3).setVisible(true);
            mMenu.getItem(4).setVisible(false);
        } else {
            // Comic can be removed from cart
            mMenu.getItem(3).setVisible(false);
            mMenu.getItem(4).setVisible(true);
        }
    }

    private void enlargeTextView(final TextView textView) {
        final int startSize = 1;
        final int endSize = 5;
        final int animationDuration = 300; // Animation duration in ms

        ValueAnimator animator = ValueAnimator.ofInt(startSize, endSize);
        animator.setDuration(animationDuration);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int animatedValue = (int) valueAnimator.getAnimatedValue();
                textView.setMaxLines(animatedValue);
            }
        });

        animator.start();
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
                    Date date = elaborateDate(mComicRelease);
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
                    Log.i(TAG, "Add comic to favorite");
                    // Add comic to favorite
                    ContentValues mUpdateValues = new ContentValues();
                    mUpdateValues.put(ComicDatabase.COMICS_FAVORITE_KEY, "yes");
                    mFavorite = "yes";
                    // Defines selection criteria for the rows you want to update
                    mSelectionClause = ComicDatabase.COMICS_NAME_KEY +  "=?";
                    mSelectionArgs = new String[]{mComicName};
                    ComicDatabaseManager.update(getActivity(), mUpdateValues, mSelectionClause, mSelectionArgs);
                    // In order to avoid conflict while deleting comics save a copy of comic with a new editor name
                    ComicDatabaseManager.insert(getActivity(), mComicName, Constants.Editors.getName(Constants.Editors.FAVORITE), mComicDescription, mComicRelease, elaborateDate(mComicRelease), mComicCover, mComicFeature, mComicPrice, mCart, mFavorite);
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
                    mSelectionClause = ComicDatabase.COMICS_NAME_KEY + "=?";
                    mSelectionArgs = new String[]{mComicName};
                    ComicDatabaseManager.update(getActivity(), mUpdateValues, mSelectionClause, mSelectionArgs);
                    // Delete the copy with the different editor
                    // Defines selection criteria for the rows to delete
                    mSelectionClause = ComicDatabase.COMICS_EDITOR_KEY + "=? AND " + ComicDatabase.COMICS_NAME_KEY + "=?";
                    mSelectionArgs = new String[]{Constants.Editors.getName(Constants.Editors.FAVORITE), mComicName};
                    ComicDatabaseManager.delete(getActivity(), ComicContentProvider.CONTENT_URI, mSelectionClause, mSelectionArgs);
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
                    mSelectionClause = ComicDatabase.COMICS_NAME_KEY +  "=?";
                    mSelectionArgs = new String[]{mComicName};
                    ComicDatabaseManager.update(getActivity(), mUpdateValues, mSelectionClause, mSelectionArgs);
                    // In order to avoid conflict while deleting comics save a copy of comic with a new editor name
                    ComicDatabaseManager.insert(getActivity(), mComicName, Constants.Editors.getName(Constants.Editors.CART), mComicDescription, mComicRelease, elaborateDate(mComicRelease), mComicCover, mComicFeature, mComicPrice, mCart, mFavorite);
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
                    mSelectionClause = ComicDatabase.COMICS_NAME_KEY + "=?";
                    mSelectionArgs = new String[]{mComicName};
                    ComicDatabaseManager.update(getActivity(), mUpdateValues, mSelectionClause, mSelectionArgs);
                    // Delete the copy with the different editor
                    // Defines selection criteria for the rows to delete
                    mSelectionClause = ComicDatabase.COMICS_EDITOR_KEY + "=? AND " + ComicDatabase.COMICS_NAME_KEY + "=?";
                    mSelectionArgs = new String[]{Constants.Editors.getName(Constants.Editors.CART), mComicName};
                    ComicDatabaseManager.delete(getActivity(), ComicContentProvider.CONTENT_URI, mSelectionClause, mSelectionArgs);
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

    private Date elaborateDate(String releaseDate) {
        Log.d(TAG, "elaborateDate - start");
        Date date = null;
        try {
            date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(releaseDate);
        } catch (ParseException e) {
            Log.w(TAG, "Error while elaborating Date from " + mComicRelease + " " + e.toString());
        } finally {
            if (date == null) {
                date = new Date();
                date.setTime(System.currentTimeMillis());
            }
        }
        Log.v(TAG, "elaborateDate - end - " + date.toString());
        return date;
    }

    /**
     * This method will fade out the arrow on info panel.
     * @param imageView The View which displays the images
     * @param images Holds R references to the images to display
     * @param imageIndex index of the first image to show in images[]
     * @param forever If true then after the last image it starts all over again with the first image resulting in an infinite loop
     */
    private void animate(final ImageView imageView, final Drawable images[], final int imageIndex, final boolean forever) {

        int fadeInDuration = 500; // Configure time values
        int timeBetween = 3000;
        int fadeOutDuration = 1000;

        imageView.setVisibility(View.INVISIBLE);    // Visible or invisible by default - this will apply when the animation ends
        imageView.setImageDrawable(images[imageIndex]);

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

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
        // Anything you put here is going to happen if user do a slide
    }

    @Override
    public void onPanelCollapsed(View panel) {
        // Anything you put here is going to happen if the view is closed
        titleTextView.setMaxLines(1);
    }

    @Override
    public void onPanelExpanded(View panel) {
        enlargeTextView(titleTextView);
        // Anything you put here is going to happen if the view is open
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.edit().putBoolean(Constants.PREF_USER_LEARNED_SLIDING_UP, true).apply();
    }

    @Override
    public void onPanelAnchored(View panel) {
        // Anything you put here is going to happen if the view is anchored
    }

    @Override
    public void onPanelHidden(View panel) {
        // Anything you put here is going to happen if the view is hidden
    }
}

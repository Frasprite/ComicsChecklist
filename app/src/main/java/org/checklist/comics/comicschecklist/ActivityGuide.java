package org.checklist.comics.comicschecklist;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

/**
 * Activity used to show to user a guide about application.
 */
public class ActivityGuide extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // TODO add screenshot on fragments and update code
        // Instead of fragments, you can also use our default slide
        addSlide(AppIntroFragment.newInstance(getResources().getString(R.string.activity_help_home),
                getResources().getString(R.string.activity_help_home_text),
                R.drawable.intro_main,
                ContextCompat.getColor(this, R.color.primary)));

        addSlide(AppIntroFragment.newInstance(getResources().getString(R.string.activity_help_list),
                getResources().getString(R.string.activity_help_list_text),
                R.drawable.intro_editori,
                ContextCompat.getColor(this, R.color.primary)));

        addSlide(AppIntroFragment.newInstance(getResources().getString(R.string.activity_help_detail),
                getResources().getString(R.string.activity_help_detail_text),
                R.drawable.intro_dettagli,
                ContextCompat.getColor(this, R.color.primary)));

        /*addSlide(AppIntroFragment.newInstance(getResources().getString(R.string.activity_help_favorite),
                getResources().getString(R.string.activity_help_favorite_text),
                R.drawable.preferiti,
                ContextCompat.getColor(this, R.color.primary)));*/

        addSlide(AppIntroFragment.newInstance(getResources().getString(R.string.activity_help_buy),
                getResources().getString(R.string.activity_help_buy_text),
                R.drawable.intro_comprare,
                ContextCompat.getColor(this, R.color.primary)));

        addSlide(AppIntroFragment.newInstance(getResources().getString(R.string.activity_help_settings),
                getResources().getString(R.string.activity_help_settings_text),
                R.drawable.intro_impostazioni,
                ContextCompat.getColor(this, R.color.primary)));

        setDoneText(getResources().getText(R.string.dialog_confirm_button));

        setZoomAnimation();
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // Just close the activity
        this.finish();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // Just close the activity
        this.finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }
}

package org.checklist.comics.comicschecklist;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.model.SliderPage;

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

        // Adding a slide for each guide
        SliderPage sliderPage1 = new SliderPage();
        sliderPage1.setTitle(getResources().getString(R.string.activity_help_home));
        sliderPage1.setDescription(getResources().getString(R.string.activity_help_home_text));
        sliderPage1.setImageDrawable(R.drawable.intro_main);
        sliderPage1.setBgColor(ContextCompat.getColor(this, R.color.primary));
        addSlide(AppIntroFragment.newInstance(sliderPage1));

        SliderPage sliderPage2 = new SliderPage();
        sliderPage2.setTitle(getResources().getString(R.string.activity_help_list));
        sliderPage2.setDescription(getResources().getString(R.string.activity_help_list_text));
        sliderPage2.setImageDrawable(R.drawable.intro_editors);
        sliderPage2.setBgColor(ContextCompat.getColor(this, R.color.primary));
        addSlide(AppIntroFragment.newInstance(sliderPage2));

        SliderPage sliderPage3 = new SliderPage();
        sliderPage3.setTitle(getResources().getString(R.string.activity_help_detail));
        sliderPage3.setDescription(getResources().getString(R.string.activity_help_detail_text));
        sliderPage3.setImageDrawable(R.drawable.intro_details);
        sliderPage3.setBgColor(ContextCompat.getColor(this, R.color.primary));
        addSlide(AppIntroFragment.newInstance(sliderPage3));

        SliderPage sliderPage4 = new SliderPage();
        sliderPage4.setTitle(getResources().getString(R.string.activity_help_buy));
        sliderPage4.setDescription(getResources().getString(R.string.activity_help_buy_text));
        sliderPage4.setImageDrawable(R.drawable.intro_add);
        sliderPage4.setBgColor(ContextCompat.getColor(this, R.color.primary));
        addSlide(AppIntroFragment.newInstance(sliderPage4));

        SliderPage sliderPage5 = new SliderPage();
        sliderPage5.setTitle(getResources().getString(R.string.activity_help_settings));
        sliderPage5.setDescription(getResources().getString(R.string.activity_help_settings_text));
        sliderPage5.setImageDrawable(R.drawable.intro_settings);
        sliderPage5.setBgColor(ContextCompat.getColor(this, R.color.primary));
        addSlide(AppIntroFragment.newInstance(sliderPage5));

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

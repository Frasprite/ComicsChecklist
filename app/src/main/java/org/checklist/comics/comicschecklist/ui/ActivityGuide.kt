package org.checklist.comics.comicschecklist.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat

import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntroFragment
import com.github.paolorotolo.appintro.model.SliderPage

import org.checklist.comics.comicschecklist.R

/**
 * Activity used to show to user a guide about application.
 */
class ActivityGuide : AppIntro() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val actionBar = supportActionBar
        actionBar?.hide()

        // Adding a slide for each guide
        val sliderPage1 = SliderPage()
        sliderPage1.title = resources.getString(R.string.activity_help_home)
        sliderPage1.description = resources.getString(R.string.activity_help_home_text)
        sliderPage1.imageDrawable = R.drawable.intro_main
        sliderPage1.bgColor = ContextCompat.getColor(this, R.color.primary)
        addSlide(AppIntroFragment.newInstance(sliderPage1))

        val sliderPage2 = SliderPage()
        sliderPage2.title = resources.getString(R.string.activity_help_list)
        sliderPage2.description = resources.getString(R.string.activity_help_list_text)
        sliderPage2.imageDrawable = R.drawable.intro_editors
        sliderPage2.bgColor = ContextCompat.getColor(this, R.color.primary)
        addSlide(AppIntroFragment.newInstance(sliderPage2))

        val sliderPage3 = SliderPage()
        sliderPage3.title = resources.getString(R.string.activity_help_detail)
        sliderPage3.description = resources.getString(R.string.activity_help_detail_text)
        sliderPage3.imageDrawable = R.drawable.intro_details
        sliderPage3.bgColor = ContextCompat.getColor(this, R.color.primary)
        addSlide(AppIntroFragment.newInstance(sliderPage3))

        val sliderPage4 = SliderPage()
        sliderPage4.title = resources.getString(R.string.activity_help_buy)
        sliderPage4.description = resources.getString(R.string.activity_help_buy_text)
        sliderPage4.imageDrawable = R.drawable.intro_add
        sliderPage4.bgColor = ContextCompat.getColor(this, R.color.primary)
        addSlide(AppIntroFragment.newInstance(sliderPage4))

        val sliderPage5 = SliderPage()
        sliderPage5.title = resources.getString(R.string.activity_help_settings)
        sliderPage5.description = resources.getString(R.string.activity_help_settings_text)
        sliderPage5.imageDrawable = R.drawable.intro_settings
        sliderPage5.bgColor = ContextCompat.getColor(this, R.color.primary)
        addSlide(AppIntroFragment.newInstance(sliderPage5))

        setDoneText(resources.getText(R.string.dialog_confirm_button))

        setZoomAnimation()
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        // Just close the activity
        this.finish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        // Just close the activity
        this.finish()
    }
}

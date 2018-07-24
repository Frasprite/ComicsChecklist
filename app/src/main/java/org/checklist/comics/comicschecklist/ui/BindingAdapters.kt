package org.checklist.comics.comicschecklist.ui

import android.databinding.BindingAdapter
import android.view.View

@BindingAdapter("visibleGone")
fun showHide(view: View, show: Boolean) {
    view.visibility = if (show) View.VISIBLE else View.GONE
}

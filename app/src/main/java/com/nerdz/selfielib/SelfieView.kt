package com.nerdz.selfielib

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.FragmentManager
import com.nerdz.selfielib.models.CameraResult
import com.nerdz.selfielib.screens.camera_screen.SelfieFragment

/**
 * This view needs to be integrated to an activity/fragment in order to use Selfie Camera.
 */
class SelfieView(context: Context, attributeSet: AttributeSet?) : LinearLayout(context, attributeSet) {
    private var selfieFragment: SelfieFragment

    init {
        inflate(context, R.layout.layout_selfie, this)
        selfieFragment = SelfieFragment()
    }

    /**
     * After integrating the view, this function needs to be called with a listener
     * So that library can notify app when there is a selfie taken.
     * @param listener a lambda implementation of listener.
     */
    fun start(listener: ((CameraResult)->Unit)) {
        selfieFragment.listener = listener
        val fragmentManager = getFragmentManager(context)
        fragmentManager?.beginTransaction()?.add(R.id.fragmentContainerView, selfieFragment)?.commit()
    }

    private fun getFragmentManager(context: Context?): FragmentManager? {
        return when (context) {
            is AppCompatActivity -> context.supportFragmentManager
            is ContextThemeWrapper -> getFragmentManager(context.baseContext)
            else -> null
        }
    }

}
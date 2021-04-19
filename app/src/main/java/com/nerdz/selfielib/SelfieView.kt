package com.nerdz.selfielib

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.FragmentManager
import com.nerdz.selfielib.models.CameraResult
import com.nerdz.selfielib.screens.camera_screen.SelfieFragment

class SelfieView(context: Context, attributeSet: AttributeSet?) : LinearLayout(context, attributeSet) {

    private var selfieFragment: SelfieFragment

    init {
        inflate(context, R.layout.layout_selfie, this)
        selfieFragment = SelfieFragment()
    }

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
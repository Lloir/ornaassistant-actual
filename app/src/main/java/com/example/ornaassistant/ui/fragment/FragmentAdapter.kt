package com.example.ornaassistant.ui.fragment

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class FragmentAdapter(fragmentManager: FragmentManager,
                      lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    var view: View? = null
    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int): Fragment {

        var frag: Fragment = when (position) {
            0 -> MainFragment()
            1 -> KingdomFragment()
            else -> MainFragment()
        }
        view = frag.view

        return frag
    }
}
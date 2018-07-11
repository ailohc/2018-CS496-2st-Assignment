package cs496.second.home

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.util.Log

class PageAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                ThirdFragment()
            }
            1 -> FirstFragment()
            else -> {
                Log.d("************", "2ndfragmentcalled")
                return SecondFragment()
            }
        }
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "Chat"
            1 -> "Contact"
            else -> {
                return "Gallery"
            }
        }
    }
}
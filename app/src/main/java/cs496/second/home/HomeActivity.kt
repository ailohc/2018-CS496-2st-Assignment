package cs496.second.home

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View
import cs496.second.R
import android.support.annotation.RequiresApi


class HomeActivity : AppCompatActivity() {

    private var mSectionsPagerAdapter: PageAdapter?=null
    private var mViewPager: ViewPager? = null

    companion object {
        fun newIntent(context: Context): Intent =
                Intent(context, HomeActivity::class.java)
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)
        mSectionsPagerAdapter = PageAdapter(supportFragmentManager)
        mViewPager = findViewById<ViewPager?>(R.id.container)
        mViewPager!!.adapter = mSectionsPagerAdapter
        val tabLayout = findViewById<View>(R.id.tabs) as TabLayout
        tabLayout.setupWithViewPager(mViewPager)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }
}
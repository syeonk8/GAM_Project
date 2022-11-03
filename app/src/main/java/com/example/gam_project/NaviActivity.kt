package com.example.gam_project

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
//import com.example.odop.databinding.ActivityNaviBinding
import com.example.gam_project.databinding.ActivityNaviBinding

private const val TAG_CALENDER = "calender_fragment"
private const val TAG_MAP = "map_fragment"
private const val TAG_MY_PAGE = "my_page_fragment"


class NaviActivity : AppCompatActivity() {

    private lateinit var binding : ActivityNaviBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNaviBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setFragment(TAG_CALENDER,CalenderFragment())

        binding.navigationView.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.calenderFragment -> setFragment(TAG_CALENDER, CalenderFragment())
                R.id.map -> setFragment(TAG_MAP, MapFragment())
                R.id.myPageFragment-> setFragment(TAG_MY_PAGE, MyPageFragment())
            }
            true
        }
    }

    private fun setFragment(tag: String, fragment: Fragment) {
        val manager: FragmentManager = supportFragmentManager
        val fragTransaction = manager.beginTransaction()

        if (manager.findFragmentByTag(tag) == null){
            fragTransaction.add(R.id.mainFrameLayout, fragment, tag)
        }
        val calender = manager.findFragmentByTag(TAG_CALENDER)
        val map = manager.findFragmentByTag(TAG_MAP)
        val myPage = manager.findFragmentByTag(TAG_MY_PAGE)


        if (calender != null){
            fragTransaction.hide(calender)
        }

        if (map != null) {
            fragTransaction.hide(map)
        }

        if (myPage != null) {
            fragTransaction.hide(myPage)
        }


        if (tag == TAG_CALENDER) {
            if (calender!=null){
                fragTransaction.show(calender)
            }
        }


        else if (tag == TAG_MAP){
            if (map != null){
                fragTransaction.show(map)
            }
        }

        else if (tag == TAG_MY_PAGE){
            if (myPage != null){
                fragTransaction.show(myPage)
            }
        }


        fragTransaction.commitAllowingStateLoss()
    }
}
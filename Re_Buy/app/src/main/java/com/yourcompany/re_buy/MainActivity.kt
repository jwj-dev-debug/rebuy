package com.yourcompany.re_buy

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.yourcompany.re_buy.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private val tabTitles by lazy {
        listOf(
            getString(R.string.tab_home),
            getString(R.string.tab_search),
            getString(R.string.tab_community),
            "지도" // Map tab
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // 툴바 설정
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // ViewPager2와 Fragment Adapter 설정
        binding.viewPager.adapter = ViewPagerAdapter(this)

        // TabLayout과 ViewPager2 연결
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        // Setup auth buttons
        setupAuthButtons()
    }

    override fun onResume() {
        super.onResume()
        // Update auth buttons when returning to this activity
        setupAuthButtons()
    }

    private fun setupAuthButtons() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // User is logged in - show logout button only
            binding.btnLoginMain.visibility = View.GONE
            binding.btnSignupMain.apply {
                visibility = View.VISIBLE
                text = getString(R.string.logout)
                setOnClickListener {
                    auth.signOut()
                    setupAuthButtons() // Refresh UI after logout
                }
            }
        } else {
            // User is not logged in - show login and signup buttons
            binding.btnLoginMain.apply {
                visibility = View.VISIBLE
                text = getString(R.string.login)
                setOnClickListener {
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                }
            }
            binding.btnSignupMain.apply {
                visibility = View.VISIBLE
                text = getString(R.string.sign_up)
                setOnClickListener {
                    startActivity(Intent(this@MainActivity, RegisterActivity::class.java))
                }
            }
        }

        // Refresh menu to show/hide profile icon based on login status
        invalidateOptionsMenu()
    }

    // Public method to allow fragments to switch tabs
    fun switchToTab(position: Int) {
        if (position in 0 until tabTitles.size) {
            binding.viewPager.currentItem = position
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Only show profile menu if user is logged in
        if (auth.currentUser != null) {
            menuInflater.inflate(R.menu.menu_main, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                val intent = Intent(this, MyProfileActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private inner class ViewPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = tabTitles.size
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> HomeFragment()
                1 -> SearchFragment()
                2 -> CommunityFragment()
                3 -> MapFragment()
                else -> throw IllegalStateException("Invalid position")
            }
        }
    }
}
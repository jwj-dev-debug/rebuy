package com.yourcompany.re_buy

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.yourcompany.re_buy.databinding.ActivityMyProfileBinding

class MyProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyProfileBinding
    private val auth = FirebaseAuth.getInstance()

    private val tabTitles = listOf(
        "내 게시글",
        "제품 즐겨찾기",
        "게시글 즐겨찾기"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupUserInfo()
        setupViewPager()
        setupImpactDashboardButton()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "내 프로필"
        }
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupUserInfo() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            binding.tvUserName.text = currentUser.displayName ?: "사용자"
            binding.tvUserEmail.text = currentUser.email ?: ""
        }
    }

    private fun setupImpactDashboardButton() {
        binding.cardImpactDashboard.setOnClickListener {
            val intent = Intent(this, ImpactDashboardActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupViewPager() {
        binding.viewPager.adapter = ProfileViewPagerAdapter(this)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    private inner class ProfileViewPagerAdapter(activity: AppCompatActivity) :
        FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = tabTitles.size

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> MyPostsFragment()
                1 -> ProductFavoritesFragment()
                2 -> FavoritesFragment()
                else -> throw IllegalStateException("Invalid position")
            }
        }
    }
}
